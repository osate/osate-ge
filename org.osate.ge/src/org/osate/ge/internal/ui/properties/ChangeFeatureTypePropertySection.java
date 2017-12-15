package org.osate.ge.internal.ui.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.DataSubcomponentType;
import org.osate.aadl2.DirectedFeature;
import org.osate.aadl2.EventDataPort;
import org.osate.aadl2.Feature;
import org.osate.aadl2.InternalFeature;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.ProcessorFeature;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.util.AadlFeatureUtil;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class ChangeFeatureTypePropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				if (bo instanceof NamedElement) {
					final NamedElement ne = (NamedElement) bo;
					// Check that the shape represents a feature and that the classifier can contain features of the type this feature changes features into.
					if (!(ne instanceof Feature || ne instanceof InternalFeature || ne instanceof ProcessorFeature)) {
						return false;
					}

					for (final EClass featureType : AadlFeatureUtil.getFeatureTypes()) {
						if (isValidFeatureType(ne, featureType)) {
							return true;
						}
					}
				}

				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private ComboViewer comboViewer;
	private EClass selectedFeatureType = null;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite container = getWidgetFactory().createFlatFormComposite(parent);
		// TODO Quick selections cause problems?
		comboViewer = PropertySectionUtil.createComboViewer(container, STANDARD_LABEL_WIDTH, featureTypeSelectionListener, featureTypeLabelProvider);
		PropertySectionUtil.createSectionLabel(container, comboViewer.getCombo(), getWidgetFactory(), "Type:");
	}

	private final SelectionAdapter featureTypeSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			// Check if selection changed
			if (selectedFeatureType != comboViewer.getStructuredSelection().getFirstElement()) {
				selectedBos.modify(boc -> (NamedElement) boc.getBusinessObject(), (feature, boc) -> {
					final Classifier featureOwner = feature.getContainingClassifier();
					final NamedElement replacementFeature = AadlFeatureUtil.createFeature(featureOwner,
							(EClass) comboViewer.getStructuredSelection().getFirstElement());

					// Copy structural feature values to the replacement object.
					PropertySectionUtil.transferStructuralFeatureValues(feature, replacementFeature);

					// Set direction
					if (!(feature instanceof DirectedFeature) && replacementFeature instanceof DirectedFeature) {
						final DirectedFeature df = (DirectedFeature) replacementFeature;
						// Only manually set if no direction is specified
						if (!df.isIn() && !df.isOut()) {
							final boolean in = getDirection((DiagramElement) boc);
							df.setIn(in);
							df.setOut(!in);
						}
					}

					// Handle copying the data feature classifier
					if (replacementFeature instanceof DirectedFeature) {
						if (replacementFeature instanceof EventDataPort) {
							((EventDataPort) replacementFeature)
							.setDataFeatureClassifier(getDataFeatureClassifier(feature));
						} else if (replacementFeature instanceof DataPort) {
							((DataPort) replacementFeature).setDataFeatureClassifier(getDataFeatureClassifier(feature));
						}
					}

					// Remove the old object
					EcoreUtil.remove(feature);

				});
			}
		}

		// TODO talk to philip about in or out based on top bottom
		private boolean getDirection(final DiagramElement de) {
			return de.getDockArea() == DockArea.LEFT || de.getDockArea() == DockArea.TOP;
		}
	};

	// TODO fix redundancy
	private final LabelProvider featureTypeLabelProvider = new LabelProvider() {
		@Override
		public String getText(final Object element) {
			final EClass featureType = (EClass) element;
			return StringUtil.camelCaseToUser(featureType.getName());
		}
	};

	private DataSubcomponentType getDataFeatureClassifier(final NamedElement feature) {
		if (feature instanceof EventDataPort) {
			return ((EventDataPort) feature).getDataFeatureClassifier();
		} else if (feature instanceof DataPort) {
			return ((DataPort) feature).getDataFeatureClassifier();
		}

		return null;
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<NamedElement> bocs = selectedBos.boStream(NamedElement.class).collect(Collectors.toSet());
		final List<EClass> featureTypes = new ArrayList<>();
		// Initial combo selected value
		selectedFeatureType = null;

		// Only add eligible feature types to the combo
		for (final EClass featureType : AadlFeatureUtil.getFeatureTypes()) {
			final Iterator<NamedElement> it = bocs.iterator();
			boolean addFeatureType = true;
			NamedElement feature = it.next();

			// Check if selected element can be converted to type
			addFeatureType = isValidFeatureType(feature, featureType);
			if (addFeatureType) {
				selectedFeatureType = feature.eClass();
				// Check the rest of selected elements if necessary
				while (addFeatureType && it.hasNext()) {
					feature = it.next();
					if (selectedFeatureType != feature.eClass()) {
						selectedFeatureType = null;
					}

					addFeatureType = isValidFeatureType(feature, featureType);
				}

				if (addFeatureType) {
					featureTypes.add(featureType);
				}
			}
		}

		comboViewer.setInput(featureTypes);
		if (selectedFeatureType != null) {
			comboViewer.setSelection(new StructuredSelection(selectedFeatureType));
		}
	}

	private static boolean isValidFeatureType(final NamedElement feature, final EClass featureType) {
		return AadlFeatureUtil.canOwnFeatureType(feature.getContainingClassifier(), featureType)
				&& (!(feature instanceof Feature) || (((Feature) feature).getRefined() == null
				|| ((Feature) feature).getRefined() instanceof AbstractFeature));
	}
}
