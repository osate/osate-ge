package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.osate.ge.graphics.Point;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;


// TODO: Cite paper
// TODO: Describe modifications.
// TODO: Unit test with sanity checks.
//     Check that left and right agree. Also up and down.
//     Segments max > min
public class OrthogonalVisibilityGraphBuilder {
	// TODO: Rename to model
	public static interface DataSource<T> {
		// All children are assumed to be contained in the bounding box of their parent.
		List<T> getChildren(final T o);

		int getNumberOfConnectionPoints(final T o);

		// TODO: Need concept of direction in points? Or some way to avoid collision with owner.
		// TODO: Rename to getConnectionPointPosition?
		org.osate.ge.graphics.Point getConnectionPoint(final T o, final int index);

		// TODO: Rename
		/**
		 *
		 * @param o
		 * @param index
		 * @return a null result means that the segments should be unconstrained.
		 */
		Rectangle getConnectionPointSegmentBounds(final T o, final int index);

		Rectangle getBounds(final T o);
	}

	public static class Rectangle {
		public final Point min;
		public final Point max;

		public Rectangle(final Point min, final Point max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public String toString() {
			return "Rect {" + min + "," + max + "}";
		}
	}

	static class VerticalSegment {
		public final double x;
		public final double minY;
		public final double maxY;

		public VerticalSegment(final double x, final double minY, final double maxY) {
			this.x = x;
			this.minY = minY;
			this.maxY = maxY;
			// TODO: Assert max > min
		}

		@Override
		public String toString() {
			return "VerticalSegment {" + x + "," + minY + " -> " + maxY + "}";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(maxY);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(minY);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			VerticalSegment other = (VerticalSegment) obj;
			if (Double.doubleToLongBits(maxY) != Double.doubleToLongBits(other.maxY)) {
				return false;
			}
			if (Double.doubleToLongBits(minY) != Double.doubleToLongBits(other.minY)) {
				return false;
			}
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
				return false;
			}
			return true;
		}
	}

	static class HorizontalSegment {
		public final double y;
		public final double minX;
		public final double maxX;

		public HorizontalSegment(final double y, final double minX, final double maxX) {
			this.y = y;
			this.minX = minX;
			this.maxX = maxX;
			// TODO: Assert max > min
		}

