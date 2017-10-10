package org.osate.ge.fx.nodes;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Helper class intended for testing purposes.
 *
 */
public class NodeApplication extends Application {
	private static Supplier<Node[]> nodeSupplier;

	public static void run(Node ... nodesToTest) {
		run(() -> nodesToTest);
	}

	/**
	 * Runs the node application. The nodeSupplier is called in the application's start() method.
	 * @param nodeSupplier
	 */
	public static void run(final Supplier<Node[]> nodeSupplier) {
		if (NodeApplication.nodeSupplier != null) {
			throw new RuntimeException("NodeApplication.run() may only be called once.");
		}

		NodeApplication.nodeSupplier = Objects.requireNonNull(nodeSupplier, "nodeSupplier  must not be null");
		Application.launch();
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		final VBox container = new VBox();
		container.setPadding(new Insets(10.0));
		container.setSpacing(10);

		final Node[] nodesToTest = nodeSupplier.get();
		for (final Node n : nodesToTest) {
			VBox.setVgrow(n, Priority.ALWAYS);
		}

		container.getChildren().setAll(nodesToTest);

		primaryStage.setScene(new Scene(container));

		// Setup the stage
		primaryStage.setResizable(true);
		primaryStage.setWidth(640);
		primaryStage.setHeight(640);
		primaryStage.setTitle(
				Arrays.stream(nodesToTest).map(n -> n.getClass().getSimpleName()).collect(Collectors.joining(" : ")));
		primaryStage.show();
	}
}
