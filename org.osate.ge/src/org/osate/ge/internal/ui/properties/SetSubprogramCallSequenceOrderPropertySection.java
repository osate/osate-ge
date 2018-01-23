package org.osate.ge.internal.ui.properties;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramCallSequence;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.ui.properties.PropertySectionUtil;
import org.osate.ge.ui.properties.PropertySectionUtil.DragAndDropElement;
import org.osate.ge.ui.properties.PropertySectionUtil.DragAndDropSupport;
import org.osate.ge.ui.properties.PropertySectionUtil.ExecuteOrderChange;
import org.osate.ge.ui.properties.PropertySectionUtil.UpDownButtonSelectionAdapter;

public class SetSubprogramCallSequenceOrderPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				return bo instanceof SubprogramCallSequence;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private TableViewer tableViewer;
	private Button upBtn;
	private Button downBtn;
	private int selectedIndex = 0; // Default table index and user selected index

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		FormData fd;
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		final Composite tableComposite = getWidgetFactory().createComposite(composite);
		fd = new FormData();
		fd.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		fd.height = 150;
		fd.width = 325;
		tableComposite.setLayoutData(fd);

		tableViewer = createTableViewer(tableComposite);

		// Drag and drop support for changing call sequence
		final DragAndDropSupport dNDSupport = new DragAndDropSupport(tableViewer.getTable(), executeChangeOrder);
		final int operations = dNDSupport.getDragAndDropOperations();
		final Transfer[] types = dNDSupport.getTransferTypes();
		tableViewer.addDropSupport(operations, types, dNDSupport.dropTargetListener);
		tableViewer.addDragSupport(operations, types, dNDSupport.dragSourceListener);

		final TableViewerColumn orderColumn = createTableViewerColumn("Order", new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final DragAndDropElement element = (DragAndDropElement) cell.getElement();
				cell.setText(Integer.toString(element.getIndex()));
			}
		});

// Editing support for changing call sequence
		orderColumn.setEditingSupport(new OptionEditingSupport(orderColumn.getViewer()));

		final TableViewerColumn subprogramCallColumn = createTableViewerColumn("Subprogram Call",
				new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final DragAndDropElement element = (DragAndDropElement) cell.getElement();
				cell.setText(element.getName());
			}
		});

		tableComposite.setLayout(createTableColumnLayout(orderColumn.getColumn(), subprogramCallColumn.getColumn()));

		final Composite btnComposite = getWidgetFactory().createFlatFormComposite(composite);
		fd = new FormData();
		fd.left = new FormAttachment(tableComposite, ITabbedPropertyConstants.HSPACE);
		fd.top = new FormAttachment(tableComposite, 0, SWT.CENTER);
		btnComposite.setLayoutData(fd);

		final int btnWidth = 40;
		final UpDownButtonSelectionAdapter moveBtnSelectionListener = new UpDownButtonSelectionAdapter(tableViewer,
				executeChangeOrder);

		upBtn = PropertySectionUtil.createButton(getWidgetFactory(), btnComposite, true, moveBtnSelectionListener, "Up",
				SWT.PUSH);
		fd = new FormData();
		fd.width = btnWidth;
		upBtn.setLayoutData(fd);

		downBtn = PropertySectionUtil.createButton(getWidgetFactory(), btnComposite, false, moveBtnSelectionListener,
				"Down", SWT.PUSH);
		fd = new FormData();
		fd.width = btnWidth;
		fd.top = new FormAttachment(upBtn, ITabbedPropertyConstants.VSPACE);
		downBtn.setLayoutData(fd);

		PropertySectionUtil.createSectionLabel(composite, getWidgetFactory(), "Call Order:");
	}

	private static TableViewer createTableViewer(final Composite tableComposite) {
		final TableViewer tableViewer = new TableViewer(tableComposite,
				SWT.NO_SCROLL | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableViewer.setContentProvider(new SubprogramCallSequenceContentProvider());
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);

		return tableViewer;
	}

	private TableViewerColumn createTableViewerColumn(final String header, final CellLabelProvider cellLabelProvider) {
		return PropertySectionUtil.createTableColumnViewer(tableViewer, header, SWT.RESIZE, cellLabelProvider);
	}

	private ExecuteOrderChange<Integer, Integer, DragAndDropElement> executeChangeOrder = (newIndex, curIndex,
			dNDElement) -> {
				if (newIndex != curIndex) {
					selectedIndex = newIndex;
					selectedBos.modify(SubprogramCallSequence.class, cs -> {
						final SubprogramCall sc = cs.getOwnedSubprogramCalls().get(dNDElement.getIndex() - 1);
						cs.getOwnedSubprogramCalls().move(newIndex, sc);
					});
				}
			};

			private static TableColumnLayout createTableColumnLayout(final TableColumn orderColumn,
					final TableColumn subprogramCallColumn) {
				final TableColumnLayout tcl = new TableColumnLayout();
				tcl.setColumnData(orderColumn, new ColumnWeightData(15, 30));
				tcl.setColumnData(subprogramCallColumn, new ColumnWeightData(85, 50));
				return tcl;
			}

			private static class SubprogramCallSequenceContentProvider implements IStructuredContentProvider {
				@Override
				public Object[] getElements(final Object inputElement) {
					@SuppressWarnings("unchecked")
					final EList<SubprogramCall> subprogramCalls = (EList<SubprogramCall>) inputElement;
					final int[] mutableIndex = { 0 };
					return subprogramCalls.stream().map(sc -> {
						mutableIndex[0] = ++mutableIndex[0];
						return new DragAndDropElement(sc.getName(), mutableIndex[0]);
					}).toArray();
				}
			}

