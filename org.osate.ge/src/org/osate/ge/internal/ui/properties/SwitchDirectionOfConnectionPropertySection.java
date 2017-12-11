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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ConnectedElement;
import org.osate.aadl2.Connection;
import org.osate.aadl2.ParameterConnection;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class SwitchDirectionOfConnectionPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				if (bo instanceof Connection) {
					Connection connection = (Connection) bo;
					if (connection.getRefined() != null) {
						connection = connection.getRefined();
					}

					final ComponentImplementation ci = connection.getSource().getContainingComponentImpl();
					return ci != null
							&& connection.getContainingClassifier() == ci/* && connection.getRefined() == null */;

				}

				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private Button bidirectionalBtn;
	private Button unidirectionalBtn;
	private Button switchDirectionBtn;

	private final SelectionListener directionSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn.getSelection()) {
				final Boolean bidirectionalValue = (Boolean) e.widget.getData();
				selectedBos.modify(Connection.class, connection -> {
					if (connection.getRefined() != null) {
						connection = connection.getRefined();
					}

					connection.setBidirectional(bidirectionalValue);
				});
			}
		}
	};

	private final SelectionListener switchDirectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			selectedBos.modify(Connection.class, connection -> {
				if (connection.getRefined() != null) {
					connection = connection.getRefined();
				}

				if (!(connection instanceof ParameterConnection)) {
					final ConnectedElement ceSource = connection.getSource();
					connection.setSource(connection.getDestination());
					connection.setDestination(ceSource);
				}
			});
		};
	};

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData ld;
//TODO revisit is the second composite necessary? (btnComp)
		final Composite btnComposite = getWidgetFactory().createComposite(composite);
		btnComposite.setLayout(new FormLayout());
		ld = new FormData();
		ld.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		btnComposite.setLayoutData(ld);

		final Composite directionContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(),
				btnComposite, 0);

		bidirectionalBtn = PropertySectionUtil.createButton(getWidgetFactory(), directionContainer, true,
				directionSelectionListener,
				"Bidirectional", SWT.RADIO);
		unidirectionalBtn = PropertySectionUtil.createButton(getWidgetFactory(), directionContainer, false,
				directionSelectionListener,
				"Unidirectional", SWT.RADIO);

		switchDirectionBtn = getWidgetFactory().createButton(btnComposite, "Switch Direction", SWT.PUSH);
		ld = new FormData();
		ld.left = new FormAttachment(directionContainer, ITabbedPropertyConstants.HSPACE);
		switchDirectionBtn.setLayoutData(ld);
		switchDirectionBtn.addSelectionListener(switchDirectionListener);

		PropertySectionUtil.createSectionLabel(composite, btnComposite, getWidgetFactory(), "Direction:");
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<Connection> selectedConnections = selectedBos.boStream(Connection.class).collect(Collectors.toSet());
		Boolean init = null;
		boolean enableSwitchDirection = true;
		for (final Connection connection : selectedConnections) {
			if (connection instanceof ParameterConnection) {
				enableSwitchDirection = false;
			}
			if (init == null) {
				init = connection.isAllBidirectional();
			} else {
				if (init != connection.isAllBidirectional()) {
					init = null;
					break;
				}
			}
		}

		// TODO handle null objects or keep the same?
		if (init != null) {
			bidirectionalBtn.setSelection(init);
			unidirectionalBtn.setSelection(!init);
		} else {
			bidirectionalBtn.setSelection(false);
			unidirectionalBtn.setSelection(false);
		}

		switchDirectionBtn.setEnabled(enableSwitchDirection);
	}
}
