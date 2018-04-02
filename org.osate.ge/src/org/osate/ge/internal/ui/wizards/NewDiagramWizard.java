package org.osate.ge.internal.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class NewDiagramWizard extends Wizard implements INewWizard {
	// TODO: Rename
	enum ContextType {
		NEW_PACKAGE,
		CONTEXTLESS, EXISTING_PACKAGE, EXISTING_CLASSIFIER
	}

	// TODO: rename
	private static class SelectProjectPage extends WizardPage {
		private Composite container;
		private TreeViewer projectViewer;

		SelectProjectPage() {
			super("Select Project"); // TODO
			// TODO
			setTitle("Select Project");
			setDescription("Select Project");
		}

		@Override
		public void createControl(final Composite parent) {
			container = new Composite(parent, SWT.NONE);
			final GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 1;

			// TODO: labels, etc

			projectViewer = new TreeViewer(container, SWT.SINGLE | SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(projectViewer.getControl());
			projectViewer.setContentProvider(new WorkbenchContentProvider() {
				@Override
				public Object[] getChildren(Object element) {
					return Arrays.stream(super.getChildren(element)).filter(r -> r instanceof IProject)
							.map(r -> (IProject) r).filter(p -> p.isOpen()).toArray();
				}
			});
			projectViewer.setLabelProvider(new WorkbenchLabelProvider());
			projectViewer.addSelectionChangedListener(event -> update());
			projectViewer.setInput(ResourcesPlugin.getWorkspace());

			setControl(container);

			update();
		}

		private void update() {
			setPageComplete(getProject() != null);
		}

		public IProject getProject() {
			return (IProject) ((StructuredSelection) projectViewer.getSelection()).getFirstElement();
		}

	}

	/**
	 * TODO: Page for selecting the type of context to be selected.
	 *
	 *
	 */
	private static class SelectContextTypePage extends WizardPage {
		private Composite container;
		private final List<Button> selectionBtns = new ArrayList<>();
		private final SelectionListener updateOnSelect = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		};

		SelectContextTypePage() {
			super("Select Context Type"); // TODO
			// TODO
			setTitle("Select Context Type");
			setDescription("Select Context Type");
		}

		@Override
		public void createControl(final Composite parent) {
			container = new Composite(parent, SWT.NONE);
			final GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 1;

			addButton("New Package", ContextType.NEW_PACKAGE);
			addButton("Contextless", ContextType.CONTEXTLESS);
			addButton("Existing Package", ContextType.EXISTING_PACKAGE);
			addButton("Existing Classifier", ContextType.EXISTING_CLASSIFIER);

			setControl(container);

			update();
		}

		private void addButton(final String label, final ContextType contextType) {
			final Button btn = new Button(container, SWT.RADIO);
			btn.setText(label);
			btn.setData(contextType);
			btn.addSelectionListener(updateOnSelect);
			selectionBtns.add(btn);
		}

		private void update() {
			setPageComplete(getContextType() != null);

			// TODO: Update description based on selection
		}

		private ContextType getContextType() {
			for (final Button selectionBtn : selectionBtns) {
				if (selectionBtn.getSelection()) {
					return (ContextType) selectionBtn.getData();
				}
			}

			return null;
		}
	}

	/**
	 * TODO: Page for selecting the context for the diagram
	 *
	 *
	 */
	private static class SelectContextPage extends WizardPage {
		private Composite container;

		SelectContextPage() {
			super("Select Context"); // TODO
			// TODO
			setTitle("Select Context");
			setDescription("Select Context");
		}

		@Override
		public void createControl(final Composite parent) {
			container = new Composite(parent, SWT.NONE);
			final GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 2;

			setControl(container);
			setPageComplete(false);

			// TODO: remove
			setPageComplete(true);
		}
	}

	/**
	 * TODO: Page for selecting the name and type for the diagram.
	 *
	 *
	 */
	// TODO: Rename
	private static class SelectNameAndTypePage extends WizardPage {
		private Composite container;

		SelectNameAndTypePage() {
			super("Select Name and Type");
			// TODO
			setTitle("Select Name and Type");
			setDescription("Select Name and Type");
		}

		@Override
		public void createControl(final Composite parent) {
			container = new Composite(parent, SWT.NONE);
			final GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 2;

			setControl(container);
			setPageComplete(false);

			// TODO: remove
			setPageComplete(true);
		}
	}

	private SelectContextTypePage contextTypePage = new SelectContextTypePage();
	private SelectProjectPage selectProjectPage = new SelectProjectPage();
	private SelectContextPage contextPage = new SelectContextPage(); // TODO: Change type
	private SelectNameAndTypePage diagramNameAndTypePage = new SelectNameAndTypePage(); // TODO: Change type. TODO: Rename. TODO: Option to configure diagram
	// after creation?

	// Disable finish button at inappropriate times.

	public NewDiagramWizard() {
		setWindowTitle("New AADL Diagram");
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addPages() {
		addPage(contextTypePage);
		addPage(selectProjectPage);
		addPage(contextPage);
		addPage(diagramNameAndTypePage);
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage currentPage) {
		if (currentPage == contextTypePage) {
			if (contextTypePage.getContextType() == ContextType.CONTEXTLESS) {
				return diagramNameAndTypePage;
			} else if (contextTypePage.getContextType() == ContextType.NEW_PACKAGE) {
				return null;
			}
		}

		return super.getNextPage(currentPage);
	}

	@Override
	public boolean canFinish() {
		if (contextTypePage.getContextType() == ContextType.NEW_PACKAGE) {
			return true;
		}

		// TODO: Implement isPageComplete() for pages.

		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		if (contextTypePage.getContextType() == ContextType.NEW_PACKAGE) {
			// Use async exec so the current wizard will be closed before the new wizard opens.
			Display.getDefault().asyncExec(() -> {
				// TODO: Want to select the Open with option for the package wizard..
				// TODO: Look into better ways for triggering the package wizard while avoiding circular dependencies.
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
				try {
					handlerService.executeCommand("org.osate.ui.wizards.packageWizard", null);
				} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

			return true;
		}

		// TODO
		return false;
	}

	public static void main(final String[] args) {
		Display.getDefault().syncExec(() -> {
			final WizardDialog dialog = new WizardDialog(new Shell(), new NewDiagramWizard());
			dialog.open();
		});
	}

}
