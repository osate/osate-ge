package org.osate.ge.internal.ui.properties;

import java.util.Iterator;
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
import org.osate.ge.ui.properties.PropertySectionUtil;

public class RefineElementPropertySection extends AbstractPropertySection {
	// TODO keep restrictions on refined elements?
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBocCompatible(toTest, boc -> {
				final Object bo = boc.getBusinessObject();
				if (bo instanceof RefinableElement) {
					final RefinableElement re = (RefinableElement) bo;
					final Object parent = boc.getParent().getBusinessObject();
					// Return true if element is refined
					if (re.getRefinedElement() != null) {
						return true;
					}

					if (re instanceof Feature) {
						return parent instanceof Classifier && re.getContainingClassifier() != parent
								&& (parent instanceof FeatureGroupType || parent instanceof ComponentType);
					} else if (re instanceof Connection) {
						return parent instanceof ComponentImplementation && re.getContainingClassifier() != parent;
					} else if (re instanceof FlowSpecification) {
						return parent instanceof ComponentType && re.getContainingClassifier() != parent;
					} else if (re instanceof Subcomponent) {
						return parent instanceof ComponentImplementation && re.getContainingClassifier() != parent
								&& SubcomponentUtil.canContainSubcomponentType((ComponentImplementation) parent,
										re.eClass());
					}
				}
				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private Button trueBtn;
	private Button falseBtn;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite container = getWidgetFactory().createFlatFormComposite(parent);
		final Composite refinedContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), container,
				STANDARD_LABEL_WIDTH);

		trueBtn = PropertySectionUtil.createButton(getWidgetFactory(), refinedContainer, true, refineSelectionListener, "True",
				SWT.RADIO);
		falseBtn = PropertySectionUtil.createButton(getWidgetFactory(), refinedContainer, false,
				refineSelectionListener, "False", SWT.RADIO);

		PropertySectionUtil.createSectionLabel(container, refinedContainer, getWidgetFactory(), "Refine:");
	}

	private SelectionAdapter refineSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn == falseBtn && btn.getSelection()) {
				selectedBos.modify(NamedElement.class, ne -> {

					EcoreUtil.remove(ne);
				});
			} else if (btn == trueBtn && btn.getSelection()) {
				selectedBos.modify(boc -> boc.getParent().getBusinessObject(), (container, boc) -> {
					if (boc.getBusinessObject() instanceof Feature) {
						final Feature feature = (Feature) boc.getBusinessObject();
						final NamedElement newFeatureEl = AadlFeatureUtil.createFeature((Classifier) container,
								feature.eClass());
						final Feature newFeature = (Feature) newFeatureEl;
						// Refine the feature
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
						// Refine the connection
						newAadlConnection.setRefined(connection);
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
	}

	@Override
	public void refresh() {
		final Set<RefinableElement> refinableElements = selectedBos.boStream(RefinableElement.class)
				.collect(Collectors.toSet());
		final Iterator<RefinableElement> it = refinableElements.iterator();
		// Initial value of buttons
		Boolean isRefined = it.next().getRefinedElement() != null;

		while (it.hasNext()) {
			// Check if all elements are refined or not refined
			if (isRefined != (it.next().getRefinedElement() != null)) {
				isRefined = null;
				break;
			}
		}

		// No selection set if elements are mixed refined and not refined
		if (isRefined != null) {
			// Set initial selection
			trueBtn.setSelection(isRefined);
			falseBtn.setSelection(!isRefined);
		} else {
			trueBtn.setSelection(false);
			falseBtn.setSelection(false);
		}
	}
}
