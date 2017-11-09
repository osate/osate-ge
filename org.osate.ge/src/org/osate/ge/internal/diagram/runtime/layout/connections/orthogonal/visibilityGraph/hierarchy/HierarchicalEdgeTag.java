package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalSegmentsFactoryDataSource;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.EdgeTagCreator;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.Rectangle;

/**
 * Class used for a tag for edges when working with hierarchical graphs.
 *
 */
public class HierarchicalEdgeTag {
	private final int levelsCrossed;

	public HierarchicalEdgeTag(int depth) {
		this.levelsCrossed = depth;
	}

	public final int getLevelsCrossed() {
		return levelsCrossed;
	}

	/**
	 * Creates an edge tag creator which creates a HierarchicalEdgeTag object. The depth of the created object is set depending on whether the node crosses
	 * hierarchical bounds.
	 * @param ds
	 * @return
	 */
	public static <SegmentTag, NodeTag extends HierarchicalNodeTag<SegmentTag>, EdgeTag> EdgeTagCreator<NodeTag, HierarchicalEdgeTag> createHierarchicalEdgeTagCreator(
			final OrthogonalSegmentsFactoryDataSource<SegmentTag> ds) {
		return (n1, n2) -> {
			int levelsCrossed = 0;
			// If the nodes do not have the same container, sets the number of levels crossed based on the depth of the nodes.
			if (n1.getTag().getContainer() != n2.getTag().getContainer()) {
				levelsCrossed = Math.abs(n1.getTag().getDepth() - n2.getTag().getDepth());
			} else {
				// If the nodes are on the border to the same element, check if the midpoint of the edge is within the element.
				if (n1.getTag().getBorderElement() != null && n1.getTag().getBorderElement() == n2.getTag().getBorderElement()) {
					final Rectangle borderBounds = ds.getBounds(n1.getTag().getBorderElement());
					final Point midpoint =
							new Point((n1.getPosition().x + n2.getPosition().x) / 2.0, (n1.getPosition().y + n2.getPosition().y) / 2.0);
					if (borderBounds.contains(midpoint) && !borderBounds.borderContains(midpoint)) {
						levelsCrossed = 1;
					}
				}
			}

			return new HierarchicalEdgeTag(levelsCrossed);
		};
	}
}
