package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

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
		protected final LineSegmentFinderDataSource<ObjectType> ds;
		final Function<ObjectType, Double> keyFunc;

		public AbstractSegmentCollector(final LineSegmentFinderDataSource<ObjectType> ds,
				final Function<ObjectType, Double> keyFunc) {
			this.ds = Objects.requireNonNull(ds, "ds must not be null");
			this.keyFunc = Objects.requireNonNull(keyFunc, "keyFunc must not be null");
		}

		@Override
		public void handleEvent(final Event<ObjectType> event, final TreeMultimap<Double, ObjectType> openObjects) {
			final ObjectType eventTagObject = event.tag; // TODO: Rename
			final ObjectType parentEventTagObject = ds.getParent(eventTagObject); // TODO: Rename

			// TODO: Document. This is for deciding which ancestors to ignore. The results are different based on whether the object is a point or has a parent
			// which is a port.
			// TODO: Rename?
			final Rectangle segmentBounds = ds.getSegmentBounds(eventTagObject);
			final ObjectType parent = parentEventTagObject;
			final ObjectType grandparent = parent == null ? null : ds.getParent(parent);

			// Find the first before object which is not the event object
			ObjectType before = null;
			for (final Double tmpKey : openObjects.keySet().headSet(keyFunc.apply(eventTagObject), true)
					.descendingSet()) {
				boolean found = false;

				final NavigableSet<ObjectType> tmpObjects = openObjects.get(tmpKey);
				for (final ObjectType tmp : tmpObjects) {
					// TODO: Think about implications of parent checks
					if (tmp != eventTagObject && tmp != parent && tmp != grandparent) {
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

			// Find the first after object which is not the event object
			ObjectType after = null;
			for (final Double tmpKey : openObjects.keySet().tailSet(keyFunc.apply(eventTagObject), true)) {
				boolean found = false;
				final NavigableSet<ObjectType> tmpObjects = openObjects.get(tmpKey);
				// TODO: Think about these checks
				for (final ObjectType tmp : tmpObjects) {
					if (tmp != eventTagObject && tmp != parent && tmp != grandparent) {
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
			createSegment(event.position, eventTagObject, segmentBounds, beforeBounds, afterBounds);
		}

		// TODO: Does value need to be a rect or change it just be a point?
		// TODO: Rename
		protected abstract void createSegment(final double position, final ObjectType eventTag,
				final Rectangle maxSegmentBounds, final Rectangle before,
				final Rectangle after);
	}

	private static class MinVerticalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<VerticalSegment<ObjectType>> segments;

		public MinVerticalSegmentCollector(final LineSegmentFinderDataSource<ObjectType> ds,
				final Set<VerticalSegment<ObjectType>> segments) {
			super(ds, o -> ds.getBounds(o).min.y);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void createSegment(final double position, final ObjectType eventTag, final Rectangle maxSegmentBounds,
				final Rectangle before,
				final Rectangle after) {
			final Rectangle eventTagBounds = ds.getBounds(eventTag);
			double min = before == null ? Double.NEGATIVE_INFINITY : before.max.y;

			// If value is inside of before
			if (min > eventTagBounds.min.y) {
				min = before.min.y;
			}

			min = Math.max(min, maxSegmentBounds.min.y);

			// Clamp max since we are dealing with mins..
			final double max = Math.min(
					Math.min(after == null ? Double.POSITIVE_INFINITY : after.min.y, eventTagBounds.max.y),
					maxSegmentBounds.max.y);

			segments.add(new VerticalSegment<>(position, min, max, eventTag));
		}
	};

// TODO: Cleanup.. Try to share code..
	private static class MaxVerticalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<VerticalSegment<ObjectType>> segments;

		public MaxVerticalSegmentCollector(final LineSegmentFinderDataSource<ObjectType> ds,
				final Set<VerticalSegment<ObjectType>> segments) {
			super(ds, o -> ds.getBounds(o).max.y);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void createSegment(final double position, final ObjectType eventTag, final Rectangle maxSegmentBounds,
				final Rectangle before,
				final Rectangle after) {
			final Rectangle eventTagBounds = ds.getBounds(eventTag);
			final double min = Math.max(
					Math.max(before == null ? Double.NEGATIVE_INFINITY : before.max.y, eventTagBounds.min.y),
					maxSegmentBounds.min.y);

			// Clamp max since we are dealing with mins..
			double max = after == null ? Double.POSITIVE_INFINITY : after.min.y;

			// If value is inside of before
			if (max < eventTagBounds.max.y) {
				max = after.max.y;
			}

			max = Math.min(max, maxSegmentBounds.max.y);

			segments.add(new VerticalSegment<>(position, min, max, eventTag));
		}
	};

	private static class MinHorizontalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<HorizontalSegment<ObjectType>> segments;

		public MinHorizontalSegmentCollector(final LineSegmentFinderDataSource<ObjectType> ds,
				final Set<HorizontalSegment<ObjectType>> segments) {
			super(ds, o -> ds.getBounds(o).min.x);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void createSegment(final double position, final ObjectType eventTag, final Rectangle maxSegmentBounds,
				final Rectangle before,
				final Rectangle after) {
			final Rectangle eventTagBounds = ds.getBounds(eventTag);
			double min = before == null ? Double.NEGATIVE_INFINITY : before.max.x;

			// If value is inside of before
			if (min > eventTagBounds.min.x) {
				min = before.min.x;
			}

			min = Math.max(min, maxSegmentBounds.min.x);

			// Clamp max since we are dealing with mins..
			final double max = Math.min(
					Math.min(after == null ? Double.POSITIVE_INFINITY : after.min.x, eventTagBounds.max.x),
					maxSegmentBounds.max.x);

			segments.add(new HorizontalSegment<>(position, min, max, eventTag));
		}
	};

	private static class MaxHorizontalSegmentCollector<ObjectType> extends AbstractSegmentCollector<ObjectType> {
		private final Set<HorizontalSegment<ObjectType>> segments;

		public MaxHorizontalSegmentCollector(final LineSegmentFinderDataSource<ObjectType> ds,
				final Set<HorizontalSegment<ObjectType>> segments) {
			super(ds, o -> ds.getBounds(o).max.x);
			this.segments = Objects.requireNonNull(segments, "segments must not be null");
		}

		@Override
		protected void createSegment(final double position, final ObjectType eventTag, final Rectangle maxSegmentBounds,
				final Rectangle before,
				final Rectangle after) {
			final Rectangle eventTagBounds = ds.getBounds(eventTag);
			final double min = Math.max(
					Math.max(before == null ? Double.NEGATIVE_INFINITY : before.max.x, eventTagBounds.min.x),
					maxSegmentBounds.min.x);

			// Clamp max since we are dealing with mins..
			double max = after == null ? Double.POSITIVE_INFINITY : after.min.x;

			// If value is inside of before
			if (max < eventTagBounds.max.x) {
				max = after.max.x;
			}

			max = Math.min(max, maxSegmentBounds.max.x);

			segments.add(new HorizontalSegment<>(position, min, max, eventTag));
		}
	};
// END X

// TODO: What is needed... Bounds of ALL the objects and connection points.. In order to make an event list.
// TODO: Does the data source really need the concept of children?
	public final static <T> Set<VerticalSegment<T>> findVerticalSegments(LineSegmentFinderDataSource<T> ds) {
		final List<Sweeper.Event<T>> events = new ArrayList<>();
		addEventsForHorizontalSweep(ds, events);
		Sweeper.sort(events);

//		// TODO: What if two objects have the same min/max y? Only one object will be inserted?
//
		final Set<VerticalSegment<T>> verticalSegments = new HashSet<>();
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
	private static <T> void addEventsForHorizontalSweep(final LineSegmentFinderDataSource<T> ds,
			final Collection<Sweeper.Event<T>> events) {
		for (final T child : ds.getObjects()) {
			final Rectangle childBounds = ds.getBounds(child);
			if (childBounds.min.x != childBounds.max.x) {
				events.add(new Event<T>(EventType.OPEN, childBounds.min.x, child));
				events.add(new Event<T>(EventType.CLOSE, childBounds.max.x, child));
			} else {
				events.add(new Event<T>(EventType.POINT, childBounds.min.x, child));
			}
		}
	}

	public final static <T> Set<HorizontalSegment<T>> findHorizontalSegments(LineSegmentFinderDataSource<T> ds) {
		final List<Sweeper.Event<T>> events = new ArrayList<>();
		addEventsForVerticalSweep(null, ds, events);
		Sweeper.sort(events);

		// TODO: What if two objects have the same min/max x? Only one object will be inserted?
		// TODO: Reenable
		final Set<HorizontalSegment<T>> horizontalSegments = new HashSet<>();
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
		// TODO: Use the function which is part of the collector.
		Sweeper.sweep(events, maxXOpenObjects, o -> ds.getBounds(o).max.x, maxHorizontalCollector);

		return horizontalSegments;
	}

// TODO: Share implementation with other horizontal sweep. Only difference is the axis
	private static <T> void addEventsForVerticalSweep(final T parent, final LineSegmentFinderDataSource<T> ds,
			final Collection<Sweeper.Event<T>> events) {
		for (final T child : ds.getObjects()) {
			final Rectangle childBounds = ds.getBounds(child);
			if (childBounds.min.y != childBounds.max.y) {
				events.add(new Event<T>(EventType.OPEN, childBounds.min.y, child));
				events.add(new Event<T>(EventType.CLOSE, childBounds.max.y, child));
			} else {
				events.add(new Event<T>(EventType.POINT, childBounds.min.y, child));
			}
		}
	}

	// TODO: Should be moved outside of this class?
	static <T> OrthogonalSegments<T> buildSegments(final LineSegmentFinderDataSource<T> ds) {
		final Set<VerticalSegment<T>> verticalSegments = findVerticalSegments(ds);
		final Set<HorizontalSegment<T>> horizontalSegments = findHorizontalSegments(ds);

		// Create segments object
		return new OrthogonalSegments(Collections.unmodifiableSet(horizontalSegments),
				Collections.unmodifiableSet(verticalSegments));
	}

// TODO: May need a better way to get the cp segment bounds... Decide after seeing how things work.

	public static void main(String[] args) {
		final LineSegmentFinderDataSource<TestModel.TestElement> ds = TestModel.createDataSource();

		// Vertical
		final Set<VerticalSegment<TestModel.TestElement>> verticalSegments = findVerticalSegments(ds);
		System.out.println("Vertical Segment Count: " + verticalSegments.size());
		for (final VerticalSegment<?> vs : verticalSegments) {
			System.out.println(vs.x + " : " + vs.minY + " -> " + vs.maxY);
		}

		// Horizontal
		final Set<HorizontalSegment<TestModel.TestElement>> horizontalSegments = findHorizontalSegments(ds);
		System.out.println("Horizontal Segment Count: " + horizontalSegments.size());
		for (final HorizontalSegment<?> hs : horizontalSegments) {
			System.out.println(hs.y + " : " + hs.minX + " -> " + hs.maxX);
		}
	}
}
