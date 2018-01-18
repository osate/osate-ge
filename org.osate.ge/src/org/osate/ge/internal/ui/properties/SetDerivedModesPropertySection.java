package org.osate.ge.internal.ui.properties;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.ComponentType;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class SetDerivedModesPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				if (bo instanceof ComponentType) {
					final ComponentType ct = (ComponentType) bo;
					return ct.getOwnedModeTransitions().size() == 0
							&& ct.getOwnedModes().size() > 0
							&& ct.getAllModes().size() == ct.getOwnedModes().size()
							&& ct.getAllModeTransitions().size() == ct.getOwnedModeTransitions().size();
				}

				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private Button trueBtn;
	private Button falseBtn;

	private final SelectionListener derivedListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn.getSelection()) {
				final boolean isDerived = (boolean) btn.getData();
				selectedBos.modify(ComponentType.class, ct -> ct.setDerivedModes(isDerived));
			}
		}
	};

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);

		final Composite derivedContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), composite,
				STANDARD_LABEL_WIDTH);

		trueBtn = PropertySectionUtil.createButton(getWidgetFactory(), derivedContainer, true,
				derivedListener, "True", SWT.RADIO);
		falseBtn = PropertySectionUtil.createButton(getWidgetFactory(), derivedContainer, false,
				derivedListener, "False", SWT.RADIO);

		PropertySectionUtil.createSectionLabel(composite, getWidgetFactory(), "Derived Modes:");
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<ComponentType> componentTypes = selectedBos.boStream(ComponentType.class).collect(Collectors.toSet());
		final Iterator<ComponentType> it = componentTypes.iterator();
		// Initial value of buttons
		Boolean isDerived = it.next().isDerivedModes();
		while (it.hasNext()) {
			// Check if all elements are derived or not derived
			if (isDerived != (it.next().isDerivedModes())) {
				isDerived = null;
				break;
			}
		}

		// No selection set if elements are mixed derived and not not derived
		if(isDerived != null) {
			trueBtn.setSelection(isDerived);
			falseBtn.setSelection(!isDerived);
		} else {
			trueBtn.setSelection(false);
			falseBtn.setSelection(false);
		}
	}
}
