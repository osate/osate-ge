package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.HorizontalSegment;
import org.osate.ge.internal.diagram.runtime.layout.connections.OrthogonalVisibilityGraphBuilder.VerticalSegment;
import org.osate.ge.internal.diagram.runtime.layout.connections.Sweeper.Event;
import org.osate.ge.internal.diagram.runtime.layout.connections.Sweeper.EventType;
import org.osate.ge.internal.diagram.runtime.layout.connections.Sweeper.ResultCollector;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

// TODO: Rename and Describe
// TODO: Turn into interface so implementation scan be swapped?

// TODO: TreeSet comparison may cause problems. Objects with the same min/max y will be considered the same? Determine how to solve
// TODO: Implement connection point
// TODO: Simplify collector code..
// TODO: Share code between collectors

public class LineSegmentFinder {
	// TODO: Rename
	private static abstract class AbstractSegmentCollector<ObjectType> implements ResultCollector<ObjectType> {
		private final OrthogonalVisibilityGraphDataSource<ObjectType> ds;
		final Function<ObjectType, Double> keyFunc;

		public AbstractSegmentCollector(final OrthogonalVisibilityGraphDataSource<ObjectType> ds,
				final Function<ObjectType, Double> keyFunc) {
			this.ds = Objects.requireNonNull(ds, "ds must not be null");
			this.keyFunc = Objects.requireNonNull(keyFunc, "keyFunc must not be null");
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleEvent(final Event<ObjectType> event, final TreeMultimap<Double, ObjectType> openObjects) {
			if (event.type == EventType.POINT) {
				// Connection Point
				// TODO
			} else {
				final ObjectType eventTagObject = event.tag; // TODO: Rename
				final ObjectType parentEventTagObject = ds.getParent(eventTagObject); // TODO: Rename
				final Rectangle tagBounds = ds.getBounds(eventTagObject);

				// Find the first before object which is not the event object
				ObjectType before = null;
				for (final Double tmpKey : openObjects.keySet().headSet(keyFunc.apply(eventTagObject), true)
						.descendingSet()) {
					boolean found = false;

					final NavigableSet<ObjectType> tmpObjects = openObjects.get(tmpKey);
					for (final ObjectType tmp : tmpObjects) {
						// TODO: Think about implications of parent checks
						if (tmp != eventTagObject
								&& tmp != parentEventTagObject) {
							before = tmp;
							found = true;
							break;
						}
					}

					// TODO: Cleanup
					if (found) {
						break;
					}
				}

				final Rectangle beforeBounds = before == null ? null : ds.getBounds(before);
				before(event.position, tagBounds, beforeBounds);

				// Find the first after object which is not the event object
				ObjectType after = null;
				// for (final ObjectType tmp : openObjects.tailSet(eventTagObject, true)) {
				for (final Double tmpKey : openObjects.keySet().tailSet(keyFunc.apply(eventTagObject), true)) {
					boolean found = false;
					final NavigableSet<ObjectType> tmpObjects = openObjects.get(tmpKey);
					// TODO: Think about these checks
					for (final ObjectType tmp : tmpObjects) {
						if (tmp != eventTagObject
								&& tmp != parentEventTagObject) {
							after = tmp;
							found = true;
							break;
						}
					}

					// TODO: Cleanup
					if (found) {
						break;
					}
				}

				final Rectangle afterBounds = after == null ? null : ds.getBounds(after);
				after(event.position, tagBounds, afterBounds);
			}
		}

		// TODO: Does value need to be a rect or change it just be a point?
		protected abstract void before(final double position, final Rectangle value, final Rectangle before);

		protected abstract void after(final double position, final Rectangle value, final Rectangle before);
	}

	private static class MinVerticalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<VerticalSegment> segments;

		public MinVerticalSegmentCollector(final OrthogonalVisibilityGraphDataSource<ObjectType> ds,
				final Set<VerticalSegment> segments) {
			super(ds, o -> ds.getBounds(o).min.y);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void before(final double position, final Rectangle value, final Rectangle before) {
			double min = before == null ? Double.NEGATIVE_INFINITY : before.max.y;

			// If value is inside of before
			if (min > value.min.y) {
				min = before.min.y;
			}

			segments.add(new VerticalSegment(position, min, value.min.y));
		}

		@Override
		protected void after(final double position, final Rectangle value, final Rectangle after) {
			// Clamp max since we are dealing with mins..
			final double max = Math.min(after == null ? Double.POSITIVE_INFINITY : after.min.y, value.max.y);
			segments.add(new VerticalSegment(position, value.min.y, max));
		}
	};

