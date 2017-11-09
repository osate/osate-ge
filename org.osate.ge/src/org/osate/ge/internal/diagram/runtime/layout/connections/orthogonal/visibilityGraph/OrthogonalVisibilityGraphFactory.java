package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph;

import java.util.ArrayList;
import java.util.Comparator;
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
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalDirection;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraph;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeperEventHandler;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

//TODO: Cite paper
//TODO: Describe modifications.
//TODO: Unit test with sanity checks.
//Check that left and right agree. Also up and down.
//Segments max > min
public class OrthogonalVisibilityGraphFactory {
	public static interface NodeTagCreator<SegmentTag, NodeTag> {
		NodeTag create(HorizontalSegment<SegmentTag> hs, VerticalSegment<SegmentTag> vs);
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
		// Perform a vertical sweep from left to right.
		// Start by creating events for the start and end points of each horizontal segments and for the location of each vertical segment.
		final List<LineSweepEvent<Object>> events = new ArrayList<>(
				segments.horizontalSegments.size() * 2 + segments.verticalSegments.size());

		for (final HorizontalSegment<SegmentTag> hs : segments.horizontalSegments) {
			events.add(new LineSweepEvent<Object>(EventType.OPEN, hs.minX, hs));
			events.add(new LineSweepEvent<Object>(EventType.CLOSE, hs.maxX, hs));
		}

		for (final VerticalSegment<SegmentTag> vs : segments.verticalSegments) {
			events.add(new LineSweepEvent<Object>(EventType.POINT, vs.x, vs));
		}

		LineSweeper.sort(events);

		final TreeMultimap<Double, Object> openObjects = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());

		// TODO: rename
		final NodeCreator<SegmentTag, NodeTag, EdgeTag> nodeCollector = new NodeCreator<>(nodeTagCreator,
				edgeTagCreator);

		// TODO: Key function should be between the collector?
		LineSweeper.sweep(events, openObjects, o -> ((HorizontalSegment<?>) o).y, nodeCollector);

		nodeCollector.finish();

		return new OrthogonalGraph<NodeTag, EdgeTag>(nodeCollector.positionToNodesMap);
	}

	// TODO: Rename
	// TODO: Generic for segment type?
	private static class SegmentNodePair<SegmentType, NodeTag, EdgeTag> {
		public SegmentType segment;
		public OrthogonalGraphNode<NodeTag, EdgeTag> node;

		public SegmentNodePair(final SegmentType segment, final OrthogonalGraphNode<NodeTag, EdgeTag> node) {
			this.segment = segment;
			this.node = node;
		}
	}

	// TODO: Move. Rename. Creates edges too
	private static class NodeCreator<SegmentTag, NodeTag, EdgeTag> implements LineSweeperEventHandler<Object> {
		private final NodeTagCreator<SegmentTag, NodeTag> nodeTagCreator;
		private final EdgeTagCreator<NodeTag, EdgeTag> edgeTagCreator;
		private final Map<Point, OrthogonalGraphNode<NodeTag, EdgeTag>> positionToNodesMap = new HashMap<>();

		// For every y, track the rightmost part of the segment and left
		private final Map<Double, SegmentNodePair<HorizontalSegment<?>, NodeTag, EdgeTag>> yToLeftNodeSegmentMap = new HashMap<>();

		private double lastX = Double.NaN;
		// Processed and cleared whenever a new X is encountered.
		private final TreeMap<Double, SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag>> yToVerticalNodeMap = new TreeMap<>();

		public NodeCreator(final NodeTagCreator<SegmentTag, NodeTag> nodeTagCreator,
				final EdgeTagCreator<NodeTag, EdgeTag> edgeTagCreator) {
			this.nodeTagCreator = Objects.requireNonNull(nodeTagCreator, "nodeTagCreator must not be null");
			this.edgeTagCreator = Objects.requireNonNull(edgeTagCreator, "edgeTagCreator must not be null");
		}

		@Override
		public void handleEvent(final LineSweepEvent<Object> event, final TreeMultimap<Double, Object> openObjects) {
			if (event.position != lastX) {
				processOrderedNodes();
				lastX = event.position;
			}

			// TODO: Get all intersections
			if (event.type == EventType.POINT) {
				@SuppressWarnings("unchecked")
				final VerticalSegment<SegmentTag> vs = (VerticalSegment<SegmentTag>) event.tag;
				for (final Double key : openObjects.keySet().subSet(vs.minY, true, vs.maxY, true)) {
					for (final Object hsObj : openObjects.get(key)) {
						@SuppressWarnings("unchecked")
						final HorizontalSegment<SegmentTag> hs = (HorizontalSegment<SegmentTag>) hsObj;

						// TODO: Should be intersection
						final Point nodePosition = new Point(vs.x, hs.y);
						final boolean nodeExists = positionToNodesMap.containsKey(nodePosition);

						// TODO: Rename
						if (nodeExists) {
							// Update the segment stored in the tuple maps if the previously stored segment is to the left/above this segment
							// This is needed to prevent from using the incorrect segment when segments overlap.
							final SegmentNodePair<HorizontalSegment<?>, NodeTag, EdgeTag> prevPair = yToLeftNodeSegmentMap
									.get(nodePosition.y);
							if (prevPair.segment.maxX < hs.maxX) {
								prevPair.segment = hs;
							}

							final SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag> prevVerticalPair = yToVerticalNodeMap
									.get(nodePosition.y);
							if (prevVerticalPair.segment.maxY < vs.maxY) {
								prevVerticalPair.segment = vs;
							}
						} else {
							final OrthogonalGraphNode<NodeTag, EdgeTag> newNode = new OrthogonalGraphNode<>(
									nodePosition,
									nodeTagCreator.create(hs, vs));

							positionToNodesMap.put(nodePosition, newNode);
							yToVerticalNodeMap.put(newNode.position.y, new SegmentNodePair<>(vs, newNode));

							// TODO: Rename
							final SegmentNodePair<HorizontalSegment<?>, NodeTag, EdgeTag> leftTestTuple = yToLeftNodeSegmentMap
									.put(nodePosition.y, new SegmentNodePair<>(hs, newNode));

							if (leftTestTuple != null && leftTestTuple.segment.maxX >= hs.minX) {
								newNode.setNeighbor(OrthogonalDirection.LEFT, leftTestTuple.node,
										edgeTagCreator.create(newNode, leftTestTuple.node));
							}
						}
					}
				}

			}
		}

		// Must be called to finish setting vertical neighbors.
		public void finish() {
			processOrderedNodes();
		}

		// Sets the vertical neighbors and then cleared
		// TODO: Rename
		private void processOrderedNodes() {
			SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag> prev = null;
			final Iterator<SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag>> tupleIt = yToVerticalNodeMap.values()
					.iterator(); // TODO: Rename
			if (tupleIt.hasNext()) {
				prev = tupleIt.next();
			}

			while (tupleIt.hasNext()) {
				final SegmentNodePair<VerticalSegment<?>, NodeTag, EdgeTag> tmp = tupleIt.next();

				if (prev.segment.maxY >= tmp.segment.minY) {
					tmp.node.setNeighbor(OrthogonalDirection.UP, prev.node, edgeTagCreator.create(tmp.node, prev.node));
				}

				prev = tmp;
			}

			yToVerticalNodeMap.clear();
		}
	}
}
