package org.osate.ge.internal.ui.properties;

import java.util.ArrayList;
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
import org.osate.aadl2.EventDataPort;
import org.osate.aadl2.Feature;
import org.osate.aadl2.InternalFeature;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.ProcessorFeature;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.util.AadlFeatureUtil;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class ChangeFeatureTypePropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {

			/*
			 * if (!(AadlFeatureUtil.canOwnFeatureType(feature.getContainingClassifier(), featureType)
						&& (!(feature instanceof Feature) || (((Feature) feature).getRefined() == null
						|| ((Feature) feature).getRefined() instanceof AbstractFeature)))) {
					addFeatureType = false;
					break;
				} else {
					System.err.println(featureType + " featureType");
				}
			 */

			return PropertySectionUtil.isBoCompatible(toTest,
					bo -> {
						if (bo instanceof Feature || bo instanceof InternalFeature || bo instanceof ProcessorFeature) {
							final NamedElement feature = (NamedElement) bo;
							for (final EClass featureType : AadlFeatureUtil.getFeatureTypes()) {
								if (AadlFeatureUtil.canOwnFeatureType(feature.getContainingClassifier(), featureType)
										&& (!(feature instanceof Feature) || (((Feature) feature).getRefined() == null
										|| ((Feature) feature).getRefined() instanceof AbstractFeature))) {
									return true;
								}
							}
						}

						return false;
						// return bo instanceof Feature || bo instanceof InternalFeature || bo instanceof ProcessorFeature;
					});
		}
	}
	// TODO Look into why it breaks with certain type changes, do these need a field initialized?

	private BusinessObjectSelection selectedBos;
	private ComboViewer comboViewer;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite container = getWidgetFactory().createFlatFormComposite(parent);

		comboViewer = PropertySectionUtil.createComboViewer(container, STANDARD_LABEL_WIDTH, new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				selectedBos.modify(NamedElement.class, ne -> {
					// If selected FeatureType does not match FeatureType
					if (ne.eClass() != comboViewer.getStructuredSelection().getFirstElement()) {
						final Classifier featureOwner = ne.getContainingClassifier();
						final NamedElement replacementFeature = AadlFeatureUtil.createFeature(featureOwner,
								(EClass) comboViewer.getStructuredSelection().getFirstElement());

						// Copy structural feature values to the replacement object.
						PropertySectionUtil.transferStructuralFeatureValues(ne, replacementFeature);

						// Handle copying the data feature classifier
						if (replacementFeature instanceof EventDataPort) {
							((EventDataPort) replacementFeature)
							.setDataFeatureClassifier(getDataFeatureClassifier(ne));
						} else if (replacementFeature instanceof DataPort) {
							((DataPort) replacementFeature).setDataFeatureClassifier(getDataFeatureClassifier(ne));
						}

						// Remove the old object
						EcoreUtil.remove(ne);
					}
				});
			}
		}, new LabelProvider() {
			@Override
			public String getText(final Object element) {
				final EClass featureType = (EClass) element;
				return StringUtil.camelCaseToUser(featureType.getName());
			}
		});

		PropertySectionUtil.createSectionLabel(container, comboViewer.getCombo(), getWidgetFactory(), "Type:");
	}

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
		final Set<BusinessObjectContext> bocs = selectedBos.bocStream().filter(boc -> {
			final Object bo = boc.getBusinessObject();
			return bo instanceof Feature || bo instanceof InternalFeature || bo instanceof ProcessorFeature;
		}).collect(Collectors.toSet());
		final List<EClass> featureTypes = new ArrayList<>();
		EClass selectedFeatureType = null;

		// TODO allow breaking or stop??
		for (final EClass featureType : AadlFeatureUtil.getFeatureTypes()) {
			boolean addFeatureType = true;
			for (final BusinessObjectContext boc : bocs) {
				final NamedElement feature = (NamedElement) boc.getBusinessObject();
				selectedFeatureType = feature.eClass();

				if (!(AadlFeatureUtil.canOwnFeatureType(feature.getContainingClassifier(), featureType)
						&& (!(feature instanceof Feature) || (((Feature) feature).getRefined() == null
						|| ((Feature) feature).getRefined() instanceof AbstractFeature)))) {
					addFeatureType = false;
					break;
				}
			}

			if (addFeatureType) {
				featureTypes.add(featureType);
			}
		}

		comboViewer.setInput(featureTypes);
		if (bocs.size() == 1) {
			comboViewer.setSelection(new StructuredSelection(selectedFeatureType));
		}
	}
}
