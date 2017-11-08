package org.osate.ge.internal.diagram.runtime.layout.connections;

import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.Graph;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestApp extends Application {
	private static final double nodeIndicatorSize = 10;
	private static final double halfNodeIndicatorSize = nodeIndicatorSize / 2.0;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		startWithDataSource(primaryStage, TestModel.createDataSource());
	}

	private <T> void startWithDataSource(final Stage primaryStage, final LineSegmentFinderDataSource<T> ds) {
		// OrthogonalVisibilityGraph.create(testDataSource);
		final OrthogonalSegments<T> segments = LineSegmentFinder.buildSegments(ds);
		final Graph graph = OrthogonalVisibilityGraphBuilder.buildGraph(segments);

		// TODO: Cleanup
		// Print the segments to the console
		for (final VerticalSegment<T> vs : segments.verticalSegments) {
			// System.out.println(vs);
		}

		for (final HorizontalSegment<T> hs : segments.horizontalSegments) {
			// System.out.println(hs);
		}

		primaryStage.setTitle("Connection Layout Test");
		final Group root = new Group();
		final Canvas canvas = new Canvas(800, 500);
		final GraphicsContext gc = canvas.getGraphicsContext2D();
		draw(gc, ds, graph, segments);
		root.getChildren().add(canvas);
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	private <T> void draw(final GraphicsContext gc, final LineSegmentFinderDataSource<T> ds, final Graph graph,
			final OrthogonalSegments<T> segments) {
		// Draw the objects
		for (final T obj : ds.getObjects()) {
			drawObjectIfBounded(gc, ds, obj);
		}

		// Draw Segments
		gc.setStroke(Color.GREY);
		gc.setLineWidth(1);
		gc.setLineDashes(1.0, 5.0);
		for (final HorizontalSegment<T> hs : segments.horizontalSegments) {
			gc.strokeLine(Math.max(0, hs.minX), hs.y, Math.min(1000, hs.maxX), hs.y);
		}

		for (final VerticalSegment<T> vs : segments.verticalSegments) {
			gc.strokeLine(vs.x, Math.max(0, vs.minY), vs.x, Math.min(1000, vs.maxY));
		}

		// Draw Nodes
		final boolean drawNodesAndEdges = true;// false; // TODO: Reenable
		if (drawNodesAndEdges) {
			gc.setFill(Color.BLUE);
			System.err.println(graph.nodes.size());
			for (final OrthogonalGraphNode n : graph.nodes) {
				gc.fillOval(n.position.x - halfNodeIndicatorSize, n.position.y - halfNodeIndicatorSize, nodeIndicatorSize,
						nodeIndicatorSize);
			}

			// Draw Edge
			gc.setLineDashes(0.0);
			for (final OrthogonalGraphNode n : graph.nodes) {
				gc.setStroke(Color.RED);
				final OrthogonalGraphNode right = n.getNeighbor(OrthogonalDirection.RIGHT);
				if (right != null) {
					gc.strokeLine(n.position.x + halfNodeIndicatorSize, n.position.y,
							right.position.x - halfNodeIndicatorSize, right.position.y);
				}

				gc.setStroke(Color.GREEN);
				final OrthogonalGraphNode down = n.getNeighbor(OrthogonalDirection.DOWN);
				if (down != null) {
					gc.strokeLine(n.position.x, n.position.y + halfNodeIndicatorSize, down.position.x,
							down.position.y - halfNodeIndicatorSize);
				}
			}
		}
	}

	private <T> void drawObjectIfBounded(final GraphicsContext gc, final LineSegmentFinderDataSource<T> ds,
			final T obj) {
		final Rectangle bounds = ds.getBounds(obj);
		if (bounds != null) {
			gc.setStroke(Color.BLACK);
			gc.setLineWidth(4);
			gc.strokeRect(bounds.min.x, bounds.min.y, bounds.max.x - bounds.min.x, bounds.max.y - bounds.min.y);
		}
	}

}
