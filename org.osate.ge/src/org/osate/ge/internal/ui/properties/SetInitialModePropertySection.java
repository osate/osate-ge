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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				if (bo instanceof Mode) {
					final Mode mode = (Mode) bo;
					return mode.getContainingClassifier() instanceof ComponentClassifier;
				}

				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private Button setInitialModeBtn;

	private final SelectionListener initialModeListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn.getSelection()) {
				selectedBos.modify(Mode.class, mode -> {
					final ComponentClassifier cc = (ComponentClassifier) mode.getContainingClassifier();
					for (final Mode m : cc.getOwnedModes()) {
						if (m.isInitial()) {
							m.setInitial(false);
						}
					}

					mode.setInitial(true);
				});
			} else {
				selectedBos.modify(Mode.class, mode -> {
					final ComponentClassifier cc = (ComponentClassifier) mode.getContainingClassifier();
					for (final Mode m : cc.getOwnedModes()) {
						if (m.isInitial()) {
							m.setInitial(false);
						}
					}
				});
			}
		}
	};

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		final Label sectionLabel = PropertySectionUtil.createSectionLabel(composite, getWidgetFactory(), "Initial:");
		setInitialModeBtn = PropertySectionUtil.createButton(getWidgetFactory(), composite, SWT.NONE,
				initialModeListener, "", SWT.CHECK);

		final FormData fd = new FormData();
		fd.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		fd.top = new FormAttachment(sectionLabel, 0, SWT.CENTER);
		setInitialModeBtn.setLayoutData(fd);
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<Mode> modes = selectedBos.boStream(Mode.class).collect(Collectors.toSet());
		// Get initial state of selected elements
		final Boolean isInitial = isInitial(modes);
		// Grayed state set if elements are mixed initial and not initial
		setInitialModeBtn.setGrayed(isInitial == null);
		// Set initial selection
		setInitialModeBtn.setSelection(isInitial == Boolean.TRUE);
	}

	private static Boolean isInitial(final Set<Mode> modes) {
		final Iterator<Mode> it = modes.iterator();
		final Boolean isInitial = it.next().isInitial();
		// Check if all modes are initial
		while (it.hasNext()) {
			if (isInitial != it.next().isInitial()) {
				return null;
			}
		}

		return isInitial;
	}
}
