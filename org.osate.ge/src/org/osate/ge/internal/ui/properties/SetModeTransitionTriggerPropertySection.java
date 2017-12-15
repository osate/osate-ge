package org.osate.ge.internal.ui.properties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ModeTransition;
import org.osate.aadl2.ModeTransitionTrigger;
import org.osate.aadl2.TriggerPort;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.ui.dialogs.ModeTransitionTriggerSelectionDialog;
import org.osate.ge.internal.ui.dialogs.ModeTransitionTriggerSelectionDialog.ModeTransitionTriggerInfo;
import org.osate.ge.ui.properties.PropertySectionUtil;

import com.google.common.collect.Streams;

public class SetModeTransitionTriggerPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBocCompatible(toTest, boc -> {
				if (boc.getBusinessObject() instanceof ModeTransition) {
					final ModeTransition modeTransition = (ModeTransition) boc.getBusinessObject();
					final Object parent = boc.getParent().getBusinessObject();
					// Check that the container is the same shape that owns the mode transition
					return parent instanceof ComponentClassifier && modeTransition.getContainingClassifier() == parent;
				}

				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private TableViewer tableViewer;
	private Button chooseBtn;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		FormData fd;

		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		final Composite tableComposite = getWidgetFactory().createComposite(composite);
		fd = new FormData();
		fd.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		fd.height = 100;
		fd.width = 200;
		tableComposite.setLayoutData(fd);

		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.HIDE_SELECTION);
		// Hide selection and highlighting
		tableViewer.getTable().addListener(SWT.EraseItem, event -> event.detail &= ~SWT.HOT & ~SWT.SELECTED);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		tableViewer.getTable().setHeaderVisible(true);
		final TableViewerColumn portCol = PropertySectionUtil.createTableColumnViewer(tableViewer, "Trigger Port",
				SWT.NONE,
				new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final TriggerPortElement tte = (TriggerPortElement) cell.getElement();
				final String portName = tte.union ? tte.tp.getName() : "<" + tte.tp.getName() + ">";
				cell.setText(portName);
			}
		});

		portCol.getColumn().setResizable(false);
		tableComposite.setLayout(createTableColumnLayout(portCol.getColumn()));

		chooseBtn = PropertySectionUtil.createButton(getWidgetFactory(), composite, null,
				setModeTransitionTriggerSelectionListener, "Choose...", SWT.PUSH);

		fd = new FormData();
		fd.left = new FormAttachment(tableComposite, ITabbedPropertyConstants.HSPACE);
		fd.top = new FormAttachment(tableComposite, 0, SWT.CENTER);
		chooseBtn.setLayoutData(fd);

		PropertySectionUtil.createSectionLabel(composite, tableComposite,
				getWidgetFactory(), "Triggers:");
	}

	private final SelectionAdapter setModeTransitionTriggerSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final ModeTransition modeTransition = selectedBos.boStream(ModeTransition.class)
					.collect(Collectors.toList()).get(0);
			final ComponentClassifier cc = (ComponentClassifier) modeTransition.getContainingClassifier();
			final ModeTransitionTriggerInfo[] selectedTriggers = ModeTransitionTriggerSelectionDialog
					.promptForTriggers(cc, modeTransition);
			if (selectedTriggers != null) {
				selectedBos.modify(ModeTransition.class, mt -> {
					// Remove all trigger port triggers from the mode transition
					mt.getOwnedTriggers().clear();

					// Add the selected ones to it
					for (ModeTransitionTriggerInfo selectedPort : selectedTriggers) {
						final ModeTransitionTrigger mtt = mt.createOwnedTrigger();
						mtt.setTriggerPort(selectedPort.port);
						mtt.setContext(selectedPort.context);
					}
				});
				// TODO make sure no other sections need a manual refresh Manually refresh to show changes in input
				refresh();
			}
		}
	};

	// TODO move to util? used in setDimsPropertySEction
	private static TableColumnLayout createTableColumnLayout(final TableColumn numColumn) {
		final TableColumnLayout tcl = new TableColumnLayout(false);
		tcl.setColumnData(numColumn, new ColumnWeightData(100, 20));
		return tcl;
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final List<ModeTransition> modeTransitions = selectedBos.boStream(ModeTransition.class)
				.collect(Collectors.toList());
		final Set<TriggerPortElement> input;
		if (modeTransitions.size() == 1) {
			input = modeTransitions.stream().flatMap(mt -> mt.getOwnedTriggers().stream())
					.map(mtt -> new TriggerPortElement(mtt.getTriggerPort(), true)).collect(Collectors.toSet());
		} else {
			final Iterator<ModeTransition> it = modeTransitions.iterator();
			final Set<TriggerPort> inTriggerPorts = it.next().getOwnedTriggers().stream()
					.map(mtt -> mtt.getTriggerPort()).collect(Collectors.toSet());
			final Set<TriggerPort> outTriggerPorts = new HashSet<>();
			while (it.hasNext()) {
				final Set<TriggerPort> nextTriggerPorts = it.next().getOwnedTriggers().stream()
						.map(mtt -> mtt.getTriggerPort()).collect(Collectors.toSet());

				inTriggerPorts.retainAll(nextTriggerPorts);
				outTriggerPorts.addAll(nextTriggerPorts);
			}

			input = Streams.concat(inTriggerPorts.stream().map(tp -> new TriggerPortElement(tp, true)),
					outTriggerPorts.stream().filter(tp -> !inTriggerPorts.contains(tp))
					.map(tp -> new TriggerPortElement(tp, false)))
					.collect(Collectors.toSet());
		}

		tableViewer.setInput(input);
	}

	private class TriggerPortElement {
		final TriggerPort tp;
		boolean union;

		private TriggerPortElement(final TriggerPort tp, final boolean union) {
			this.tp = tp;
			this.union = union;
		}
	}
}
