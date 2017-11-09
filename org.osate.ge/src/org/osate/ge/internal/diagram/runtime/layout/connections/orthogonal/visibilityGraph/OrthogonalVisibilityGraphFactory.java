package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweepEvent;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeper;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeper.EventType;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeperEventHandler;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalDirection;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraph;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;

import com.google.common.collect.TreeMultimap;

/**
 * Creates an orthogonal visibility graph based on "interesting" horizontal and vertical segments.
 * See Wybrow M., Marriott K., Stuckey P.J. (2010) Orthogonal Connector Routing. In: Eppstein D., Gansner E.R. (eds) Graph Drawing. GD 2009.
 * Lecture Notes in Computer Science, vol 5849. Springer, Berlin, Heidelberg
 * Section 3
 *
 * Also see {@link OrthogonalSegmentsFactory} for a description of some of the differences in the interesting segments generated.
 */
public class OrthogonalVisibilityGraphFactory {
	public static interface NodeTagCreator<SegmentTag, NodeTag> {
		NodeTag create(Point nodePosition, HorizontalSegment<SegmentTag> hs, VerticalSegment<SegmentTag> vs);
	}

	public static interface EdgeTagCreator<NodeTag, EdgeTag> {
		EdgeTag create(OrthogonalGraphNode<NodeTag, EdgeTag> n1, OrthogonalGraphNode<NodeTag, EdgeTag> n2);
	}

	public static <SegmentTag, NodeTag, EdgeTag> OrthogonalGraph<NodeTag, EdgeTag> create(
			final OrthogonalSegmentsFactoryDataSource<SegmentTag> segmentsFactoryDataSource,
			final NodeTagCreator<SegmentTag, NodeTag> nodeTagCreator,
			final EdgeTagCreator<NodeTag, EdgeTag> edgeTagCreator) {

		return create(OrthogonalSegmentsFactory.create(segmentsFactoryDataSource), nodeTagCreator, edgeTagCreator);
	}

	public static <SegmentTag, NodeTag, EdgeTag> OrthogonalGraph<NodeTag, EdgeTag> create(
			final OrthogonalSegments<SegmentTag> segments,
			final NodeTagCreator<SegmentTag, NodeTag> nodeTagCreator,
			final EdgeTagCreator<NodeTag, EdgeTag> edgeTagCreator) {
		//
		// Perform a vertical sweep from left to right which will create node and edges.
		//

		// Start by creating events for the start and end points of each horizontal segments and for the location of each vertical segment.
		final List<LineSweepEvent<Object>> events = new ArrayList<>(
				segments.getHorizontalSegments().size() * 2 + segments.getVerticalSegments().size());

		for (final HorizontalSegment<SegmentTag> hs : segments.getHorizontalSegments()) {
			events.add(new LineSweepEvent<Object>(EventType.OPEN, hs.getMinX(), hs));
			events.add(new LineSweepEvent<Object>(EventType.CLOSE, hs.getMaxX(), hs));
		}

		for (final VerticalSegment<SegmentTag> vs : segments.getVerticalSegments()) {
			events.add(new LineSweepEvent<Object>(EventType.POINT, vs.x, vs));
		}

		LineSweeper.sortByPosition(events);

		// Create the nodes and edges
		final NodeCreator<SegmentTag, NodeTag, EdgeTag> nodeCreator = new NodeCreator<>(nodeTagCreator,
				edgeTagCreator);
		LineSweeper.sweep(events, hs -> hs.getY(), nodeCreator);
		nodeCreator.finish(); // Finish the creation process

		return new OrthogonalGraph<NodeTag, EdgeTag>(nodeCreator.positionToNodesMap);
	}

	private static class SegmentNodePair<SegmentType, NodeTag, EdgeTag> {
		public SegmentType segment;
		public OrthogonalGraphNode<NodeTag, EdgeTag> node;

		public SegmentNodePair(final SegmentType segment, final OrthogonalGraphNode<NodeTag, EdgeTag> node) {
			this.segment = segment;
			this.node = node;
		}
	}

