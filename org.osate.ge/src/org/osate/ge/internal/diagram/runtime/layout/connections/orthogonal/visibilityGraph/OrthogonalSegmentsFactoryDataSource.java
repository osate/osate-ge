package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph;

import java.util.List;

public interface OrthogonalSegmentsFactoryDataSource<T> {
	// TODO: Rename to Element?
	List<T> getObjects();

	T getParent(T obj);

	/**
	 * For points the min and max of the bounds should be equal.
	 * @param obj
	 * @return
	 */
	Rectangle getBounds(T obj);

	// TODO: Document
	// Must not return null
	Rectangle getSegmentBounds(T obj);

}
