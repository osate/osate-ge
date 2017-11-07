package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
	private static final double nodeIndicatorSize = 10;
	private static final double halfNodeIndicatorSize = nodeIndicatorSize / 2.0;

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
				TestObject.createNonPort(null, new Point(100, 100), new Dimension(200, 200)),
				TestObject.createNonPort(null, new Point(500, 100), new Dimension(200, 200)));

		// TestObject.createSimple(objects.get(0), new Point(50, 10), new Dimension(50, 50));
		// TestObject.createSimple(objects.get(0), new Point(80, 130), new Dimension(50, 50));

		// TestObject.createSimple(objects.get(1), new Point(420, 50), new Dimension(50, 50));
		// TestObject.createSimple(objects.get(1), new Point(500, 30), new Dimension(50, 50));

		TestObject.createPort(objects.get(1), new Point(500, 130), new Dimension(80, 80),
				new Point[] { new Point(20, 10), new Point(20, 30), new Point(20, 50) },
				new Point[] { new Point(60, 10), new Point(60, 30), new Point(60, 50) });

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
				return o.connectionPoints.get(index).position;
			}

			@Override
			public Rectangle getBounds(TestObject o) {
				return o.bounds;
			}

			@Override
			public Rectangle getConnectionPointSegmentBounds(final TestObject o, final int index) {
				return o.connectionPoints.get(index).segmentBounds;
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

		primaryStage.setTitle("Connection Layout Test");
		final Group root = new Group();
		final Canvas canvas = new Canvas(800, 400);
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

	private static class TestConnectionPoint {
		public final Point position;
		public final Rectangle segmentBounds;

		public TestConnectionPoint(final Point position, final Rectangle segmentBounds) {
			this.position = position;
			this.segmentBounds = segmentBounds;
		}
	}

	private static class TestObject {
		public final TestObject parent;
		private final boolean isPort;
		private final List<TestObject> modifiableChildren = new ArrayList<>();
		public final List<TestObject> children = Collections.unmodifiableList(modifiableChildren);
		public final Rectangle bounds;
		public final List<TestConnectionPoint> connectionPoints;

		private TestObject(final TestObject parent, final boolean isPort, final Point position, final Dimension size,
				final TestConnectionPoint[] connectionPoints) {
			this.parent = parent;
			this.isPort = isPort;
			if (parent != null) {
				parent.modifiableChildren.add(this);
			}
			this.bounds = new Rectangle(position, new Point(position.x + size.width, position.y + size.height));
			this.connectionPoints = Collections.unmodifiableList(Arrays.asList(connectionPoints));
		}

		private static Rectangle getFirstNonPortBounds(final TestObject parent) {
			return getNonPortBounds(parent, 1);
		}

		private static Rectangle getNonPortBounds(final TestObject parent, final int searchDepth) {
			int depth = 0;
			for (TestObject tmp = parent; tmp != null; tmp = tmp.parent) {
				if (!parent.isPort) {
					depth++;
					if (depth == searchDepth) {
						return parent.bounds;
					}
				}
			}

			return null;
		}

		/**
		 * Represents a box with connection points on left and right.
		 * @param parent must not be a port
		 * @param position
		 * @param size
		 * @return
		 */
		public static TestObject createNonPort(final TestObject parent, final Point position,
				final Dimension size) {
			return new TestObject(parent, false, position, size,
					new TestConnectionPoint[] {
							new TestConnectionPoint(new Point(position.x, position.y + size.height / 2.0),
									getFirstNonPortBounds(parent)),
							new TestConnectionPoint(
									new Point(position.x + size.width, position.y + size.height / 2.0),
									getFirstNonPortBounds(parent)) });
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
				final Point[] exteriorConnectionPointPositions, final Point[] interiorConnectionPointPositions) {
			Objects.requireNonNull(parent, "parent must not be null");

			final List<TestConnectionPoint> connectionPoints = new ArrayList<>();

			// Constrain the segment bounds of the interior and exterior ports so that the segments generated by the graph builder will be horizontal and will
			// only extend outwards in one direction. In a real implementation this would need to consider the side to which the port is docked.
			final Rectangle nonPortParentBounds = getNonPortBounds(parent, 1);

			for (final Point interiorConnectionPointPosition : interiorConnectionPointPositions) {
				final Rectangle interiorPortSegmentBounds;
				if (nonPortParentBounds == null) {
					throw new RuntimeException("Unexpected case");
				} else {
					interiorPortSegmentBounds = new Rectangle(
							new Point(position.x + interiorConnectionPointPosition.x, position.y + interiorConnectionPointPosition.y),
							new Point(nonPortParentBounds.max.x, position.y + interiorConnectionPointPosition.y));
				}

				connectionPoints.add(new TestConnectionPoint(
						new Point(position.x + interiorConnectionPointPosition.x, position.y + interiorConnectionPointPosition.y),
						interiorPortSegmentBounds));
			}

			final Rectangle nonPortGrandparentBounds = getNonPortBounds(parent, 2);
			for (final Point exteriorConnectionPointPosition : exteriorConnectionPointPositions) {
				final Rectangle exteriorPortSegmentBounds;
				if (nonPortGrandparentBounds == null) {
					exteriorPortSegmentBounds = new Rectangle(
							new Point(Double.NEGATIVE_INFINITY, position.y + exteriorConnectionPointPosition.y),
							new Point(position.x + exteriorConnectionPointPosition.x, position.y + exteriorConnectionPointPosition.y));
				} else {
					exteriorPortSegmentBounds = new Rectangle(
							new Point(nonPortGrandparentBounds.min.x, position.y + exteriorConnectionPointPosition.y),
							new Point(position.x + exteriorConnectionPointPosition.x, position.y + exteriorConnectionPointPosition.y));
				}
				connectionPoints.add(new TestConnectionPoint(
						new Point(position.x + exteriorConnectionPointPosition.x, position.y + exteriorConnectionPointPosition.y),
						exteriorPortSegmentBounds));
			}

			return new TestObject(parent, true, position, size,
					connectionPoints.toArray(new TestConnectionPoint[connectionPoints.size()]));
		}
	}
}