// Editing for order column
			private class OptionEditingSupport extends EditingSupport {
				private TextCellEditor textCellEditor;

				public OptionEditingSupport(final ColumnViewer viewer) {
					super(viewer);
					textCellEditor = new TextCellEditor((Composite) viewer.getControl());
					textCellEditor.setValidator(value -> {
						try {
							Integer.parseInt((String) value);
						} catch (final NumberFormatException e) {
							return "Not a valid integer value";
						}
						return null;
					});
				}

				@Override
				protected CellEditor getCellEditor(final Object element) {
					return textCellEditor;
				}

				@Override
				protected boolean canEdit(final Object element) {
					return true;
				}

				@Override
				protected Object getValue(final Object element) {
					final DragAndDropElement dNDElement = (DragAndDropElement) element;
					return Integer.toString(dNDElement.getIndex());
				}

				@Override
				protected void setValue(final Object element, final Object value) {
					final DragAndDropElement dNDElement = (DragAndDropElement) element;
					final int newIndex = getNewIndex(tableViewer.getTable().getItemCount(), Integer.parseInt((String) value));
					executeChangeOrder.apply(newIndex, dNDElement.getIndex() - 1, dNDElement);
				}
			}

			private static int getNewIndex(final int totalSize, final int newIndex) {
				if (newIndex < 1) {
					return 0;
				}

				return (newIndex > totalSize ? totalSize : newIndex) - 1;
			}

			@Override
			public void setInput(final IWorkbenchPart part, final ISelection selection) {
				super.setInput(part, selection);
				selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
			}

			@Override
			public void refresh() {
				final List<SubprogramCallSequence> subprogramCallSeqs = selectedBos.boStream(SubprogramCallSequence.class)
						.collect(Collectors.toList());
				// Do not allow editing when multiple call sequences are selected
				setControlsEnabled(subprogramCallSeqs.size() == 1);
				final EList<SubprogramCall> subprogramCalls = subprogramCallSeqs.get(0).getOwnedSubprogramCalls();
				tableViewer.setInput(subprogramCalls);
// Default table selection and keep selection after modification
				tableViewer.getTable().setSelection(selectedIndex);
			}

			private void setControlsEnabled(final boolean isEnabled) {
				tableViewer.getTable().setEnabled(isEnabled);
				upBtn.setEnabled(isEnabled);
				downBtn.setEnabled(isEnabled);
			}
}
