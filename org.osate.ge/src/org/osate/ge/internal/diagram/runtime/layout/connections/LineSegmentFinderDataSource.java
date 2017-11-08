package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.List;

public interface LineSegmentFinderDataSource<T> {
	// TODO: Rename to Element?
	List<T> getObjects();

	T getParent(T obj);

	/**
	 * For points the min and max of the bounds should be equal.
	 * @param obj
	 * @return
	 */
	Rectangle getBounds(T obj);

	// TODO: Document what this affects
	/**
	 * The parent of a port must not be a port. Ports must not have any children.
	 * @param obj
	 * @return
	 */
	boolean isPort(T obj);

}