		@Override
		public String toString() {
			return "HorizontalSegment {" + y + "," + minX + " -> " + maxX + "}";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(maxX);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(minX);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			HorizontalSegment other = (HorizontalSegment) obj;
			if (Double.doubleToLongBits(maxX) != Double.doubleToLongBits(other.maxX)) {
				return false;
			}
			if (Double.doubleToLongBits(minX) != Double.doubleToLongBits(other.minX)) {
				return false;
			}
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
				return false;
			}
			return true;
		}
	}

	static class Graph {
		public final ImmutableList<OrthogonalGraphNode> nodes;

		public Graph(final ImmutableList<OrthogonalGraphNode> nodes) {
			this.nodes = nodes;
		}
	}

	public static class Segments {
		public final NavigableSet<HorizontalSegment> horizontalSegments;
		public final NavigableSet<VerticalSegment> verticalSegments;

		public Segments(final NavigableSet<HorizontalSegment> horizontalSegments,
				final NavigableSet<VerticalSegment> verticalSegments) {
			this.horizontalSegments = horizontalSegments;
			this.verticalSegments = verticalSegments;
		}
	}
	// TODO: Return value
	// TODO: Accept connector points.
	// TODO: Need concept of container and owners right? To support hierarical.
	// TODO: Accept Obstacles.
	public static <T> Graph create(final DataSource<T> ds) {
		return buildGraph(buildSegments(ds));
	}

	static <T> Graph buildGraph(final Segments segments) {
		final List<OrthogonalGraphNode> nodes = new ArrayList<>();

		// TODO: Some intersections aren't showing up...
		// Intersections in the middle of object segments aren't showing up either.
		// That's because segments aren't being created for edges.

		// Create nodes at intersections of segments
		// TODO: Avoid duplicates. Distinct clears them out but would be better not to create them in the first place
		// Compare performance of a hash set versus creating and then clearing

		// TO Create Horizontal Edges
		// Sort Nodes By X
		// Map segment to node.
		// Set edges based on position of node...

		// TODO: Group nodes by horizontal segment. Sorted
		// Go through each node and set edges.

		// TODO: Sort segments.. Already sorted?

		final Map<VerticalSegment, OrthogonalGraphNode> vsToUpNode = new HashMap<>();

		for (final HorizontalSegment hs : segments.horizontalSegments) {
			OrthogonalGraphNode leftNode = null;

			for (final VerticalSegment vs : segments.verticalSegments) {
				if (vs.minY <= hs.y && vs.maxY >= hs.y && vs.x >= hs.minX && vs.x <= hs.maxX) {
					final OrthogonalGraphNode newNode = new OrthogonalGraphNode(new Point(vs.x, hs.y));
					nodes.add(newNode);

					if(leftNode != null) {
						leftNode.setNeighbor(OrthogonalDirection.RIGHT, newNode);
					}

					leftNode = newNode;

					// TODO: Get the previous node from the same vertical segment.
					final OrthogonalGraphNode upNode = vsToUpNode.get(vs);
					if (upNode != null) {
						upNode.setNeighbor(OrthogonalDirection.DOWN, newNode);
					}

					vsToUpNode.put(vs, newNode);
				}
			}
		}

		// TODO: Edges

		return new Graph(nodes.stream().distinct().collect(ImmutableList.toImmutableList()));
	}

	// Assumes that segments are sorted by the fixed coordinate and the min of the secondary coordinate.
	static <T> void addSegmentsForChild(final DataSource<T> ds, final T object, final Rectangle segmentBounds,
			final Iterable<T> collisionObjects,
			final NavigableSet<HorizontalSegment> horizontalSegments,
			final NavigableSet<VerticalSegment> verticalSegments,
			final double x, final double y) {

		// TODO: Optimize finding spaces by sorting bounding boxes or using a specialized data structure

		// Create Vertical Segments
		double up = segmentBounds == null ? Double.NEGATIVE_INFINITY : segmentBounds.min.y;
		double down = segmentBounds == null ? Double.POSITIVE_INFINITY : segmentBounds.max.y;
		// TODO: Avoid allocations?
		final VerticalSegment topSearchVerticalSegment = new VerticalSegment(x, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		final VerticalSegment bottomSearchVerticalSegment = new VerticalSegment(x, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		final NavigableSet<VerticalSegment> verticalSegmentsMatchingX = verticalSegments
				.subSet(topSearchVerticalSegment, true,
						bottomSearchVerticalSegment, true);

		boolean addVerticalSegments = true;
		final VerticalSegment yPointSegment = new VerticalSegment(x, y, y);
		for (final VerticalSegment tmp : verticalSegmentsMatchingX.tailSet(yPointSegment, true)) {
			if (tmp.minY <= y && tmp.maxY >= y) {
				addVerticalSegments = false;
				break;
			}
		}

		if (addVerticalSegments) {
			for (final T o2 : collisionObjects) {
				final Rectangle b2 = ds.getBounds(o2);

				// Up
				if (b2.max.y < y && b2.min.x < x && b2.max.x > x) {
					up = Math.max(up, b2.max.y);
				}

				// Down
				if (b2.min.y > y && b2.min.x < x && b2.max.x > x) {
					down = Math.min(down, b2.min.y);
				}
			}

			verticalSegments.add(new VerticalSegment(x, up, down));
		}

		// Create Horizontal Segments
		double left = segmentBounds == null ? Double.NEGATIVE_INFINITY : segmentBounds.min.x;
		double right = segmentBounds == null ? Double.POSITIVE_INFINITY : segmentBounds.max.x;
		// TODO: Avoid allocations?
		final HorizontalSegment leftSearchHorizontalSegment = new HorizontalSegment(y, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
		final HorizontalSegment rightSearchHorizontalSegment = new HorizontalSegment(y, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		final NavigableSet<HorizontalSegment> horizontalSegmentsMatchingY = horizontalSegments
				.subSet(leftSearchHorizontalSegment, true, rightSearchHorizontalSegment, true);

		boolean addHorizontalSegments = true;
		final HorizontalSegment xPointSegment = new HorizontalSegment(y, x, x);
		for (final HorizontalSegment tmp : horizontalSegmentsMatchingY.tailSet(xPointSegment, true)) {
			if (tmp.minX <= x && tmp.maxX >= x) {
				addHorizontalSegments = false;
				break;
			}
		}

		if (addHorizontalSegments) {
			for (final T o2 : collisionObjects) {
				final Rectangle b2 = ds.getBounds(o2);

				// Left
				if (b2.max.x < x && b2.min.y < y && b2.max.y > y) {
					left = Math.max(left, b2.max.x);
				}

				// Right
				if (b2.min.x > x && b2.min.y < y && b2.max.y > y) {
					right = Math.min(right, b2.min.x);
				}
			}

			horizontalSegments.add(new HorizontalSegment(y, left, right));
		}
	}

	static <T> Segments buildSegments(final DataSource<T> ds) {
		// TODO: Look into horizontal or vertical sweep algorithms

		// TODO: Handle hierarchy

		// TODO: Need comparator
		// TODO: Need to compare other portions

		// TODO: Consider whether max needs to be compared too.. Shouldn't have overlapping segments...
		final Comparator<VerticalSegment> xComparator = (vs1, vs2) -> Double.compare(vs1.x, vs2.x);
		final TreeSet<VerticalSegment> verticalSegments = new TreeSet<>(
				xComparator.thenComparing((vs1, vs2) -> Double.compare(vs1.minY, vs2.minY)));

		final Comparator<HorizontalSegment> yComparator = (hs1, hs2) -> Double.compare(hs1.y, hs2.y);
		final TreeSet<HorizontalSegment> horizontalSegments = new TreeSet<>(
				yComparator.thenComparing((hs1, hs2) -> Double.compare(hs1.minX, hs2.minX)));

		addSegments(ds, verticalSegments, horizontalSegments, null);

		System.err.println("VERTICAL SEGMENTS: " + verticalSegments.size());
		System.err.println("HORIZONTAL SEGMENTS: " + horizontalSegments.size());

		// Create segments object
		return new Segments(Collections.unmodifiableNavigableSet(horizontalSegments),
				Collections.unmodifiableNavigableSet(verticalSegments));
	}

	private static <T> void addSegments(final DataSource<T> ds, final TreeSet<VerticalSegment> verticalSegments,
			final TreeSet<HorizontalSegment> horizontalSegments, final T parent) {
		// TODO: Want to treat each point independently instead of just checking a single direction?

		// Look for interesting segments
		final List<T> objects = ds.getChildren(parent);
		final Rectangle parentBounds = parent == null ? null : ds.getBounds(parent);
		for (int i = 0; i < objects.size(); i++) {
			final T child = objects.get(i);
			final Rectangle b1 = ds.getBounds(child);

			final Iterable<T> collisionObjects = Iterables.concat(objects, ds.getChildren(child));
			addSegmentsForChild(ds, child, parentBounds, collisionObjects, horizontalSegments, verticalSegments, b1.min.x,
					b1.min.y);
			addSegmentsForChild(ds, child, parentBounds, collisionObjects, horizontalSegments, verticalSegments, b1.max.x,
					b1.min.y);
			addSegmentsForChild(ds, child, parentBounds, collisionObjects, horizontalSegments, verticalSegments, b1.min.x,
					b1.max.y);
			addSegmentsForChild(ds, child, parentBounds, collisionObjects, horizontalSegments, verticalSegments, b1.max.x,
					b1.max.y);

			for (int connectionPointIndex = 0; connectionPointIndex < ds
					.getNumberOfConnectionPoints(child); connectionPointIndex++) {
				final Point cp = ds.getConnectionPoint(child, connectionPointIndex);
				addSegmentsForChild(ds, child, ds.getConnectionPointSegmentBounds(child, connectionPointIndex),
						collisionObjects, horizontalSegments, verticalSegments, cp.x,
						cp.y);
			}

			addSegments(ds, verticalSegments, horizontalSegments, child);
		}
	}
}
