package org.osate.ge.internal.ui.dialogs;

import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osate.ge.internal.diagram.runtime.types.DiagramType;

public class CreateDiagramDialog {
	public static interface Model {

	}

	public static class Result {
		// TODO: Consider renaming fields and methods
		private final IFile diagramFile;
		private final DiagramType diagramType;

		public Result(final IFile diagramFile, final DiagramType diagramType) {
			this.diagramFile = Objects.requireNonNull(diagramFile, "diagramFile must not be null");
			this.diagramType = Objects.requireNonNull(diagramType, "diagramType must not be null");
		}

		public final IFile getDiagramFile() {
			return diagramFile;
		}

		public final DiagramType getDiagramType() {
			return diagramType;
		}
	}

	// TODO: Set messsage and error message as appropriate
	private class InnerDialog extends TitleAreaDialog {
		public InnerDialog(final Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.setHelpAvailable(false);
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Create Diagram");
			newShell.setMinimumSize(250, 400);
			newShell.setSize(500, 350);
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			final Composite area = (Composite) super.createDialogArea(parent);

			final Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			container.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

			final Label nameLabel = new Label(container, SWT.NONE);
			nameLabel.setText("Name");

			final Text nameField = new Text(container, SWT.SINGLE | SWT.BORDER);
			nameField.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
			// TODO: Default value

			final Label typeLabel = new Label(container, SWT.NONE);
			typeLabel.setText("Type:");

			final ComboViewer typeField = new ComboViewer(container);
			typeField.getCombo().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

			return area;
		}
	}

	private final Model model;
	private final InnerDialog dlg;

	protected CreateDiagramDialog(final Shell parentShell, final Model model) {
		this.model = Objects.requireNonNull(model, "model must not be null");
		this.dlg = new InnerDialog(parentShell);
	}

	private Result open() {
		// TODO
		return dlg.open() == Window.OK ? new Result(null, null) : null;
	}

	/**
	 * A null return value indicates that the dialog was canceled.
	 * @param initialSelectionBoPath is an array of business objects which form a path to the node that should be selected. May be null
	 * @return
	 */
	public static Result show(final Shell parentShell, final Model model) {
		final CreateDiagramDialog dlg = new CreateDiagramDialog(parentShell, model);
		return dlg.open();
	}

	public static void main(String[] args) {
		final Model model = new Model() {

		};

		// Show the dialog
		Display.getDefault().syncExec(() -> {
			final Result result = show(null, model);
			if (result == null) {
				System.out.println("Dialog was canceled.");
			} else {
				System.out.println(result.getDiagramFile());
				System.out.println(result.getDiagramFile());
			}
		});
	}
}
