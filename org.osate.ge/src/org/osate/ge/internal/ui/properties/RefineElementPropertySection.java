package org.osate.ge.internal.ui.properties;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import org.osate.aadl2.Access;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.Connection;
import org.osate.aadl2.DirectedFeature;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.FlowSpecification;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.RefinableElement;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.util.AadlConnectionUtil;
import org.osate.ge.internal.util.AadlFeatureUtil;
import org.osate.ge.internal.util.SubcomponentUtil;
import org.osate.ge.query.StandaloneQuery;
import org.osate.ge.services.QueryService;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class RefineElementPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBocCompatible(toTest, boc -> {
				final Object bo = boc.getBusinessObject();
				final Object parent = boc.getParent().getBusinessObject();
				if (bo instanceof Feature) {
					if (!(parent instanceof Classifier)) {
						return false;
					}

					final Feature feature = (Feature) bo;
					if (feature.getRefined() != null) {
						return true;
					}
					return feature.getContainingClassifier() != parent
							&& (parent instanceof FeatureGroupType || parent instanceof ComponentType);
				} else if (bo instanceof Connection) {
					if (!(parent instanceof ComponentImplementation)) {
						return false;
					}

					final Connection connection = (Connection) bo;
					if (connection.getRefined() != null) {
						return true;
					}

					final ComponentImplementation ci = (ComponentImplementation) parent;
					return connection.getContainingClassifier() != ci;
				} else if (bo instanceof FlowSpecification) {
					if (!(parent instanceof ComponentType)) {
						return false;
					}

					final FlowSpecification fs = (FlowSpecification) bo;
					if (fs.getRefined() != null) {
						return true;
					}

					return fs.getContainingClassifier() != parent;
				} else if (bo instanceof Subcomponent) {
					if (!(parent instanceof ComponentImplementation)) {
						return false;
					}

					final Subcomponent sc = (Subcomponent) bo;
					if (sc.getRefined() != null) {
						return true;
					}

					final ComponentImplementation ci = (ComponentImplementation) parent;
					return sc.getContainingClassifier() != ci
							&& SubcomponentUtil.canContainSubcomponentType(ci, sc.eClass());
				}
				return false;
			});
		}
	}

	private static final StandaloneQuery parentQuery = StandaloneQuery.create((root) -> root.ancestor(1));
	private BusinessObjectSelection selectedBos;
	private QueryService queryService;
	private Button trueBtn;
	private Button falseBtn;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite container = getWidgetFactory().createFlatFormComposite(parent);

		final Composite refinedContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), container,
				STANDARD_LABEL_WIDTH);

		trueBtn = PropertySectionUtil.createButton(getWidgetFactory(), refinedContainer, true, selectionAdapter, "True",
				SWT.RADIO);
		falseBtn = PropertySectionUtil.createButton(getWidgetFactory(), refinedContainer, false, selectionAdapter,
				"False", SWT.RADIO);

		PropertySectionUtil.createSectionLabel(container, refinedContainer, getWidgetFactory(), "Refine:");
	}

	private SelectionAdapter selectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn == falseBtn && btn.getSelection()) {
				selectedBos.modify(boc -> {
					if (boc.getBusinessObject() instanceof FlowSpecification) {
						return boc.getBusinessObject();
					}

					return boc.getBusinessObject();
				}, (modify, boc) -> {
					EcoreUtil.remove(modify);
				});
			} else if (btn == trueBtn && btn.getSelection()) {
				// TODO remove parentQuery and just used boc.getParent???
				selectedBos.modify(boc -> queryService.getFirstBusinessObject(parentQuery, boc), (container, boc) -> {
					if (boc.getBusinessObject() instanceof Feature) {
						final Feature feature = (Feature) boc.getBusinessObject();
						// Refine the feature
						final NamedElement newFeatureEl = AadlFeatureUtil.createFeature((Classifier) container,
								feature.eClass());
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
					} else if (boc.getBusinessObject() instanceof Connection) {
						final Connection connection = (Connection) boc.getBusinessObject();
						final org.osate.aadl2.Connection newAadlConnection = AadlConnectionUtil
								.createConnection((ComponentImplementation) container, connection.eClass());
						if (newAadlConnection != null) {
							newAadlConnection.setRefined(connection);
						}
					} else if (boc.getBusinessObject() instanceof FlowSpecification) {
						final FlowSpecification fs = (FlowSpecification) boc.getBusinessObject();
						final ComponentType ct = (ComponentType) container;
						// Refine the flow specification
						final FlowSpecification newFs = ct.createOwnedFlowSpecification();
						newFs.setKind(fs.getKind());
						newFs.setRefined(fs);
					} else if (boc.getBusinessObject() instanceof Subcomponent) {
						final Subcomponent sc = (Subcomponent) boc.getBusinessObject();
						// Refine the subcomponent
						final Subcomponent newSc = SubcomponentUtil
								.createSubcomponent((ComponentImplementation) container, sc.eClass());
						newSc.setRefined(sc);
					}
				});
			}
		}
	};

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
		queryService = Adapters.adapt(part, QueryService.class);
	}

	@Override
	public void refresh() {
		// TODO make sure it handles multiple selections
		final Set<RefinableElement> refinableElements = selectedBos.boStream(RefinableElement.class)
				.collect(Collectors.toSet());
		for (final RefinableElement refinable : refinableElements) {
			final boolean isRefined = refinable.getRefinedElement() != null;
			trueBtn.setSelection(isRefined);
			falseBtn.setSelection(!isRefined);
		}
	}
}
