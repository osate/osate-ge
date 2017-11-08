package org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.TreeMultimap;

//TODO:Rename? Describe
public class LineSweeper<Tag> {
	public static enum EventType {
		OPEN, CLOSE, POINT
	}

	// TODO: Cleanup documentation and terminology.. Axis, etc.
	// TODO: Tag and OpenObjectsTag must be related
	/**
	 *
	 * @param events must be such that iterating through it will produce events in order.
	 */
	// TODO: getObjectKey.. Only needed for object that will be opened/closed? Need to document. Could be part of event but then a new set would have be created
	// each time..
	// But wouldn't have to call function to get.. Is it just called once per thing?
	// TODO: Consider going back to a special type for open objects...
	public static <Tag> void sweep(final List<LineSweepEvent<Tag>> events, final TreeMultimap<Double, Tag> openObjects, // TODO: use interface type
			final Function<Tag, Double> getObjectKey,
			final LineSweeperEventHandler<Tag> resultCollector) {
		// TODO: Cleanup variable names
		for (int i = 0; i < events.size(); i++) {
			final double position = events.get(i).position;

			// Find the index of the first event with a different position. This is needed to properly handle multiple events at the same position.
			int indexForNextLoopIteration;
			for (indexForNextLoopIteration = i + 1; indexForNextLoopIteration < events
					.size(); indexForNextLoopIteration++) {
				final LineSweepEvent<Tag> tmp = events.get(indexForNextLoopIteration);
				if (tmp.position != position) {
					break;
				}
			}

			// Add objects to open objects collection based on open events.
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final LineSweepEvent<Tag> tmp = events.get(j);
				if (tmp.type == EventType.OPEN) {
					openObjects.put(getObjectKey.apply(tmp.tag), tmp.tag);
				}
			}

			// Process the event
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final LineSweepEvent<Tag> tmp = events.get(j);
				resultCollector.handleEvent(tmp, openObjects);
			}


			// Remove objects to open objects collection based on close events.
			for (int j = i; j < indexForNextLoopIteration; j++) {
				final LineSweepEvent<Tag> tmp = events.get(j);
				if (tmp.type == EventType.CLOSE) {
					openObjects.remove(getObjectKey.apply(tmp.tag), tmp.tag);
				}
			}

			i = indexForNextLoopIteration - 1;
		}
	}

	public static <Tag, OpenObjectsTag> void sort(final List<LineSweepEvent<OpenObjectsTag>> events) {
		events.sort(EVENT_COMPARATOR);
	}

	@SuppressWarnings("rawtypes")
	public final static Comparator<LineSweepEvent> EVENT_COMPARATOR = (e1, e2) -> Double.compare(e1.position, e2.position);

}
