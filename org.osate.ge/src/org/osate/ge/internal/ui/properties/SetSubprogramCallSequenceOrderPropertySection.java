package org.osate.ge.internal.ui.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramCallSequence;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class SetSubprogramCallSequenceOrderPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				return bo instanceof SubprogramCallSequence;
			});
		}
	}
	// TODO add move up down buttons to call sequence and change index to order
	//TODO make sure multi selection doesnt work

	private BusinessObjectSelection selectedBos;
	private TableViewer tableViewer;
	private List<SubprogramCallElement> elements;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		final Composite tableComposite = getWidgetFactory().createComposite(composite);
		final FormData fd = new FormData();
		fd.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		fd.height = 150;
		fd.width = 325;
		tableComposite.setLayoutData(fd);

		tableViewer = new TableViewer(tableComposite,
				SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableViewer.setContentProvider(new SubprogramCallSequenceContentProvider());

		// Drag and drop support for changing call sequence
		final DragAndDropSupport dNDSupport = new DragAndDropSupport();
		final int operations = dNDSupport.operations;
		final Transfer[] types = dNDSupport.types;
		tableViewer.addDropSupport(operations, types, dNDSupport.dropTargetListener);
		tableViewer.addDragSupport(operations, types, dNDSupport.dragSourceListener);

		final TableViewerColumn numColumn = PropertySectionUtil.createTableColumnViewer(tableViewer, "Index",
				SWT.RESIZE, new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final SubprogramCallElement element = (SubprogramCallElement) cell.getElement();
				cell.setText(Integer.toString(element.getIndex() + 1));
			}
		});

		// Editing support for changing call sequence
		numColumn.setEditingSupport(new OptionEditingSupport(numColumn.getViewer()));

		final TableViewerColumn subprogramCallColumn = PropertySectionUtil.createTableColumnViewer(tableViewer,
				"Subprogram Call",
				SWT.RESIZE,
				new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final SubprogramCallElement element = (SubprogramCallElement) cell.getElement();
				cell.setText(element.getSubprogramCall().getName());
			}
		});

		tableComposite.setLayout(createTableColumnLayout(numColumn.getColumn(), subprogramCallColumn.getColumn()));
		PropertySectionUtil.createSectionLabel(composite, tableViewer.getControl(), getWidgetFactory(), "Call Order:");
	}

	private static TableColumnLayout createTableColumnLayout(final TableColumn numColumn,
			final TableColumn subprogramCallColumn) {
		final TableColumnLayout tcl = new TableColumnLayout(true);
		// TODO is false on trigger table??
		tcl.setColumnData(numColumn, new ColumnWeightData(12, 20));
		tcl.setColumnData(subprogramCallColumn, new ColumnWeightData(75, 50));
		return tcl;
	}

	private class SubprogramCallSequenceContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(final Object inputElement) {
			final SubprogramCallSequence sCSequence = (SubprogramCallSequence) inputElement;
			elements = new ArrayList<>();
			int i = 0;
			for (final SubprogramCall sc : sCSequence.getOwnedSubprogramCalls()) {
				elements.add(new SubprogramCallElement(sc, i++));
			}

			return elements.toArray();
		}
	}

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
			final SubprogramCallElement sCElement = (SubprogramCallElement) element;
			return Integer.toString(sCElement.getIndex() + 1);
		}

		@Override
		protected void setValue(final Object element, final Object value) {
			final SubprogramCallElement sCElement = (SubprogramCallElement) element;
			final int newIndex = getNewIndex(elements.size(), Integer.parseInt((String) value) - 1);

			// Do not update if index has not changed
			if (newIndex != sCElement.getIndex()) {
				selectedBos.modify(SubprogramCallSequence.class, cs -> {
					cs.getOwnedSubprogramCalls().move(newIndex,
							sCElement.getSubprogramCall());
				});
			}
		}
	}

	private static int getNewIndex(final int totalSize, final int newIndex) {
		if (newIndex > totalSize) {
			return totalSize - 1;
		} else if (newIndex < 0) {
			return 0;
		}

		return newIndex;
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
		tableViewer.getTable().setEnabled(subprogramCallSeqs.size() == 1);
		tableViewer.setInput(subprogramCallSeqs.get(0));
	}

	private class DragAndDropSupport {
		private final int operations = DND.DROP_MOVE;
		private final Transfer[] types = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		private final Table table;
		private SubprogramCallElement dragElement; // Element moving indices
		private SubprogramCallElement targetElement; // Element targeted

		private DragAndDropSupport() {
			table = tableViewer.getTable();
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
		}

		private DragSourceAdapter dragSourceListener = new DragSourceAdapter() {
			@Override
			public void dragStart(final DragSourceEvent event) {
				dragElement = elements.get(table.getSelectionIndex());
			}
		};

		// Drag element will be placed above targeted element
		private DropTargetAdapter dropTargetListener = new DropTargetAdapter() {
			@Override
			public void drop(final DropTargetEvent event) {
				final SubprogramCall dragCall = dragElement.getSubprogramCall();
				final int dragIndex = dragElement.getIndex();
				final int newIndex = getNewIndex(targetElement, dragIndex);

				if (newIndex != dragElement.getIndex()) {
					selectedBos.modify(SubprogramCallSequence.class, cs -> {
						cs.getOwnedSubprogramCalls().move(newIndex, dragCall);
					});
				}

				dragElement = null;
				targetElement = null;
			}

			private int getNewIndex(final SubprogramCallElement targetElement, final int dragIndex) {
				// Set in last index
				if (targetElement == null) {
					return elements.size() - 1;
				}

				final int targetIndex = targetElement.getIndex();
				// If dragging up, subtract 1
				return dragIndex < targetIndex ? targetIndex - 1 : targetIndex;
			}

			@Override
			public void dragOver(final DropTargetEvent event) {
				final TableItem tableItem;
				final SubprogramCallElement dragTargetItem;
				if (event.item instanceof TableItem) {
					tableItem = (TableItem) event.item;
					dragTargetItem = (SubprogramCallElement) tableItem.getData();
				} else {
					tableItem = null;
					dragTargetItem = null;
				}

				if (dragTargetItem != targetElement) {
					if (dragTargetItem != null) {
						// Scroll while dragging
						if (dragTargetItem.getIndex() > 0) {
							table.showItem(table.getItem(dragTargetItem.getIndex() - 1));
						}

						if (dragTargetItem.getIndex() + 1 < table.getItemCount()) {
							table.showItem(table.getItem(dragTargetItem.getIndex() + 1));
						}
					}

					// Cleans up lines drawn for previous targeted index
					table.redraw();
				}

				// Set next target element
				targetElement = dragTargetItem;

				// Draw the drop index line
				final GC gc = new GC(table);
				gc.setLineWidth(3);
				final Rectangle bounds = getBounds(tableItem);
				gc.drawLine(bounds.x, bounds.height, bounds.width, bounds.height);
				gc.dispose();
			}

			// Bounds used for drawing target index line
			private Rectangle getBounds(final TableItem targetItem) {
				final Rectangle bounds;
				final int y;

				// Draw below last item for last index
				if (targetItem == null) {
					bounds = table.getItem(table.getItemCount() - 1).getBounds();
					y = bounds.y + bounds.height;
				} else {
					bounds = targetItem.getBounds();
					y = bounds.y + 1;
				}

				return new Rectangle(bounds.x - 5, y, bounds.x + table.getBounds().width, y);
			}

			@Override
			public void dragLeave(final DropTargetEvent event) {
				// Cleans up any leftover drawing
				table.redraw();
			}
		};
	}

	private class SubprogramCallElement {
		private final SubprogramCall subprogramCall;
		private final int index;

		private SubprogramCallElement(final SubprogramCall subprogramCall, final int index) {
			this.subprogramCall = subprogramCall;
			this.index = index;
		}

		private SubprogramCall getSubprogramCall() {
			return subprogramCall;
		}

		private int getIndex() {
			return index;
		}
	}
}
