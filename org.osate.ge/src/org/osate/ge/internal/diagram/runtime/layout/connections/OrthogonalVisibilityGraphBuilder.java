package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osate.ge.graphics.Point;

import com.google.common.collect.ImmutableList;


// TODO: Cite paper
// TODO: Describe modifications.
// TODO: Unit test with sanity checks.
//     Check that left and right agree. Also up and down.
//     Segments max > min
public class OrthogonalVisibilityGraphBuilder {
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
		public final Set<HorizontalSegment> horizontalSegments;
		public final Set<VerticalSegment> verticalSegments;

		public Segments(final Set<HorizontalSegment> horizontalSegments, final Set<VerticalSegment> verticalSegments) {
			this.horizontalSegments = horizontalSegments;
			this.verticalSegments = verticalSegments;
		}
	}
	// TODO: Return value
	// TODO: Accept connector points.
	// TODO: Need concept of container and owners right? To support hierarical.
	// TODO: Accept Obstacles.
	public static <T> Graph create(final OrthogonalVisibilityGraphDataSource<T> ds) {
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

	static <T> Segments buildSegments(final OrthogonalVisibilityGraphDataSource<T> ds) {
		final Set<VerticalSegment> verticalSegments = LineSegmentFinder.findVerticalSegments(ds);
		final Set<HorizontalSegment> horizontalSegments = LineSegmentFinder.findHorizontalSegments(ds);

		// Create segments object
		return new Segments(Collections.unmodifiableSet(horizontalSegments),
				Collections.unmodifiableSet(verticalSegments));
	}
}
