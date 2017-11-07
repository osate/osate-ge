package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.TreeMultimap;

//TODO:Rename? Describe
public class Sweeper<Tag> {
	public static enum EventType {
		OPEN, CLOSE, POINT
	}

	public static class Event<Tag> {
		public final EventType type;

		public final double position; // Position along the axis being swept. For a sweep using a vertical line, this would be the x-value of the object.

		public final Tag tag; // TODO: Rename tag?

		// TODO: Should tag be the last argument?
		public Event(final EventType type, final double position, final Tag tag) {
			this.type = Objects.requireNonNull(type, "type must not be null");
			this.position = position;
			this.tag = tag;
		}
	}

	// TODO: Rename
	// TODO: Rename methods
	// TODO: Describe
	static interface ResultCollector<Tag> {
		void handleEvent(final Event<Tag> event, final TreeMultimap<Double, Tag> openObjects);
	}

	// TODO: Cleanup documentation and terminology.. Axis, etc.
	// TODO: Tag and OpenObjectsTag must be related
	/**
	 *
	 * @param events must be such that iterating through it will produce events in order.
	 */
	public static <Tag> void sweep(final List<Event<Tag>> events, final TreeMultimap<Double, Tag> openObjects, // TODO: use interface type
			final Function<Tag, Double> getObjectKey,
			final ResultCollector<Tag> resultCollector) {
		// TODO: Cleanup variable names
		for (int i = 0; i < events.size(); i++) {
			final double position = events.get(i).position;

			// Find the index of the first event with a different position. This is needed to properly handle multiple events at the same position.
			int indexForNextLoopIteration;
			for (indexForNextLoopIteration = i + 1; indexForNextLoopIteration < events
					.size(); indexForNextLoopIteration++) {
				final Event<Tag> tmp = events.get(indexForNextLoopIteration);
				if (tmp.position != position) {
					break;
				}
			}

			// Add objects to open objects collection based on open events.
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final Event<Tag> tmp = events.get(j);
				if (tmp.type == EventType.OPEN) {
					openObjects.put(getObjectKey.apply(tmp.tag), tmp.tag);
				}
			}

			// Process the event
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final Event<Tag> tmp = events.get(j);
				resultCollector.handleEvent(tmp, openObjects);
			}


			// Remove objects to open objects collection based on close events.
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final Event<Tag> tmp = events.get(j);
				if (tmp.type == EventType.CLOSE) {
					openObjects.remove(getObjectKey.apply(tmp.tag), tmp.tag);
				}
			}

			i = indexForNextLoopIteration - 1;
		}
	}

	public static <Tag, OpenObjectsTag> void sort(final List<Event<OpenObjectsTag>> events) {
		events.sort(EVENT_COMPARATOR);
	}

	@SuppressWarnings("rawtypes")
	public final static Comparator<Event> EVENT_COMPARATOR = (e1, e2) -> Double.compare(e1.position, e2.position);

}
