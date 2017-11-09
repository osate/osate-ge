package org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep;

import com.google.common.collect.TreeMultimap;

/**
 * Interface for processing events at part of a sweep line algorithm.
 *
 * @param <EventTag>
 * @param <OpenObjectEventTag>
 * @param <OpenTagKey>
 */
public interface LineSweeperEventHandler<EventTag, OpenObjectEventTag, OpenTagKey> {
	/**
	 * Called for every event.
	 * @param event
	 * @param openObjects
	 */
	void handleEvent(final LineSweepEvent<EventTag> event, final TreeMultimap<OpenTagKey, OpenObjectEventTag> openObjects);
}