package org.osate.ge.internal.diagram.runtime.layout;

import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.Dimension;

/**
 * Provides information needed to layout a diagram which is not contained in the runtime diagram data structure.
 *
 */
public interface LayoutInfoProvider {
	/**
	 *
	 * @param de
	 * @return null if a size cannot be determined.
	 */
	Dimension getPrimaryLabelSize(DiagramElement de);
}
