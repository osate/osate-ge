package org.osate.ge.internal.ui.properties;

import java.util.HashSet;
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
import org.osate.aadl2.AbstractSubcomponent;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.internal.util.SubcomponentUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class ChangeSubcomponentTypePropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				return bo instanceof Subcomponent
						&& ((Subcomponent) bo).getContainingClassifier() instanceof ComponentImplementation;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private ComboViewer comboViewer;
	private EClass selectedEClass = null;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite container = getWidgetFactory().createFlatFormComposite(parent);
		comboViewer = PropertySectionUtil.createComboViewer(container, STANDARD_LABEL_WIDTH, scTypeSelectionListener, subcompTypeLabelProvider);
		PropertySectionUtil.createSectionLabel(container, getWidgetFactory(), "Type:");
	}

	private final SelectionAdapter scTypeSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final SubcomponentTypeElement ste = (SubcomponentTypeElement) comboViewer.getStructuredSelection().getFirstElement();
			// Check if selection has changed
			if (selectedEClass != ste.getEClass()) {
				selectedBos.modify(Subcomponent.class, sc -> {
					if (sc.eClass() != ste.getEClass()) {
						final ComponentImplementation ci = sc.getContainingComponentImpl();

						// Copy structural feature values to the replacement object.
						PropertySectionUtil.transferStructuralFeatureValues(sc,
								SubcomponentUtil.createSubcomponent(ci, ste.getEClass()));

						// Remove the old object
						EcoreUtil.remove(sc);
					}
				});
			}
		}
	};

	private final LabelProvider subcompTypeLabelProvider = new LabelProvider() {
		@Override
		public String getText(final Object element) {
			final SubcomponentTypeElement ste = (SubcomponentTypeElement) element;
			return ste.getEClassName();
		}
	};

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<NamedElement> subcomponents = selectedBos.boStream(NamedElement.class).collect(Collectors.toSet());
		selectedEClass = null;

		final Set<EClass> availableEClasses = new HashSet<>();
		selectedEClass = PropertySectionUtil.getSelectedEClassAndPopulateOptionsList(subcomponents,
				SubcomponentUtil.getSubcomponentTypes(),
				(sc, eClass) -> isCompatibleSupcomponentType(sc, eClass), availableEClasses);

		final Set<SubcomponentTypeElement> availableScTypeElements = availableEClasses.stream()
				.map(eClass -> new SubcomponentTypeElement(eClass)).collect(Collectors.toSet());

		comboViewer.setInput(availableScTypeElements);
		// Set comboviewer selection
		if (selectedEClass != null) {
			availableScTypeElements.stream().filter(scTypeElement -> selectedEClass == scTypeElement.getEClass())
			.findAny().ifPresent(scTypeElement -> {
				comboViewer.setSelection(new StructuredSelection(scTypeElement));
			});
		}
	}

	private static boolean isCompatibleSupcomponentType(final NamedElement ne,
			final EClass subcomponentType) {
		Subcomponent sc = (Subcomponent) ne;
		if (sc.getRefined() != null) {
			sc = sc.getRefined();
		}
		final ComponentImplementation ci = (ComponentImplementation) sc.getContainingClassifier();
		return SubcomponentUtil.canContainSubcomponentType(ci, subcomponentType)
				&& (sc.getRefined() == null || sc.getRefined() instanceof AbstractSubcomponent);
	}

	private class SubcomponentTypeElement {
		final EClass eClass;
		final String eClassName;

		private SubcomponentTypeElement(final EClass eClass) {
			this.eClass = eClass;
			this.eClassName = setEClassName(eClass.getName());
		}

		private String setEClassName(final String eClassName) {
			final String tmpEClassName = StringUtil.camelCaseToUser(eClassName);
			return tmpEClassName.substring(0, tmpEClassName.lastIndexOf(" "));
		}

		private String getEClassName() {
			return eClassName;
		}

		private EClass getEClass() {
			return eClass;
		}
	}
}
