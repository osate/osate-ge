package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.Sweeper.Event;
import org.osate.ge.internal.diagram.runtime.layout.connections.Sweeper.EventType;
import org.osate.ge.internal.diagram.runtime.layout.connections.Sweeper.ResultCollector;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

// TODO: Cite paper
// TODO: Describe modifications.
// TODO: Unit test with sanity checks.
//     Check that left and right agree. Also up and down.
//     Segments max > min
public class OrthogonalVisibilityGraphBuilder {
	static class Graph {
		public final Collection<OrthogonalGraphNode> nodes;

		public Graph(final Collection<OrthogonalGraphNode> nodes) {
			this.nodes = Collections.unmodifiableCollection(nodes);
		}
	}

	// TODO: Should this exist? Should just have the version that is passed segments and then a helper in another class?
	public static <T> Graph create(final LineSegmentFinderDataSource<T> ds) {
		return buildGraph(LineSegmentFinder.buildSegments(ds));
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
	private static class LineIntersectionCollector<T> implements ResultCollector<Object> {
		private final Map<Point, OrthogonalGraphNode> positionToNodesMap = new HashMap<>();

		// For every y, track the rightmost part of the segment and left
		private final Map<Double, SegmentNodePair<HorizontalSegment<?>>> yToLeftNodeSegmentMap = new HashMap<>();

		private double lastX = Double.NaN;
		// Processed and cleared whenever a new X is encountered.
		private final TreeMap<Double, SegmentNodePair<VerticalSegment<?>>> yToVerticalNodeMap = new TreeMap<Double, SegmentNodePair<VerticalSegment<?>>>();

		@Override
		public void handleEvent(final Event<Object> event, final TreeMultimap<Double, Object> openObjects) {
			if (event.position != lastX) {
				processOrderedNodes();
				lastX = event.position;
			}

			// TODO: Get all intersections
			if(event.type == EventType.POINT) {
				// TODO: Support any vertical segment... Type doesn't really matter.. Right?
				@SuppressWarnings("unchecked")
				final VerticalSegment<?> vs = (VerticalSegment<?>) event.tag;
				for(final Double key : openObjects.keySet().subSet(vs.minY, true, vs.maxY, true)) {
					for(final Object hsObj : openObjects.get(key)) {
						final HorizontalSegment<T> hs = (HorizontalSegment<T>)hsObj;

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

							final SegmentNodePair<VerticalSegment<?>> prevVertPair = yToVerticalNodeMap.get(nodePosition.y);
							if (prevVertPair.segment.maxY < vs.maxY) {
								prevVertPair.segment = vs;
							}
						} else {
							final OrthogonalGraphNode newNode = new OrthogonalGraphNode(nodePosition);

							positionToNodesMap.put(nodePosition, newNode);
							yToVerticalNodeMap.put(newNode.position.y, new SegmentNodePair<>(vs, newNode));

							final SegmentNodePair<HorizontalSegment<?>> leftTestTuple = yToLeftNodeSegmentMap
									.put(nodePosition.y,
									new SegmentNodePair<>(hs, newNode));

							if (leftTestTuple != null
									&& leftTestTuple.segment.maxX >= hs.minX) {
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

	@SuppressWarnings("unchecked")
	static <T> Graph buildGraph(final OrthogonalSegments<T> segments) {
		// Create events from segments
		final List<Event<Object>> events = new ArrayList<>(
				segments.horizontalSegments.size() * 2 + segments.verticalSegments.size());

		for (final HorizontalSegment<T> hs : segments.horizontalSegments) {
			events.add(new Event<Object>(EventType.OPEN, hs.minX, hs));
			events.add(new Event<Object>(EventType.CLOSE, hs.maxX, hs));
		}

		for (final VerticalSegment<T> vs : segments.verticalSegments) {
			events.add(new Event<Object>(EventType.POINT, vs.x, vs));
		}

		Sweeper.sort(events);

		final TreeMultimap<Double, Object> openObjects = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());

		// TODO: rename
		final LineIntersectionCollector<Object> intersectionCollector = new LineIntersectionCollector<Object>();

		// TODO: Key function should be between the collector?
		Sweeper.sweep(events, openObjects, o -> ((HorizontalSegment<T>) o).y, intersectionCollector);

		intersectionCollector.finish();

		// TODO: Return entire map.. Will need it to lookup nodes
		return new Graph(intersectionCollector.positionToNodesMap.values());// nodes.stream().distinct().collect(ImmutableList.toImmutableList()));
		// return new Graph(Collections.emptySet());
	}
}
