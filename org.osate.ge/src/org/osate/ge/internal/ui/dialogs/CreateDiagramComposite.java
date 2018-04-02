package org.osate.ge.internal.ui.dialogs;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

import com.google.common.collect.ImmutableCollection;

public class CreateDiagramComposite<DiagramType> extends Composite {
	public static interface Model<DiagramType> {
		ImmutableCollection<DiagramType> getDiagramTypes();

		String getDiagramTypeName(final DiagramType diagramType);

		IProject getProject();

		String getDefaultName();

		DiagramType getDefaultDiagramType();
	}

	public static class Value<DiagramType> {
		private final IFile file;
		private final DiagramType diagramType;

		Value(final IFile file, final DiagramType diagramType) {
			this.file = file;
			this.diagramType = diagramType;
		}

		public final IFile getFile() {
			return file;
		}

		public final DiagramType getDiagramType() {
			return diagramType;
		}

		public boolean isValid() {
			return file != null && diagramType != null;
		}
	}

	private final CopyOnWriteArrayList<SelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
	private Model<DiagramType> model;
	private Text nameField;
	private IFile file;
	private DiagramType diagramType;
	private String errorMessage;

	@SuppressWarnings("unchecked")
	public CreateDiagramComposite(final Composite parent, final Model<DiagramType> model) {
		super(parent, SWT.NONE);
		this.model = Objects.requireNonNull(model, "model must not be null");

		setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

		final Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name");

		nameField = new Text(this, SWT.SINGLE | SWT.BORDER);
		nameField.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		nameField.addModifyListener(e -> {
			updateFile();
		});

		// Determine the initial name. A suffix will be added to the default name if the file already exists
		final IProject project = model.getProject();
		final String baseName = model.getDefaultName();
		if (baseName != null) {
			IFile tmpFile;
			int nameCount = 1;
			String name;
			do {
				final String suffix = nameCount == 1 ? "" : "(" + nameCount + ")";
				name = baseName + suffix;
				tmpFile = createFile(project, name);
				nameCount++;
			} while (tmpFile.exists());

			nameField.setText(name);
		}

		//
		// Create type field
		//
		final Label typeLabel = new Label(this, SWT.NONE);
		typeLabel.setText("Type:");

		final ComboViewer typeField = new ComboViewer(this);
		typeField.getCombo().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		typeField.setComparator(new ViewerComparator());
		typeField.setContentProvider(new ArrayContentProvider());
		typeField.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return model.getDiagramTypeName((DiagramType) element);
			}
		});
		typeField.setInput(model.getDiagramTypes());
		typeField.addSelectionChangedListener(event -> {
			diagramType = (DiagramType) ((StructuredSelection) typeField.getSelection()).getFirstElement();
			validate();
			notifySelectionListeners();
		});

		diagramType = model.getDefaultDiagramType();
		if (diagramType != null) {
			typeField.setSelection(new StructuredSelection(diagramType));
		}

		// Update the initial value for the file and validate initial values
		updateFile();
		validate();
	}

	private void updateFile() {
		file = createFile(model.getProject(), nameField.getText());
		validate();
		notifySelectionListeners();
	}

	private IFile createFile(final IProject project, final String name) {
		final IFolder diagramFolder = project.getFolder("diagrams/");
		return diagramFolder.getFile(name + AgeDiagramEditor.EXTENSION);
	}

	/**
	 * Selection listeners are called when the value of the widget changes.
	 * @param listener
	 */
	public void addSelectionListener(final SelectionListener listener) {
		selectionListeners.add(listener);
	}

	private void notifySelectionListeners() {
		final Event e = new Event();
		e.widget = this;
		final SelectionEvent selectionEvent = new SelectionEvent(e);
		for (final SelectionListener l : selectionListeners) {
			l.widgetSelected(selectionEvent);
		}
	}

	private void validate() {
		// Check values and set error message
		if (file == null) {
			setErrorMessage("Enter a name.");
		} else if (file.exists()) {
			setErrorMessage("File '" + file.getProjectRelativePath().makeAbsolute().toString() + "' already exists.");
		} else if (!nameField.getText().matches("\\S[\\w\\d \\(\\)-]*")) {
			setErrorMessage("Name is not valid.");
		} else if (diagramType == null) {
			setErrorMessage("Select a diagram type.");
		} else {
			setErrorMessage(null);
		}
	}

	private void setErrorMessage(final String value) {
		this.errorMessage = value;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public Value<DiagramType> getValue() {
		return new Value<DiagramType>(file, diagramType);
	}
}