	// TODO: Cleanup.. Try to share code..
	private static class MaxVerticalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<VerticalSegment> segments;

		public MaxVerticalSegmentCollector(final OrthogonalVisibilityGraphDataSource<ObjectType> ds,
				final Set<VerticalSegment> segments) {
			super(ds, o -> ds.getBounds(o).max.y);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void before(final double position, final Rectangle value, final Rectangle before) {
			final double min = Math.max(before == null ? Double.NEGATIVE_INFINITY : before.max.y, value.min.y);
			segments.add(new VerticalSegment(position, min, value.max.y));
		}

		@Override
		protected void after(final double position, final Rectangle value, final Rectangle after) {

			// Clamp max since we are dealing with mins..
			double max = after == null ? Double.POSITIVE_INFINITY : after.min.y;

			// If value is inside of before
			if (max < value.max.y) {
				max = after.max.y;
			}

			segments.add(new VerticalSegment(position, value.max.y, max));
		}
	};

	// TODO
	/////////////////////////////////////////// X
	private static class MinHorizontalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<HorizontalSegment> segments;

		public MinHorizontalSegmentCollector(final OrthogonalVisibilityGraphDataSource<ObjectType> ds,
				final Set<HorizontalSegment> segments) {
			super(ds, o -> ds.getBounds(o).min.x);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void before(final double position, final Rectangle value, final Rectangle before) {
			double min = before == null ? Double.NEGATIVE_INFINITY : before.max.x;

			// If value is inside of before
			if (min > value.min.x) {
				min = before.min.x;
			}

			segments.add(new HorizontalSegment(position, min, value.min.x));
		}

		@Override
		protected void after(final double position, final Rectangle value, final Rectangle after) {
			// Clamp max since we are dealing with mins..
			final double max = Math.min(after == null ? Double.POSITIVE_INFINITY : after.min.x, value.max.x);
			segments.add(new HorizontalSegment(position, value.min.x, max));
		}
	};

	// TODO: Cleanup.. Try to share code..
	private static class MaxHorizontalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<HorizontalSegment> segments;

		public MaxHorizontalSegmentCollector(final OrthogonalVisibilityGraphDataSource<ObjectType> ds,
				final Set<HorizontalSegment> segments) {
			super(ds, o -> ds.getBounds(o).max.x);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void before(final double position, final Rectangle value, final Rectangle before) {
			final double min = Math.max(before == null ? Double.NEGATIVE_INFINITY : before.max.x, value.min.x);
			segments.add(new HorizontalSegment(position, min, value.max.x));
		}

		@Override
		protected void after(final double position, final Rectangle value, final Rectangle after) {

			// Clamp max since we are dealing with mins..
			double max = after == null ? Double.POSITIVE_INFINITY : after.min.x;

			// If value is inside of before
			if (max < value.max.x) {
				max = after.max.x;
			}

			segments.add(new HorizontalSegment(position, value.max.x, max));
		}
	};
	// END X

	// TODO: What is needed... Bounds of ALL the objects and connection points.. In order to make an event list.
	// TODO: Does the data source really need the concept of children?
	public final static <T> Set<VerticalSegment> findVerticalSegments(OrthogonalVisibilityGraphDataSource<T> ds) {
		final List<Sweeper.Event<T>> events = new ArrayList<>();
		addEventsForHorizontalSweep(null, ds, events);
		Sweeper.sort(events);

//		// TODO: What if two objects have the same min/max y? Only one object will be inserted?
//
		final Set<VerticalSegment> verticalSegments = new HashSet<>();
		final TreeMultimap<Double, T> minYOpenObjects = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());
		final MinVerticalSegmentCollector<T> minVerticalCollector = new MinVerticalSegmentCollector<>(ds,
				verticalSegments);
		Sweeper.sweep(events, minYOpenObjects, minVerticalCollector.keyFunc, minVerticalCollector);

		final TreeMultimap<Double, T> maxYOpenObjects = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());
		final MaxVerticalSegmentCollector<T> maxVerticalCollector = new MaxVerticalSegmentCollector<>(ds,
				verticalSegments);
		Sweeper.sweep(events, maxYOpenObjects, maxVerticalCollector.keyFunc, maxVerticalCollector);

