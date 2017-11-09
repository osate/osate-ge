package org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep;

import com.google.common.collect.TreeMultimap;

/**
 * Interface for processing events at part of a sweep line algorithm.
 *
 * @param <Tag>
 * @param <OpenObjectEventTag>
 * @param <OpenTagKey>
 */
public interface LineSweeperEventHandler<Tag, OpenObjectEventTag, OpenTagKey> {
	/**
	 * Called for every event.
	 * @param event
	 * @param openObjects
	 */
	void handleEvent(final LineSweepEvent<Tag> event, final TreeMultimap<OpenTagKey, OpenObjectEventTag> openObjects);
}