	private static class NodeCreator<SegmentTag, NodeTag, EdgeTag>
	implements LineSweeperEventHandler<Object, HorizontalSegment<SegmentTag>, Double> {
		private final NodeTagCreator<SegmentTag, NodeTag> nodeTagCreator;
		private final EdgeTagCreator<NodeTag, EdgeTag> edgeTagCreator;
		private final Map<Point, OrthogonalGraphNode<NodeTag, EdgeTag>> positionToNodesMap = new HashMap<>();

		// For every y, track the rightmost part of the segment and left
		private final Map<Double, SegmentNodePair<HorizontalSegment<?>, NodeTag, EdgeTag>> yToLeftNodeSegmentMap = new HashMap<>();

		private double lastX = Double.NaN;
		// Processed and cleared whenever a new X is encountered.
		private final TreeMap<Double, SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag>> yToVerticalSegmentNodeMap = new TreeMap<>();

		public NodeCreator(final NodeTagCreator<SegmentTag, NodeTag> nodeTagCreator,
				final EdgeTagCreator<NodeTag, EdgeTag> edgeTagCreator) {
			this.nodeTagCreator = Objects.requireNonNull(nodeTagCreator, "nodeTagCreator must not be null");
			this.edgeTagCreator = Objects.requireNonNull(edgeTagCreator, "edgeTagCreator must not be null");
		}

		@Override
		public void handleEvent(final LineSweepEvent<Object> event,
				final TreeMultimap<Double, HorizontalSegment<SegmentTag>> openHorizontalSegments) {
			if (event.position != lastX) {
				assignVerticalNeighbors();
				lastX = event.position;
			}

			// A point represents a vertical segment. For each vertical segment, an open horizontal segment indicates an interesection.
			if (event.type == EventType.POINT) {
				@SuppressWarnings("unchecked")
				final VerticalSegment<SegmentTag> vs = (VerticalSegment<SegmentTag>) event.tag;
				for (final Double key : openHorizontalSegments.keySet().subSet(vs.minY, true, vs.maxY, true)) {
					for (final HorizontalSegment<SegmentTag> hs : openHorizontalSegments.get(key)) {
						// Create a node and or edge based on the intersection
						final Point nodePosition = new Point(vs.x, hs.getY());

						// Check if the node already exists.
						if (positionToNodesMap.containsKey(nodePosition)) {
							// Update the segment stored if the previously stored segment is to the left/above this segment
							// This is needed to ensure the segment that extends the farthest to the right is used when determining if nodes are connected.
							final SegmentNodePair<HorizontalSegment<?>, NodeTag, EdgeTag> prevPair = yToLeftNodeSegmentMap
									.get(nodePosition.y);
							if (prevPair.segment.getMaxX() < hs.getMaxX()) {
								prevPair.segment = hs;
							}

							// Similarly, do the same for vertical segments to ensure the segment which extends downward the most is used.
							final SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag> prevVerticalPair = yToVerticalSegmentNodeMap
									.get(nodePosition.y);
							if (prevVerticalPair.segment.maxY < vs.maxY) {
								prevVerticalPair.segment = vs;
							}
						} else {
							// Create and store the node
							final OrthogonalGraphNode<NodeTag, EdgeTag> newNode = new OrthogonalGraphNode<>(
									nodePosition,
									nodeTagCreator.create(nodePosition, hs, vs));
							positionToNodesMap.put(nodePosition, newNode);

							// Store the vertical segment pair in the map so it can be used to find vertical neighbors once the X position changes or
							// the finish() method is called.
							yToVerticalSegmentNodeMap.put(newNode.getPosition().y, new SegmentNodePair<>(vs, newNode));

							// Store the horizontal segment pair so that the left node can be determined for future nodes with the same y value.
							final SegmentNodePair<HorizontalSegment<?>, NodeTag, EdgeTag> leftNeighbor = yToLeftNodeSegmentMap
									.put(nodePosition.y, new SegmentNodePair<>(hs, newNode));

							// Set the left neighbor if there was a node with the same y value and its horizontal segment extends to the segment connected to
							// this node.
							if (leftNeighbor != null && leftNeighbor.segment.getMaxX() >= hs.getMinX()) {
								newNode.setNeighbor(OrthogonalDirection.LEFT, leftNeighbor.node,
										edgeTagCreator.create(newNode, leftNeighbor.node));
							}
						}
					}
				}

			}
		}

		// Must be called to finish setting vertical neighbors.
		public void finish() {
			assignVerticalNeighbors();
		}

		// Sets the vertical neighbors and then clears the y to vertical node map.
		private void assignVerticalNeighbors() {
			SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag> prev = null;
			final Iterator<SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag>> it = yToVerticalSegmentNodeMap.values()
					.iterator();
			if (it.hasNext()) {
				prev = it.next();
			}

			while (it.hasNext()) {
				final SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag> tmp = it.next();

				if (prev.segment.maxY >= tmp.segment.minY) {
					tmp.node.setNeighbor(OrthogonalDirection.UP, prev.node, edgeTagCreator.create(tmp.node, prev.node));
				}

				prev = tmp;
			}

			yToVerticalSegmentNodeMap.clear();
		}
	}
}
