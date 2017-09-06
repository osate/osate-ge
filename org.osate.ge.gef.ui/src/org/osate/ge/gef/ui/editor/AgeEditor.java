package org.osate.ge.gef.ui.editor;

import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.osate.ge.gef.AgeModule;
import org.osate.ge.gef.Palette;
import org.osate.ge.gef.ui.AgeUiModule;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.RelativeBusinessObjectReference;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

import javafx.embed.swt.FXCanvas;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// TODO: Implement selection notification..
// TODO: Rename to AgeDiagramEditor to match name of existing editor?
public class AgeEditor extends AbstractFXEditor {
	public AgeEditor() {
		super(Guice.createInjector(Modules.override(new AgeModule()).with(new AgeUiModule())));
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		super.init(site, input);

		// TODO: Create and set model
//		// TODO: Should load something from the input
		final AgeDiagram diagram = new AgeDiagram(1);
		diagram.modify("Initial", m -> {
			final DiagramElement newElement = new DiagramElement(diagram, null, null, new RelativeBusinessObjectReference("test1"));
			m.addElement(newElement);
		});

		getContentViewer().getContents().setAll(diagram.getDiagramElements());
	}

	// Provide access to the SWT widget. This should only be used for testing.
	@Override
	public FXCanvas getCanvas() {
		return super.getCanvas();
	}

	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);

		// Create SWT Menu for testing purposes. In reality it would be registered to accept contributions
		final FXCanvas canvas = getCanvas();
		final Menu testMenu = new Menu(canvas);
		canvas.setMenu(testMenu);

		final MenuItem miTest = new MenuItem(testMenu, SWT.NONE);
		miTest.setText("Test Menu Item");
		miTest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final SelectionModel selectionModel = getContentViewer().getAdapter(SelectionModel.class);
				final String selectionDescription = selectionModel.getSelectionUnmodifiable().stream()
						.map(cp -> cp.getContent())
						.filter(c -> c instanceof DiagramElement).map(de -> de.toString())
						.collect(Collectors.joining("\n"));
				MessageDialog.openInformation(canvas.getShell(), "Test",
						"Test Menu Item was selected.\nEditor Selection:\n" + selectionDescription);
			}
		});
	}

	@Override
	protected void hookViewers() {
		// TODO: Should the creation of the widgets be in createPartControl() instead?
		// TODO: Cleanup? Move?
		final SplitPane sp = new SplitPane();

		// TODO: Share with DemoApplication
		// TODO: Should this be part of palette. Then there would need to be a way to handle collapsing.
		final VBox palettePane = new VBox();
		final HBox palettePaneHeader = new HBox();
		final Label paletteHeaderLbl = new Label("Palette");
		final Button toggleBtn = new Button("T"); // TODO: Rename
		palettePaneHeader.getChildren().addAll(paletteHeaderLbl, toggleBtn); // TODO: Layout
		paletteHeaderLbl.managedProperty().bind(paletteHeaderLbl.visibleProperty());

		final Palette palette = new Palette();
		toggleBtn.setOnAction(new EventHandler<ActionEvent>() {
			double[] dividerPositions;

			@Override
			public void handle(ActionEvent event) {
				final boolean isVisible = !palette.isVisible();
				palette.setVisible(isVisible);
				paletteHeaderLbl.setVisible(isVisible);
				if (isVisible) {
					sp.setDividerPositions(dividerPositions);
				} else {
					dividerPositions = sp.getDividerPositions();
					sp.setDividerPositions(1.0);
				}
			}
		});
		palettePane.getChildren().addAll(palettePaneHeader, palette);

		sp.getItems().addAll(getContentViewer().getCanvas(), palettePane);

		// Set the scene
		getCanvas().setScene(new Scene(sp));

		// TODO: Appropriate arg for prefWidth?
		// TODO: Padding? For divider. Somethign besides hard coding?
		final double initialDividerPosition = Math.max(0.0, 1.0 - (palette.prefWidth(100) + 5) / sp.getWidth());
		sp.setDividerPositions(initialDividerPosition);
		sp.layout();
		SplitPane.setResizableWithParent(palettePane, false);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
}
