package org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep;

import com.google.common.collect.TreeMultimap;

// TODO: Rename
// TODO: Rename methods
// TODO: Describe
public interface LineSweeperEventHandler<Tag> {
	void handleEvent(final LineSweepEvent<Tag> event, final TreeMultimap<Double, Tag> openObjects);
}