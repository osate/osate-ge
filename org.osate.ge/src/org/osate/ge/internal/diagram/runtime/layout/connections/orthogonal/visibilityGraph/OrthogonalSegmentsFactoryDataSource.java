package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph;

import java.util.List;

public interface OrthogonalSegmentsFactoryDataSource<ElementType> {
	/**
	 * Returns the elements provided by this data source. Each element will be turned into one or more events and then processed
	 * by the OrthogonalSegmentsFactory based on the values returned by the methods of this interface.
	 * @return
	 */
	List<ElementType> getElements();

	ElementType getParent(ElementType e);

	/**
	 * For points the min and max of the bounds should be equal.
	 * @param e
	 * @return
	 */
	Rectangle getBounds(ElementType e);

	/**
	 * Returns a bounds to which the segments generated for this element are constrained. Useful to prevent how far segments generated
	 * by connection points can extend.
	 * @param e
	 * @return must not return null.
	 */
	Rectangle getSegmentBounds(ElementType e);

}