		// TODO: Do this for max as well... That will finish collecting all the vertical segments

		return verticalSegments;
	}

	// TODO: A stream would be better to avoid having to store everything in an array
	private static <T> void addEventsForHorizontalSweep(final T parent,
			final OrthogonalVisibilityGraphDataSource<T> ds,
			final Collection<Sweeper.Event<T>> events) {
		for (final T child : ds.getChildren(parent)) {
			final Rectangle childBounds = ds.getBounds(child);
			// TODO: Add events
			events.add(new Event<T>(EventType.OPEN, childBounds.min.x, child));
			events.add(new Event<T>(EventType.CLOSE, childBounds.max.x, child));

			final int numberOfConnectionPoints = ds.getNumberOfConnectionPoints(child);
			for (int cpIndex = 0; cpIndex < numberOfConnectionPoints; cpIndex++) {
				final T cp = ds.getConnectionPoint(child, cpIndex);
				events.add(new Event<T>(EventType.POINT, ds.getConnectionPointPosition(cp).x, cp));
			}

			addEventsForHorizontalSweep(child, ds, events);
		}
	}

	public final static <T> Set<HorizontalSegment> findHorizontalSegments(OrthogonalVisibilityGraphDataSource<T> ds) {
		final List<Sweeper.Event<T>> events = new ArrayList<>();
		addEventsForVerticalSweep(null, ds, events);
		Sweeper.sort(events);

		// TODO: What if two objects have the same min/max x? Only one object will be inserted?
		// TODO: Reenable
		final Set<HorizontalSegment> horizontalSegments = new HashSet<>();
		final TreeMultimap<Double, T> minXOpenObjects = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());
		final MinHorizontalSegmentCollector<T> minHorizontalCollector = new MinHorizontalSegmentCollector<T>(ds,
				horizontalSegments);
		// TODO: Avoid passing function in.. Should be part of collector? or some model?
		Sweeper.sweep(events, minXOpenObjects, minHorizontalCollector.keyFunc, minHorizontalCollector);

		final TreeMultimap<Double, T> maxXOpenObjects = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());
		final MaxHorizontalSegmentCollector<T> maxHorizontalCollector = new MaxHorizontalSegmentCollector<T>(ds,
				horizontalSegments);
		Sweeper.sweep(events, maxXOpenObjects, o -> ds.getBounds(o).max.x, maxHorizontalCollector);

		return horizontalSegments;
	}

	// TODO: A stream would be better to avoid having to store everything in an array
	private static <T> void addEventsForVerticalSweep(final T parent, final OrthogonalVisibilityGraphDataSource<T> ds,
			final Collection<Sweeper.Event<T>> events) {
		for (final T child : ds.getChildren(parent)) {
			final Rectangle childBounds = ds.getBounds(child);
			// TODO: Add events
			events.add(new Event<T>(EventType.OPEN, childBounds.min.y, child));
			events.add(new Event<T>(EventType.CLOSE, childBounds.max.y, child));

			final int numberOfConnectionPoints = ds.getNumberOfConnectionPoints(child);
			for (int cpIndex = 0; cpIndex < numberOfConnectionPoints; cpIndex++) {
				final T cp = ds.getConnectionPoint(child, cpIndex);
				events.add(new Event<T>(EventType.POINT, ds.getConnectionPointPosition(cp).y, cp));
			}

			addEventsForVerticalSweep(child, ds, events);
		}
	}

	// TODO: May need a better way to get the cp segment bounds... Decide after seeing how things work.

	public static void main(String[] args) {
		final OrthogonalVisibilityGraphDataSource<?> ds = TestModel.createDataSource();

		// Vertical
		final Set<VerticalSegment> verticalSegments = findVerticalSegments(ds);
		System.out.println("Vertical Segment Count: " + verticalSegments.size());
		for (final VerticalSegment vs : verticalSegments) {
			System.out.println(vs.x + " : " + vs.minY + " -> " + vs.maxY);
		}

		// Horizontal
		final Set<HorizontalSegment> horizontalSegments = findHorizontalSegments(ds);
		System.out.println("Horizontal Segment Count: " + horizontalSegments.size());
		for (final HorizontalSegment hs : horizontalSegments) {
			System.out.println(hs.y + " : " + hs.minX + " -> " + hs.maxX);
		}
	}
}
