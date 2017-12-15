package org.osate.ge.internal.ui.properties;

import java.util.Iterator;
import java.util.List;
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
							&& connection.getContainingClassifier() == ci;
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

		final Composite directionContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), composite,
				STANDARD_LABEL_WIDTH);
		bidirectionalBtn = PropertySectionUtil.createButton(getWidgetFactory(), directionContainer, true,
				directionSelectionListener,
				"Bidirectional", SWT.RADIO);
		unidirectionalBtn = PropertySectionUtil.createButton(getWidgetFactory(), directionContainer, false,
				directionSelectionListener,
				"Unidirectional", SWT.RADIO);
		switchDirectionBtn = PropertySectionUtil.createButton(getWidgetFactory(), composite, null,
				switchDirectionListener, "SwitchDirection", SWT.PUSH);
		final FormData ld = new FormData();
		ld.left = new FormAttachment(directionContainer, ITabbedPropertyConstants.HSPACE);
		ld.top = new FormAttachment(directionContainer, 0, SWT.CENTER);
		switchDirectionBtn.setLayoutData(ld);

		PropertySectionUtil.createSectionLabel(composite, directionContainer, getWidgetFactory(),
				"Direction:");
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final List<Connection> selectedConnections = selectedBos.boStream(Connection.class)
				.collect(Collectors.toList());

		if (selectedConnections.size() == 1) {
			final Connection connection = selectedConnections.get(0);
			final boolean isBidirectional = connection.isAllBidirectional();
			bidirectionalBtn.setSelection(isBidirectional);
			unidirectionalBtn.setSelection(!isBidirectional);
			switchDirectionBtn.setEnabled(connection instanceof ParameterConnection);
		} else {
			boolean enableSwitchDirection = false;
			final Iterator<Connection> it = selectedConnections.iterator();
			final Connection connection = it.next();
			if (!(connection instanceof ParameterConnection)) {
				enableSwitchDirection = true;
			}

			Boolean isBidirectional = connection.isAllBidirectional();
			while (it.hasNext()) {
				final Connection nxtConnection = it.next();
				if (!Boolean.valueOf(nxtConnection.isAllBidirectional()).equals(isBidirectional)) {
					isBidirectional = null;
					// Exit loop if obtained both initial control values
					if (enableSwitchDirection) {
						break;
					}
				}

				if (!enableSwitchDirection && !(nxtConnection instanceof ParameterConnection)) {
					enableSwitchDirection = true;
				}
			}

			if (isBidirectional != null) {
				bidirectionalBtn.setSelection(isBidirectional);
				unidirectionalBtn.setSelection(!isBidirectional);
			} else {
				bidirectionalBtn.setSelection(false);
				unidirectionalBtn.setSelection(false);
			}

			switchDirectionBtn.setEnabled(enableSwitchDirection);
		}
	}
}
