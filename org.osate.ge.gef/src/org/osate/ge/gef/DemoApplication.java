package org.osate.ge.gef;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.RelativeBusinessObjectReference;

import com.google.inject.Guice;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DemoApplication extends Application {
	private IDomain domain;

	@Override
	public void start(final Stage primaryStage) throws Exception {
		// Create the domain
		this.domain = Guice.createInjector(new AgeModule()).getInstance(IDomain.class);

		final SplitPane sp = new SplitPane();

		// TODO: Share with AgeDiagramEditor
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

		final Parent canvas = getContentViewer().getCanvas();

		sp.getItems().addAll(canvas, palettePane);

		// Create viewer
		primaryStage.setScene(new Scene(sp));

		// Setup the stage
		primaryStage.setResizable(true);
		primaryStage.setWidth(640);
		primaryStage.setHeight(480);
		primaryStage.setTitle("Test Application");
		// primaryStage.sizeToScene();

		primaryStage.show();

		// TODO: Is there an event this can be done in?
		System.err.println(sp.getWidth());
		// TODO: Appropriate arg for prefWidth?
		// TODO: Padding? For divider. Somethign besides hard coding?
		final double initialDividerPosition = Math.max(0.0, 1.0 - (palette.prefWidth(100) + 5) / sp.getWidth());
		sp.setDividerPositions(initialDividerPosition);
		sp.layout();
		SplitPane.setResizableWithParent(palettePane, false);

		// Active the domain
		domain.activate();

		// Build model
		final AgeDiagram diagram = new AgeDiagram(1);
		diagram.modify("Initial", m -> {
			final DiagramElement newElement = new DiagramElement(diagram, null, null,
					new RelativeBusinessObjectReference("test1"));
			m.addElement(newElement);
		});

		getContentViewer().getContents().setAll(diagram.getDiagramElements());

		// TODO: Use a more substantial model.
	}

	private IViewer getContentViewer() {
		return domain.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
	}

	public static void main(final String[] args) {
		Application.launch(args);
	}
}
