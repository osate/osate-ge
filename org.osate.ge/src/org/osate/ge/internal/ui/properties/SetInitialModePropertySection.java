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
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.Mode;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class SetInitialModePropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBocCompatible(toTest, boc -> {
				if(boc.getBusinessObject() instanceof Mode) {
					final Mode mode = (Mode) boc.getBusinessObject();
					final Object parent = boc.getParent().getBusinessObject();
					return parent instanceof ComponentClassifier && mode.getContainingClassifier() == parent;
				}

				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private Button trueBtn;
	private Button falseBtn;

	private final SelectionListener initialModeListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Button btn = (Button) e.widget;
			final boolean isInitial = (boolean) btn.getData();
			if (btn.getSelection()) {
				selectedBos.modify(Mode.class, mode -> {
					if (isInitial) {
						final ComponentClassifier cc = (ComponentClassifier) mode.getContainingClassifier();
						for (final Mode m : cc.getOwnedModes()) {
							if (m.isInitial()) {
								m.setInitial(false);
							}
						}
					}

					mode.setInitial(isInitial);
				});
			}
		}
	};

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);

		final Composite initialModesContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), composite,
				STANDARD_LABEL_WIDTH);

		trueBtn = PropertySectionUtil.createButton(getWidgetFactory(), initialModesContainer, true, initialModeListener, "True",
				SWT.RADIO);
		falseBtn = PropertySectionUtil.createButton(getWidgetFactory(), initialModesContainer, false, initialModeListener,
				"False", SWT.RADIO);

		PropertySectionUtil.createSectionLabel(composite, initialModesContainer, getWidgetFactory(), "Initial:");
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<Mode> modes = selectedBos.boStream(Mode.class).collect(Collectors.toSet());
		final Iterator<Mode> it = modes.iterator();
		boolean isInitial = it.next().isInitial();
		while (it.hasNext()) {
			if (isInitial != it.next().isInitial()) {
				System.err.println("neither");
			}
		}

		trueBtn.setSelection(isInitial);
		falseBtn.setSelection(!isInitial);
	}
}
