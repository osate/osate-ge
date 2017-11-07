package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.List;

import org.osate.ge.graphics.Point;

// TODO: Rename to model
// TODO: Is children really needed? Just flat information should be enough?

//TODO: Unify connection points and children... Possible to just treat them all as the same?
//TODO: Connection Point and rects have different methods.. They should be different types.. correct? However they should have the same base type?
// TODO: Is there a reasonable expectation of being able to implement this interface with existing classes. If not, just create a data model and avoid the data source concept?
// TODO: Considering points are not represented as separate objects, it could be difficult.

public interface OrthogonalVisibilityGraphDataSource<T> {
	T getParent(final T o);

	// All children are assumed to be contained in the bounding box of their parent.
	List<? extends T> getChildren(final T o);

	int getNumberOfConnectionPoints(final T o);

	// TODO: Need concept of direction in points? Or some way to avoid collision with owner.
	// TODO: Rename to getConnectionPointPosition?
	T getConnectionPoint(final T o, final int index);

	Point getConnectionPointPosition(final T o);

	// TODO: Rename. Still need?
	/**
	 *
	 * @param o
	 * @param index
	 * @return a null result means that the segments should be unconstrained.
	 */
	Rectangle getConnectionPointSegmentBounds(final T o, final int index);

	Rectangle getBounds(final T o);
}