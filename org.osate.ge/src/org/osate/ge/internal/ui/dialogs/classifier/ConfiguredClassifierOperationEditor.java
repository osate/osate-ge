package org.osate.ge.internal.ui.dialogs.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osate.ge.internal.ui.dialogs.ElementLabelProvider;
import org.osate.ge.internal.ui.dialogs.ElementSelectionDialog;
import org.osate.ge.internal.util.StringUtil;

import com.google.common.base.CaseFormat;

// TODO: Rename
class ConfiguredClassifierOperationEditor extends Composite {
	private ElementLabelProvider elementLabelProvider = new ElementLabelProvider();
	private final CopyOnWriteArrayList<SelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
	private final Group operationGroup;
	private final Label existingLabel;
	private final Composite existingValueContainer;
	private final Label existingValueLabel;
	private final Label packageLabel;
	private final Composite packageValueContainer;
	private final Label selectedPackageLabel;
	private final Label nameLabel;
	private final Text nameField;
	private final List<Button> operationBtns = new ArrayList<>();
	private final boolean showPackageSelector;
	Value currentValue = new Value();

	static interface InnerWidgetModel {
		Collection<?> getPackageOptions();

		Collection<?> getSelectOptions();

		Collection<?> getUnfilteredSelectOptions();

		String getSelectTitle();

		String getSelectMessage();
	}

	// TODO: Rename?
	private class Value {
		ClassifierOperation operation;
		private Object selectedPackage;
		private Object selectedElement;

		public String getName() {
			return nameField.getText();
		}

		public ConfiguredClassifierOperation toConfiguredOperation() {
			return new ConfiguredClassifierOperation(operation, selectedPackage, getName(), selectedElement);
		}
	}

