package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.DataSource;
import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.Graph;
import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.HorizontalSegment;
import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.Rectangle;
import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.Segments;
import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.VerticalSegment;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		final DataSource<TestObject> testDataSource = new DataSource<TestObject>() {
			private List<TestObject> rects = Arrays.asList(new TestObject(new Point(100, 100)),
					new TestObject(new Point(100, 300)), new TestObject(new Point(100, 500)),
					new TestObject(new Point(300, 175)),
					new TestObject(new Point(250, 35)));

			// private List<TestObject> rects = Arrays.asList(new TestObject(new Point(100, 100)));

			@Override
			public List<TestObject> getChildren(final TestObject o) {
				if (o == null) {
					return rects;
				} else {
					return Collections.emptyList();
				}
			}

			@Override
			public int getNumberOfConnectionPoints(final TestObject o) {
				return o.connectionPoints.length;
			}

			@Override
			public Point getConnectionPoint(final TestObject o, final int index) {
				if (index >= o.connectionPoints.length) {
					throw new IllegalArgumentException("Index out of range: " + index);
				}

				return o.connectionPoints[index];
			}

			@Override
			public Rectangle getBounds(TestObject o) {
				return o.bounds;
			}
		};

		// OrthogonalVisibilityGraph.create(testDataSource);
		final Segments segments = OrthogonalVisibilityGraphBuilder.buildSegments(testDataSource);
		final Graph graph = OrthogonalVisibilityGraphBuilder.buildGraph(segments);

		// TODO: Cleanup
		// Print the segments to the console
		for (final VerticalSegment vs : segments.verticalSegments) {
			// System.out.println(vs);
		}

		for (final HorizontalSegment hs : segments.horizontalSegments) {
			// System.out.println(hs);
		}

		// TODO: Size based on graph bounds

		primaryStage.setTitle("Connection Layout Test");
		final Group root = new Group();
		final Canvas canvas = new Canvas(600, 700);
		final GraphicsContext gc = canvas.getGraphicsContext2D();
		draw(gc, testDataSource, graph, segments);
		root.getChildren().add(canvas);
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	private <T> void draw(final GraphicsContext gc, final DataSource<T> ds, final Graph graph,
			final Segments segments) {
		// TODO: Handle hierarchy
		for (final T obj : ds.getChildren(null)) {
			final Rectangle bounds = ds.getBounds(obj);
			gc.setStroke(Color.BLACK);
			gc.setLineWidth(4);
			gc.strokeRect(bounds.min.x, bounds.min.y, bounds.max.x - bounds.min.x, bounds.max.y - bounds.min.y);
		}

		// Draw Segments
		gc.setStroke(Color.GREY);
		gc.setLineWidth(1);
		gc.setLineDashes(1.0, 5.0);
		for (final OrthogonalVisibilityGraphBuilder.HorizontalSegment hs : segments.horizontalSegments) {
			gc.strokeLine(Math.max(0, hs.minX), hs.y, Math.min(1000, hs.maxX), hs.y);
		}

		for (final OrthogonalVisibilityGraphBuilder.VerticalSegment vs : segments.verticalSegments) {
			gc.strokeLine(vs.x, Math.max(0, vs.minY), vs.x, Math.min(1000, vs.maxY));
		}

		// TODO: Move make static
		final double nodeIndicatorSize = 10;
		final double halfNodeIndicatorSize = nodeIndicatorSize / 2.0;

		// Draw Nodes
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

	private static class TestObject {
		private static final double width = 100;
		private static final double height = 100;

		private final Rectangle bounds;
		public final Point connectionPoints[];

		public TestObject(final Point topLeft) {
			bounds = new Rectangle(topLeft, new Point(topLeft.x + width, topLeft.y + height));
			// TODO: Define connection points
			connectionPoints = new Point[] {
					/*
					 * new Point(topLeft.x + width / 2, topLeft.y)
					 * , new Point(topLeft.x + width / 2, topLeft.y + height),
					 * new Point(topLeft.x, topLeft.y + height / 2),
					 * new Point(topLeft.x + width, topLeft.y + height / 2)
					 */
			};
		}
	}
}
