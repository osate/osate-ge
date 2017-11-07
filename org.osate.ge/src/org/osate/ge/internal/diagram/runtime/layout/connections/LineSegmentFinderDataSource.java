package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.List;

import org.osate.ge.graphics.Point;

public interface LineSegmentFinderDataSource<T> {
	// TODO: Rename to Element?
	List<T> getObjects();

	T getParent(T obj);

	Point getPosition(T obj);

	/**
	 * Returns null if the object does not have a bounds.
	 * @param obj
	 * @return
	 */
	Rectangle getBounds(T obj);

}
