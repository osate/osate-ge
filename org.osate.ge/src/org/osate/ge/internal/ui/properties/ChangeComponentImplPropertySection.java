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
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.util.AadlClassifierUtil;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class ChangeComponentImplPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> bo instanceof ComponentImplementation
					/*
					 * bo -> (bo instanceof ComponentType || bo instanceof FeatureGroupType)
					 * && ((Classifier) bo).getElementRoot() instanceof AadlPackage
					 */);
		}
	}

	private BusinessObjectSelection selectedBos;
	private ComboViewer comboViewer;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite container = getWidgetFactory().createFlatFormComposite(parent);

		comboViewer = PropertySectionUtil.createComboViewer(container, STANDARD_LABEL_WIDTH, listener, labelProvider);
		PropertySectionUtil.createSectionLabel(container, comboViewer.getCombo(), getWidgetFactory(), "Type:");
	}

	private final SelectionAdapter listener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			selectedBos.modify(ComponentImplementation.class, compImpl -> {
				// TODO use this in the createBOHandler
				final Classifier replacementClassifier = ((AadlPackage) compImpl.getElementRoot()).getPublicSection()
						.createOwnedClassifier((EClass) comboViewer.getStructuredSelection().getFirstElement());

				// Copy structural feature values to the replacement object.
				PropertySectionUtil.transferStructuralFeatureValues(compImpl, replacementClassifier);

				// Remove the old object
				EcoreUtil.remove(compImpl);
			});
		}
	};

	private final LabelProvider labelProvider = new LabelProvider() {
		@Override
		public String getText(final Object element) {
			final EClass componentType = (EClass) element;
			return StringUtil.camelCaseToUser(componentType.getName());
		}
	};

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<BusinessObjectContext> bocs = selectedBos.bocStream().collect(Collectors.toSet());
		final List<EClass> componentImpls = new ArrayList<>();
		EClass selectedCompImpl = null;

		for (final EClass compImplEClass : AadlClassifierUtil.getComponentImplementations().keySet()) {
			for (final BusinessObjectContext boc : bocs) {
				final ComponentImplementation compImpl = (ComponentImplementation) boc.getBusinessObject();
				if (compImpl.eClass() == compImplEClass) {
					selectedCompImpl = compImplEClass;
					break;
				}
			}

			componentImpls.add(compImplEClass);
		}

		comboViewer.setInput(componentImpls);
		if (bocs.size() == 1) {
			comboViewer.setSelection(new StructuredSelection(selectedCompImpl));
		}
	}
}