	public ConfiguredClassifierOperationEditor(final Composite parent, final EnumSet<ClassifierOperation> allowedOperations,
			final boolean showPackageSelector,
			final ConfiguredClassifierOperationEditor.InnerWidgetModel widgetModel) {
		super(parent, SWT.NONE);
		this.showPackageSelector = showPackageSelector;
		setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		//
		// Operations
		//
		operationGroup = new Group(this, SWT.NONE);
		operationGroup.setLayout(RowLayoutFactory.fillDefaults().type(SWT.HORIZONTAL).wrap(true).create());
		operationGroup.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false).create());

		final SelectionListener operationSelectedListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button btn = ((Button) e.widget);
				if (btn.getSelection()) {
					currentValue.operation = (ClassifierOperation) btn.getData();
					notifySelectionListeners();
					updateOperationDetailsVisibility();
				}
			}
		};

		for (final ClassifierOperation operation : ClassifierOperation.values()) {
			final Button newBtn = new Button(operationGroup, SWT.RADIO);
			newBtn.setText(StringUtil
					.camelCaseToUser(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, operation.toString())));
			newBtn.setData(operation);
			newBtn.addSelectionListener(operationSelectedListener);
			operationBtns.add(newBtn);
		}

		setAllowedOperations(allowedOperations);

		//
		// Select Existing
		//
		// TODO: Need way to select an existing element.
		// TODO: Only show when existing is selecting
		existingLabel = new Label(this, SWT.NONE);
		existingLabel.setText("Existing:"); // TODO: Configurable?
		existingLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).create());

		final RowLayout existingValueContainerLayout = new RowLayout(SWT.HORIZONTAL);
		existingValueContainerLayout.center = true;
		existingValueContainer = new Composite(this, SWT.NONE);
		existingValueContainer.setLayout(existingValueContainerLayout);
		existingValueContainer.setLayoutData(GridDataFactory.fillDefaults().create());

		existingValueLabel = new Label(existingValueContainer, SWT.NONE);
		existingValueLabel.setText(CreateSelectClassifierDialog.NOT_SELECTED_LABEL);

		final Button selectExistingBtn = new Button(existingValueContainer, SWT.NONE);
		selectExistingBtn.setText("...");

		selectExistingBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ElementSelectionDialog dlg = new ElementSelectionDialog(Display.getCurrent().getActiveShell(),
						widgetModel.getSelectTitle(), widgetModel.getSelectMessage(),
						widgetModel.getSelectOptions(), "Show All", widgetModel.getUnfilteredSelectOptions(),
						false);
				if (dlg.open() == Window.OK) {
					setSelectedElement(dlg.getFirstSelectedElement());
					notifySelectionListeners();
				}
			}
		});

		//
		// Create
		//

		// TODO: Only show when new is selecting
		packageLabel = new Label(this, SWT.NONE);
		packageLabel.setText("Package:");
		packageLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).create());

		final RowLayout packageValueContainerLayout = new RowLayout(SWT.HORIZONTAL);
		packageValueContainerLayout.center = true;
		packageValueContainer = new Composite(this, SWT.NONE);
		packageValueContainer.setLayout(packageValueContainerLayout);
		packageValueContainer
		.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).create());

		selectedPackageLabel = new Label(packageValueContainer, SWT.NONE);
		selectedPackageLabel.setText(CreateSelectClassifierDialog.NOT_SELECTED_LABEL);

		final Button selectPackageBtn = new Button(packageValueContainer, SWT.NONE);
		selectPackageBtn.setText("...");

		selectPackageBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ElementSelectionDialog dlg = new ElementSelectionDialog(Display.getCurrent().getActiveShell(),
						"Select a Package", "Select a package.", widgetModel.getPackageOptions(), false);
				if (dlg.open() == Window.OK) {
					setSelectedPackage(dlg.getFirstSelectedElement());
					notifySelectionListeners();
				}
			}
		});

		nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Identifier:");
		nameField = new Text(this, SWT.SINGLE | SWT.BORDER);
		nameField.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		nameField.addModifyListener(e -> notifySelectionListeners());

		// Update widget visibility
		updateOperationDetailsVisibility();
	}

	public void setAllowedOperations(final EnumSet<ClassifierOperation> allowedOperations) {
		if (allowedOperations == null || allowedOperations.size() == 0) {
			throw new RuntimeException("allowedOperations must contain at least one operation.");
		}

		setGridChildVisible(operationGroup, allowedOperations.size() > 1);

		if (allowedOperations.size() > 1) {
			// TODO: Need to have helper?
			for (final Button operationBtn : operationBtns) {
				// TODO: Need to exclude?
				final boolean visible = allowedOperations.contains(operationBtn.getData());
				operationBtn.setVisible(visible);
				operationBtn.setLayoutData(RowDataFactory.swtDefaults().exclude(!visible).create());
				operationBtn.setSelection(operationBtn.getData() == currentValue.operation);
			}

			operationGroup.requestLayout();
		} else {
			currentValue.operation = allowedOperations.iterator().next();
		}

		updateOperationDetailsVisibility();

		// Request Layout
		requestLayout();
	}

	/**
	 * Selection listeners are called when the value of the widget changes.
	 * @param listener
	 */
	public void addSelectionListener(final SelectionListener listener) {
		selectionListeners.add(listener);
	}

	void setSelectedElement(final Object element) {
		currentValue.selectedElement = element;
		existingValueLabel.setText(getElementDescription(element));
		existingValueContainer.requestLayout();
	}

	void setSelectedPackage(final Object pkg) {
		currentValue.selectedPackage = pkg;
		selectedPackageLabel.setText(getElementDescription(pkg));
		packageValueContainer.requestLayout();
	}

	private String getElementDescription(final Object value) {
		if (value == null) {
			return CreateSelectClassifierDialog.NOT_SELECTED_LABEL;
		}

		final String desc = elementLabelProvider.getText(value);
		return desc == null ? "<null>" : desc;
	}

	private void notifySelectionListeners() {
		final Event e = new Event();
		e.widget = this;
		final SelectionEvent selectionEvent = new SelectionEvent(e);
		for (final SelectionListener l : selectionListeners) {
			l.widgetSelected(selectionEvent);
		}
	}

	private static void setGridChildVisible(final Control c, final boolean visible) {
		// Check for null to support widgets that are not created because they are not necessary for the allowed operations
		if (c != null) {
			if (c.getLayoutData() == null) {
				c.setLayoutData(GridDataFactory.fillDefaults().create());
			}

			((GridData) c.getLayoutData()).exclude = !visible;
			// c.setLayoutData(GridDataFactory.fillDefaults().exclude(!visible).create());
			c.setVisible(visible);
		}
	}

	private void updateOperationDetailsVisibility() {
		final boolean selectExisting = currentValue.operation == ClassifierOperation.EXISTING;
		setGridChildVisible(existingLabel, selectExisting);
		setGridChildVisible(existingValueContainer, selectExisting);

		final boolean createNew = ClassifierOperation.isCreate(currentValue.operation);
		setGridChildVisible(nameLabel, createNew);
		setGridChildVisible(nameField, createNew);

		final boolean showPackageWidgets = showPackageSelector && createNew;
		setGridChildVisible(packageLabel, showPackageWidgets);
		setGridChildVisible(packageValueContainer, showPackageWidgets);

		requestLayout();
	}

	public ConfiguredClassifierOperation getConfiguredOperation() {
		return currentValue.toConfiguredOperation();
	}
}