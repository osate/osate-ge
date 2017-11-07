package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.Dimension;

// TODO: Rename
public class TestModel {
	public static OrthogonalVisibilityGraphDataSource<TestElement> createDataSource() {
		final List<TestObject> objects = Arrays.asList(
				TestObject.createNonPort(null, new Point(100, 100), new Dimension(200, 200)),
				TestObject.createNonPort(null, new Point(200, 350), new Dimension(400, 50)),
				TestObject.createNonPort(null, new Point(500, 100), new Dimension(200, 200)));

		// TestObject.createPort(objects.get(1), new Point(500, 100), new Dimension(50, 200),
		// TODO: Reeenable connection points
		TestObject.createPort(objects.get(1), new Point(550, 150), new Dimension(50, 50),
				new Point[] { /* new Point(20, 10), new Point(20, 30), new Point(20, 50) */ },
				new Point[] { /* new Point(60, 10), new Point(60, 30), new Point(60, 50) */ });

		final OrthogonalVisibilityGraphDataSource<TestElement> testDataSource = new OrthogonalVisibilityGraphDataSource<TestElement>() {
			@Override
			public TestElement getParent(final TestElement o) {
				// TODO: Needed for connection points too
				return o.parent;
			}

			@Override
			public List<TestObject> getChildren(final TestElement o) {
				if (o == null) {
					return objects;
				} else {
					return ((TestObject) o).children;
				}
			}

			@Override
			public int getNumberOfConnectionPoints(final TestElement o) {
				return ((TestObject) o).connectionPoints.size();
			}

			@Override
			public TestElement getConnectionPoint(final TestElement o, final int index) {
				return ((TestObject) o).connectionPoints.get(index);
			}

			@Override
			public Point getConnectionPointPosition(final TestElement o) {
				return ((TestConnectionPoint) o).position;
			}

			@Override
			public Rectangle getBounds(TestElement o) {
				return ((TestObject) o).bounds;
			}

			@Override
			public Rectangle getConnectionPointSegmentBounds(final TestElement o, final int index) {
				return ((TestObject) o).connectionPoints.get(index).segmentBounds;
			}
		};

		return testDataSource;
	}

	// TOOD: Rename
	private static class TestElement {
		public final TestElement parent;

		public TestElement(final TestElement parent) {
			this.parent = parent;
		}
	}

	private static class TestConnectionPoint extends TestElement {
		public final Point position;
		public final Rectangle segmentBounds;

		public TestConnectionPoint(final Point position, final Rectangle segmentBounds) {
			super(null); // TODO
			this.position = position;
			this.segmentBounds = segmentBounds;
		}
	}

	private static class TestObject extends TestElement {
		private final boolean isPort;
		private final List<TestObject> modifiableChildren = new ArrayList<>();
		public final List<TestObject> children = Collections.unmodifiableList(modifiableChildren);
		public final Rectangle bounds;
		public final List<TestConnectionPoint> connectionPoints;

		private TestObject(final TestObject parent, final boolean isPort, final Point position, final Dimension size,
				final TestConnectionPoint[] connectionPoints) {
			super(parent);
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
		public static TestObject createNonPort(final TestObject parent, final Point position, final Dimension size) {
			return new TestObject(parent, false, position, size,
					new TestConnectionPoint[] {
							new TestConnectionPoint(new Point(position.x, position.y + size.height / 2.0),
									getFirstNonPortBounds(parent)),
							new TestConnectionPoint(new Point(position.x + size.width, position.y + size.height / 2.0),
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
							new Point(position.x + interiorConnectionPointPosition.x,
									position.y + interiorConnectionPointPosition.y),
							new Point(nonPortParentBounds.max.x, position.y + interiorConnectionPointPosition.y));
				}

				connectionPoints.add(new TestConnectionPoint(new Point(position.x + interiorConnectionPointPosition.x,
						position.y + interiorConnectionPointPosition.y), interiorPortSegmentBounds));
			}

			final Rectangle nonPortGrandparentBounds = getNonPortBounds(parent, 2);
			for (final Point exteriorConnectionPointPosition : exteriorConnectionPointPositions) {
				final Rectangle exteriorPortSegmentBounds;
				if (nonPortGrandparentBounds == null) {
					exteriorPortSegmentBounds = new Rectangle(
							new Point(Double.NEGATIVE_INFINITY, position.y + exteriorConnectionPointPosition.y),
							new Point(position.x + exteriorConnectionPointPosition.x,
									position.y + exteriorConnectionPointPosition.y));
				} else {
					exteriorPortSegmentBounds = new Rectangle(
							new Point(nonPortGrandparentBounds.min.x, position.y + exteriorConnectionPointPosition.y),
							new Point(position.x + exteriorConnectionPointPosition.x,
									position.y + exteriorConnectionPointPosition.y));
				}
				connectionPoints.add(new TestConnectionPoint(new Point(position.x + exteriorConnectionPointPosition.x,
						position.y + exteriorConnectionPointPosition.y), exteriorPortSegmentBounds));
			}

			return new TestObject(parent, true, position, size,
					connectionPoints.toArray(new TestConnectionPoint[connectionPoints.size()]));
		}
	}
}
