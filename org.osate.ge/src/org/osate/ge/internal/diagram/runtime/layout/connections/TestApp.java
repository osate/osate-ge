package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.Dimension;
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
//		final List<TestObject> objects = Arrays.asList(
//				TestObject.createContainer(null, new Point(100, 100), new Dimension(100, 100)),
//				TestObject.createContainer(null, new Point(100, 300), new Dimension(100, 100)),
//				TestObject.createContainer(null, new Point(100, 500), new Dimension(100, 100)),
//				TestObject.createContainer(null, new Point(300, 175), new Dimension(100, 100)),
//				TestObject.createContainer(null, new Point(250, 35), new Dimension(100, 100)));

		final List<TestObject> objects = Arrays.asList(
				TestObject.createSimple(null, new Point(0, 0), new Dimension(200, 200)),
				TestObject.createSimple(null, new Point(400, 0), new Dimension(200, 200)));

		TestObject.createSimple(objects.get(0), new Point(50, 100), new Dimension(50, 50));
		TestObject.createSimple(objects.get(1), new Point(500, 100), new Dimension(50, 50));

		final DataSource<TestObject> testDataSource = new DataSource<TestObject>() {
			@Override
			public List<TestObject> getChildren(final TestObject o) {
				if (o == null) {
					return objects;
				} else {
					return o.children;
				}
			}

			@Override
			public int getNumberOfConnectionPoints(final TestObject o) {
				return o.connectionPoints.size();
			}

			@Override
			public Point getConnectionPoint(final TestObject o, final int index) {
				return o.connectionPoints.get(index);
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
		// Draw the objects
		drawChildren(gc, ds, null);

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

	private <T> void drawChildren(final GraphicsContext gc, final DataSource<T> ds, final T parent) {
		for (final T child : ds.getChildren(parent)) {
			drawObject(gc, ds, child);
		}
	}

	private <T> void drawObject(final GraphicsContext gc, final DataSource<T> ds, final T obj) {
		final Rectangle bounds = ds.getBounds(obj);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(4);
		gc.strokeRect(bounds.min.x, bounds.min.y, bounds.max.x - bounds.min.x, bounds.max.y - bounds.min.y);
		drawChildren(gc, ds, obj);
	}

	private static class TestObject {
		public final TestObject parent;
		private final List<TestObject> modifiableChildren = new ArrayList<>();
		public final List<TestObject> children = Collections.unmodifiableList(modifiableChildren);
		public final Rectangle bounds;
		public final List<Point> connectionPoints; // In Absolute coordinates

		private TestObject(final TestObject parent, final Point position, final Dimension size,
				final Point[] connectionPoints) {
			this.parent = parent;
			if (parent != null) {
				parent.modifiableChildren.add(this);
			}
			this.bounds = new Rectangle(position, new Point(position.x + size.width, position.y + size.height));
			this.connectionPoints = Collections.unmodifiableList(Arrays.asList(connectionPoints));
		}

		/**
		 * Represents a box with connection points on left and right.
		 * @param parent
		 * @param position
		 * @param size
		 * @return
		 */
		public static TestObject createSimple(final TestObject parent, final Point position,
				final Dimension size) {
			return new TestObject(parent, position, size,
					new Point[] { new Point(position.x, position.y + size.height / 2.0),
							new Point(position.x + size.width, position.y + size.height / 2.0) });
		}

		/**
		 *
		 * @param parent
		 * @param position
		 * @param size
		 * @param exteriorConnectionPoint relative to position
		 * @param interiorConnectionPoint relative to position
		 * @return
		 */
		public static TestObject createPort(final TestObject parent, final Point position, final Dimension size,
				final Point exteriorConnectionPoint, final Point interiorConnectionPoint) {
			return new TestObject(parent, position, size,
					new Point[] {
							new Point(position.x + exteriorConnectionPoint.x, position.y + exteriorConnectionPoint.y),
							new Point(position.x + interiorConnectionPoint.x,
									position.y + interiorConnectionPoint.y) });
		}
	}
}
