package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonalVisibilityGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweepEvent;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeper;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeper.EventType;
import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeperEventHandler;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

//TODO: Cite paper
//TODO: Describe modifications.
//TODO: Unit test with sanity checks.
//Check that left and right agree. Also up and down.
//Segments max > min
public class OrthogonalVisibilityGraphFactory {
	public static <T> OrthogonalGraph create(final OrthogonalSegmentsFactoryDataSource<T> segmentsFactoryDataSource) {
		return create(OrthogonalSegmentsFactory.create(segmentsFactoryDataSource));
	}

	public static <T> OrthogonalGraph create(final OrthogonalSegments<T> segments) {
		// Perform a vertical sweep from left to right.
		// Start by creating events for the start and end points of each horizontal segments and for the location of each vertical segment.
		final List<LineSweepEvent<Object>> events = new ArrayList<>(
				segments.horizontalSegments.size() * 2 + segments.verticalSegments.size());

		for (final HorizontalSegment<T> hs : segments.horizontalSegments) {
			events.add(new LineSweepEvent<Object>(EventType.OPEN, hs.minX, hs));
			events.add(new LineSweepEvent<Object>(EventType.CLOSE, hs.maxX, hs));
		}

		for (final VerticalSegment<T> vs : segments.verticalSegments) {
			events.add(new LineSweepEvent<Object>(EventType.POINT, vs.x, vs));
		}

		LineSweeper.sort(events);

		final TreeMultimap<Double, Object> openObjects = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());

		// TODO: rename
		final LineIntersectionCollector<Object> nodeCollector = new LineIntersectionCollector<Object>();

		// TODO: Key function should be between the collector?
		LineSweeper.sweep(events, openObjects, o -> ((HorizontalSegment<?>) o).y, nodeCollector);

		nodeCollector.finish();

		return new OrthogonalGraph(nodeCollector.positionToNodesMap);
	}

	// TODO: Rename
	// TODO: Generic for segment type?
	private static class SegmentNodePair<SegmentType> {
		public SegmentType segment;
		public OrthogonalGraphNode node;

		public SegmentNodePair(final SegmentType segment, final OrthogonalGraphNode node) {
			this.segment = segment;
			this.node = node;
		}
	}

	// TODO: Move. Rename
	private static class LineIntersectionCollector<T> implements LineSweeperEventHandler<Object> {
		private final Map<Point, OrthogonalGraphNode> positionToNodesMap = new HashMap<>();

		// For every y, track the rightmost part of the segment and left
		private final Map<Double, SegmentNodePair<HorizontalSegment<?>>> yToLeftNodeSegmentMap = new HashMap<>();

		private double lastX = Double.NaN;
		// Processed and cleared whenever a new X is encountered.
		private final TreeMap<Double, SegmentNodePair<VerticalSegment<?>>> yToVerticalNodeMap = new TreeMap<Double, SegmentNodePair<VerticalSegment<?>>>();

		@Override
		public void handleEvent(final LineSweepEvent<Object> event, final TreeMultimap<Double, Object> openObjects) {
			if (event.position != lastX) {
				processOrderedNodes();
				lastX = event.position;
			}

			// TODO: Get all intersections
			if (event.type == EventType.POINT) {
				// TODO: Support any vertical segment... Type doesn't really matter.. Right?
				@SuppressWarnings("unchecked")
				final VerticalSegment<?> vs = (VerticalSegment<?>) event.tag;
				for (final Double key : openObjects.keySet().subSet(vs.minY, true, vs.maxY, true)) {
					for (final Object hsObj : openObjects.get(key)) {
						final HorizontalSegment<T> hs = (HorizontalSegment<T>) hsObj;

						// TODO: Should be intersection
						final Point nodePosition = new Point(vs.x, hs.y);
						final boolean nodeExists = positionToNodesMap.containsKey(nodePosition);

						// TODO: Rename
						if (nodeExists) {
							// Update the segment stored in the tuple maps if the previously stored segment is to the left/above this segment
							// This is needed to prevent from using the incorrect segment when segments overlap.
							final SegmentNodePair<HorizontalSegment<?>> prevPair = yToLeftNodeSegmentMap
									.get(nodePosition.y);
							if (prevPair.segment.maxX < hs.maxX) {
								prevPair.segment = hs;
							}

							final SegmentNodePair<VerticalSegment<?>> prevVertPair = yToVerticalNodeMap
									.get(nodePosition.y);
							if (prevVertPair.segment.maxY < vs.maxY) {
								prevVertPair.segment = vs;
							}
						} else {
							final OrthogonalGraphNode newNode = new OrthogonalGraphNode(nodePosition);

							positionToNodesMap.put(nodePosition, newNode);
							yToVerticalNodeMap.put(newNode.position.y, new SegmentNodePair<>(vs, newNode));

							final SegmentNodePair<HorizontalSegment<?>> leftTestTuple = yToLeftNodeSegmentMap
									.put(nodePosition.y, new SegmentNodePair<>(hs, newNode));

							if (leftTestTuple != null && leftTestTuple.segment.maxX >= hs.minX) {
								newNode.setNeighbor(OrthogonalDirection.LEFT, leftTestTuple.node);
							}
						}
					}
					// vs.minY
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
			SegmentNodePair<VerticalSegment<?>> prev = null;
			final Iterator<SegmentNodePair<VerticalSegment<?>>> tupleIt = yToVerticalNodeMap.values().iterator(); // TODO: Rename
			if (tupleIt.hasNext()) {
				prev = tupleIt.next();
			}

			while (tupleIt.hasNext()) {
				final SegmentNodePair<VerticalSegment<?>> tmp = tupleIt.next();

				if (prev.segment.maxY >= tmp.segment.minY) {
					tmp.node.setNeighbor(OrthogonalDirection.UP, prev.node);
				}

				prev = tmp;
			}

			yToVerticalNodeMap.clear();
		}
	}
}
