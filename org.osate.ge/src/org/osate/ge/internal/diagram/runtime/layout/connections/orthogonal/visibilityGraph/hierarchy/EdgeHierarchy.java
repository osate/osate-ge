package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalSegmentsFactoryDataSource;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.Rectangle;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.EdgeTagCreator;

// TODO: Rename
public class EdgeHierarchy {
	public final int depth; // TODO: Rename

	public EdgeHierarchy(int depth) {
		this.depth = depth;
	}

	public static <SegmentTag, NodeTag extends NodeHierarchy<SegmentTag>, EdgeTag> EdgeTagCreator<NodeTag, EdgeHierarchy> createEdgeHierarchyCreator(
			final OrthogonalSegmentsFactoryDataSource<SegmentTag> ds) {
		return (n1, n2) -> {
			final int depth;
			if (n1.tag.container != n2.tag.container) {
				depth = Math.abs(n1.tag.depth - n2.tag.depth);
			} else {
				if (n1.tag.borderElement != null && n1.tag.borderElement == n2.tag.borderElement) {
					final Rectangle borderBounds = ds.getBounds(n1.tag.borderElement);
					final Point midpoint =
							new Point((n1.position.x + n2.position.x) / 2.0, (n1.position.y + n2.position.y) / 2.0);
					if (borderBounds.contains(midpoint) && !borderBounds.borderContains(midpoint)) {
						depth = 1;
					} else {
						depth = 0;
					}
				} else {
					depth = 0;
				}
			}

			return new EdgeHierarchy(depth);
		};
	}
}
