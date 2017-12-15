package org.osate.ge.internal.ui.properties;

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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.ArrayDimension;
import org.osate.aadl2.ArraySize;
import org.osate.aadl2.ArrayableElement;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.ui.dialogs.EditDimensionsDialog;
import org.osate.ge.internal.ui.util.SelectionUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

// TODO handle mult selection??? should it show current?
public class SetDimensionsPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBocCompatible(toTest, boc -> {
				if (boc.getBusinessObject() instanceof ArrayableElement) {
					final ArrayableElement ae = (ArrayableElement) boc.getBusinessObject();
					return ae.getContainingClassifier() == boc.getParent().getBusinessObject();
				}
				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private TableViewer tableViewer;
	private Button chooseBtn;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
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
				SWT.NONE, new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				// final TriggerPortElement tte = (TriggerPortElement) cell.getElement();
				// final String portName = tte.union ? tte.tp.getName() : "<" + tte.tp.getName() + ">";
				// cell.setText(portName);
			}
		});

		portCol.getColumn().setResizable(false);
		tableComposite.setLayout(createTableColumnLayout(portCol.getColumn()));
		chooseBtn = PropertySectionUtil.createButton(getWidgetFactory(), composite, null,
				setDimensionsSelectionListener, "Modify...", SWT.PUSH);

		fd = new FormData();
		fd.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		fd.top = new FormAttachment(composite, ITabbedPropertyConstants.VSPACE);
		chooseBtn.setLayoutData(fd);

		PropertySectionUtil.createSectionLabel(composite, chooseBtn, getWidgetFactory(), "Dimensions:");
	}

	private final SelectionAdapter setDimensionsSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			selectedBos.modify(ArrayableElement.class, ae -> {
				final EditDimensionsDialog dlg = new EditDimensionsDialog(Display.getCurrent().getActiveShell(),
						SelectionUtil.getProject(ae.eResource()), ae.getArrayDimensions(), ae instanceof Subcomponent);
				// Prompt the user for the element
				if (dlg.open() != Window.CANCEL) {
					// Replace the element's array dimensions in a round about way.
					// For some reason, if the dimension is a dimension property and it is replaced with a copy, the aadl source is updated properly but the
					// reference to the property is broken.
					for (int dimIndex = 0; dimIndex < dlg.getDimensions().size(); dimIndex++) {
						final ArrayDimension newDimension = dlg.getDimensions().get(dimIndex);
						if (ae.getArrayDimensions().size() > dimIndex) {
							// See if it changed
							final ArrayDimension oldDimension = ae.getArrayDimensions().get(dimIndex);
							final ArraySize oldSize = oldDimension.getSize();
							final ArraySize newSize = newDimension.getSize();
							boolean equals = false;
							if (oldSize == null && newSize == null) {
								equals = true;
							} else if (oldSize != null && newSize != null) {
								// Possibly equals
								if (oldSize.getSizeProperty() == null && newSize.getSizeProperty() == null) {
									if (oldSize.getSize() == newSize.getSize()) {
										equals = true;
									}
								} else if (oldSize.getSizeProperty() instanceof NamedElement
										&& newSize.getSizeProperty() instanceof NamedElement) {
									final NamedElement oldSizeProperty = (NamedElement) oldSize.getSizeProperty();
									final NamedElement newSizeProperty = (NamedElement) newSize.getSizeProperty();
									if (newSizeProperty.getQualifiedName() != null && newSizeProperty.getQualifiedName()
											.equalsIgnoreCase(oldSizeProperty.getQualifiedName())) {
										equals = true;
									}
								}
							}

							if (!equals) {
								ae.getArrayDimensions().set(dimIndex, newDimension);
							}
						} else {
							// Add the array dimension
							ae.getArrayDimensions().add(newDimension);
						}
					}
				}
			});
		}
	};

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
}
