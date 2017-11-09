package org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

/**
 * Implementation of a sweep line algorithm.
 */
public class LineSweeper {
	@SuppressWarnings("rawtypes")
	public final static Comparator<LineSweepEvent> EVENT_COMPARATOR = (e1, e2) -> Double.compare(e1.position,
			e2.position);

	public static enum EventType {
		OPEN, CLOSE, POINT
	}

	/**
	 * Processes the specified events in the provided order.
	 * When a open event is encountered, its tag is added to a multimap which is sorted by the key provided by openTagToKey then processed.
	 * When a closed event is encountered, it is processed then its tag is removed from the open tag map.
	 * When a point event is encountered, it is processed.
	 * All updates to the open tag collection for a particular position occur before/after processing any of the events at that position.
	 * @param events must be a pre-sorted list of events.
	 * @param openTagToKey
	 * @param resultCollector
	 */
	public static <EventTag, OpenEventTag extends EventTag, OpenTagKey extends Comparable<OpenTagKey>> void sweep(
			final List<LineSweepEvent<EventTag>> events,
			final Function<OpenEventTag, OpenTagKey> openTagToKey,
			final LineSweeperEventHandler<EventTag, OpenEventTag, OpenTagKey> resultCollector) {

		final TreeMultimap<OpenTagKey, OpenEventTag> openTags = TreeMultimap.create(Comparator.naturalOrder(),
				Ordering.arbitrary());

		for (int i = 0; i < events.size(); i++) {
			final double position = events.get(i).position;

			// Find the index of the first event with a different position. This is needed to properly handle multiple events at the same position.
			int indexForNextLoopIteration;
			for (indexForNextLoopIteration = i + 1; indexForNextLoopIteration < events
					.size(); indexForNextLoopIteration++) {
				final LineSweepEvent<EventTag> tmp = events.get(indexForNextLoopIteration);
				if (tmp.position != position) {
					break;
				}
			}

			// Add objects to open objects collection based on open events.
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final LineSweepEvent<EventTag> tmp = events.get(j);
				if (tmp.type == EventType.OPEN) {
					@SuppressWarnings("unchecked")
					final OpenEventTag openTag = (OpenEventTag) tmp.tag;
					openTags.put(openTagToKey.apply(openTag), openTag);
				}
			}

			// Process the event
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final LineSweepEvent<EventTag> tmp = events.get(j);
				resultCollector.handleEvent(tmp, openTags);
			}

			// Remove objects to open objects collection based on close events.
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final LineSweepEvent<EventTag> tmp = events.get(j);
				if (tmp.type == EventType.CLOSE) {
					@SuppressWarnings("unchecked")
					final OpenEventTag closeTag = (OpenEventTag) tmp.tag;
					openTags.remove(openTagToKey.apply(closeTag), closeTag);
				}
			}

			i = indexForNextLoopIteration - 1;
		}
	}

	/**
	 * Sorts events based on position.
	 * @param events
	 */
	public static <Tag, OpenObjectsTag> void sortByPosition(final List<LineSweepEvent<OpenObjectsTag>> events) {
		events.sort(EVENT_COMPARATOR);
	}
}
