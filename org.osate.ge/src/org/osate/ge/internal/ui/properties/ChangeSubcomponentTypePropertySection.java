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
import org.osate.aadl2.AbstractSubcomponent;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.internal.util.SubcomponentUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class ChangeSubcomponentTypePropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			// TODO revisit filter
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				return bo instanceof Subcomponent
						&& ((Subcomponent) bo).getContainingClassifier() instanceof ComponentImplementation;
			});
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
			selectedBos.modify(Subcomponent.class, sc -> {
				final ComponentImplementation ci = sc.getContainingComponentImpl();
				final Subcomponent replacementSc = SubcomponentUtil.createSubcomponent(ci,
						(EClass) comboViewer.getStructuredSelection().getFirstElement());

				// Copy structural feature values to the replacement object.
				PropertySectionUtil.transferStructuralFeatureValues(sc, replacementSc);

				// Remove the old object
				EcoreUtil.remove(sc);
			});
		}
	};

	private final LabelProvider labelProvider = new LabelProvider() {
		@Override
		public String getText(final Object element) {
			final EClass subcompType = (EClass) element;
			return StringUtil.camelCaseToUser(subcompType.getName());
		}
	};

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<BusinessObjectContext> bocs = selectedBos.bocStream()
				.filter(boc -> boc.getBusinessObject() instanceof Subcomponent).collect(Collectors.toSet());
		final List<EClass> subcomponentTypes = new ArrayList<>();
		EClass selectedSubcomponentType = null;

		for (final EClass subcomponentType : SubcomponentUtil.getSubcomponentTypes()) {
			boolean addSubType = false;
			for (final BusinessObjectContext boc : bocs) {
				final Subcomponent sc = (Subcomponent) boc.getBusinessObject();
				addSubType = isCompatibleSupcomponentType(sc, subcomponentType);
				if (!addSubType) {
					break;
				}

				selectedSubcomponentType = sc.eClass();
			}

			if (addSubType) {
				subcomponentTypes.add(subcomponentType);
			}
		}

		comboViewer.setInput(subcomponentTypes);
		if (bocs.size() == 1) {
			comboViewer.setSelection(new StructuredSelection(selectedSubcomponentType));
		}
	}

	private boolean isCompatibleSupcomponentType(final Subcomponent sc,
			final EClass subcomponentType) {
		final ComponentImplementation ci = (ComponentImplementation) sc.getContainingClassifier();
		return SubcomponentUtil.canContainSubcomponentType(ci, subcomponentType)
				&& (sc.getRefined() == null || sc.getRefined() instanceof AbstractSubcomponent);
	}
}
