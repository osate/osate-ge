package org.osate.ge.internal.ui.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.Access;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.DataSubcomponentType;
import org.osate.aadl2.DirectedFeature;
import org.osate.aadl2.EventDataPort;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.InternalFeature;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.ProcessorFeature;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.util.AadlFeatureUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class RefineElementPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			/*
			 * final Object parent = queryService.getFirstBusinessObject(parentQuery, boc);
		if(!(parent instanceof Classifier)) {
			return false;
		}

		return feature.getContainingClassifier() != parent &&
				(parent instanceof FeatureGroupType || parent instanceof ComponentType);
			 */
			return PropertySectionUtil.isBoCompatible(toTest,
					bo -> {
						if (bo instanceof Feature) {
							final Feature feature = (Feature) bo;
							final Classifier containingClassifier = feature.getContainingClassifier();
							return containingClassifier instanceof FeatureGroupType
									|| containingClassifier instanceof ComponentType;
						}
						return false;
					});
		}
	}
	// TODO Look into why it breaks with certain type changes, do these need a field initialized?

	private BusinessObjectSelection selectedBos;
	// private Button refineBtn;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite container = getWidgetFactory().createFlatFormComposite(parent);

		final Composite refinedContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), container,
				STANDARD_LABEL_WIDTH);

		PropertySectionUtil.createButton(getWidgetFactory(), refinedContainer, true, selectionAdapter, "True",
				SWT.RADIO);
		PropertySectionUtil.createButton(getWidgetFactory(), refinedContainer, false, selectionAdapter, "False",
				SWT.RADIO);

		// PropertySectionUtil.createButton(getWidgetFactory(), container, null, selectionAdapter, "Refine", SWT.CHECK);
		PropertySectionUtil.createSectionLabel(container, refinedContainer, getWidgetFactory(), "Refined:");
	}

	private SelectionAdapter selectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn.getSelection()) {
				System.err.println("Selection " + btn.getData());
				if ((boolean) btn.getData()) {
					final List<Feature> features = selectedBos.boStream(Feature.class).collect(Collectors.toList());

					// Refine the feature
					final NamedElement newFeatureEl = AadlFeatureUtil.createFeature(featureOwner, feature.eClass());
					final Feature newFeature = (Feature) newFeatureEl;
					newFeature.setRefined(feature);

					if (feature instanceof DirectedFeature) {
						final DirectedFeature refinedDirectedFeature = (DirectedFeature) feature;
						final DirectedFeature newDirectedFeature = (DirectedFeature) newFeature;
						newDirectedFeature.setIn(refinedDirectedFeature.isIn());
						newDirectedFeature.setOut(refinedDirectedFeature.isOut());
					} else if (feature instanceof Access) {
						((Access) newFeature).setKind(((Access) feature).getKind());
					}
				}
			}
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
		final Set<BusinessObjectContext> bocs = selectedBos.bocStream().filter(boc -> {
			final Object bo = boc.getBusinessObject();
			return bo instanceof Feature || bo instanceof InternalFeature || bo instanceof ProcessorFeature;
		}).collect(Collectors.toSet());
		final List<EClass> featureTypes = new ArrayList<>();
		EClass selectedFeatureType = null;

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

		// comboViewer.setInput(featureTypes);
		// if (bocs.size() == 1) {
		// comboViewer.setSelection(new StructuredSelection(selectedFeatureType));
		// }
	}
}
