package org.osate.ge.internal.ui.properties;

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
import org.osate.aadl2.Access;
import org.osate.aadl2.AccessType;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class AccessPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> bo instanceof Access);
		}
	}

	private BusinessObjectSelection selectedBos;
	private Button providesBtn;
	private Button requiresBtn;

	private final SelectionListener directionSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn.getSelection()) {
				final AccessType newType = (AccessType) e.widget.getData();
				selectedBos.modify(Access.class, a -> a.setKind(newType));
			}
		}
	};

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory().createFlatFormComposite(parent);

		final Composite directionContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), composite,
				STANDARD_LABEL_WIDTH);

		providesBtn = PropertySectionUtil.createButton(getWidgetFactory(), directionContainer, AccessType.PROVIDES,
				directionSelectionListener, "Provides", SWT.RADIO);
		requiresBtn = PropertySectionUtil.createButton(getWidgetFactory(), directionContainer, AccessType.REQUIRES,
				directionSelectionListener, "Requires", SWT.RADIO);

		PropertySectionUtil.createSectionLabel(composite, directionContainer, getWidgetFactory(), "Access Type:");
	}

	@Override
	public void setInput(final IWorkbenchPart part,
			final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<AccessType> selectedDirections = selectedBos.boStream(Access.class)
				.map(a -> a.getKind()).collect(Collectors.toSet());

		providesBtn.setSelection(selectedDirections.contains(AccessType.PROVIDES));
		requiresBtn.setSelection(selectedDirections.contains(AccessType.REQUIRES));
	}
}
