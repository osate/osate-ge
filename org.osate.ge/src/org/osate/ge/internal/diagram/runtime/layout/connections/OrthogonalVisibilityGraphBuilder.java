package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osate.ge.graphics.Point;

import com.google.common.collect.ImmutableList;


// TODO: Cite paper
// TODO: Describe modifications.
// TODO: Unit test with sanity checks.
//     Check that left and right agree. Also up and down.
//     Segments max > min
public class OrthogonalVisibilityGraphBuilder {
	static class Graph {
		public final ImmutableList<OrthogonalGraphNode> nodes;

		public Graph(final ImmutableList<OrthogonalGraphNode> nodes) {
			this.nodes = nodes;
		}
	}

	// TODO: Should this exist? Should just have the version that is passed segments and then a helper in another class?
	public static <T> Graph create(final LineSegmentFinderDataSource<T> ds) {
		return buildGraph(LineSegmentFinder.buildSegments(ds));
	}

	static <T> Graph buildGraph(final OrthogonalSegments<T> segments) {
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

		final Map<VerticalSegment<T>, OrthogonalGraphNode> vsToUpNode = new HashMap<>();

		for (final HorizontalSegment<T> hs : segments.horizontalSegments) {
			OrthogonalGraphNode leftNode = null;

			for (final VerticalSegment<T> vs : segments.verticalSegments) {
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
}
