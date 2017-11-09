package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalSegmentsFactoryDataSource;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.Rectangle;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.NodeTagCreator;

// TODO: Rename
public class NodeHierarchy<ModelElement> {
	public final ModelElement container;
	public final ModelElement borderElement; // Will only be set if the node is on the border of a model elmeent
	public final int depth;

	public NodeHierarchy(final ModelElement container, final ModelElement borderElement, final int depth) {
		this.container = container;
		this.borderElement = borderElement;
		this.depth = depth;
	}

	// TODO: Rename
	public static <SegmentTag> NodeTagCreator<SegmentTag, NodeHierarchy<SegmentTag>> createNodeHierarchyCreator(
			final OrthogonalSegmentsFactoryDataSource<SegmentTag> ds) {
		return (hs, vs) -> {
			final Point nodePoint = new Point(vs.x, hs.y); // TODO: Could pass this in?
			SegmentTag borderElement = null;

			// Start with the first common ancestor
			SegmentTag st = getFirstCommonAncestor(ds, hs.tag, vs.tag);
			if (st != null) {
				Rectangle bounds = ds.getBounds(st);

				// Skip over rectangles that are actually points. These are typically connection points and aren't considered their own level of hierarchy
				while (bounds.isPoint() && st != null) {
					st = ds.getParent(st);
					if (st != null) {
						bounds = ds.getBounds(st);
					}
				}

				// While the point is on the border of the current segment tag, go to the parent
				while (bounds.borderContains(nodePoint) && st != null) {
					borderElement = st;
					st = ds.getParent(st);
					if (st != null) {
						bounds = ds.getBounds(st);
					}
				}
			}

			return new NodeHierarchy<SegmentTag>(st, borderElement, getDepth(ds, st));
		};
	}

	private static <SegmentTag> SegmentTag getFirstCommonAncestor(
			final OrthogonalSegmentsFactoryDataSource<SegmentTag> ds, final SegmentTag st1, final SegmentTag st2) {
		SegmentTag temp1 = st1;
		while (temp1 != null) {
			SegmentTag temp2 = st2;
			while (temp2 != null) {
				if (temp1 == temp2) {
					return temp1;
				}
				temp2 = ds.getParent(temp2);
			}

			temp1 = ds.getParent(temp1);
		}

		return null;
	}

	private static <SegmentTag> int getDepth(final OrthogonalSegmentsFactoryDataSource<SegmentTag> ds,
			final SegmentTag st) {
		int depth = 0;
		for (SegmentTag tmp = st; tmp != null; tmp = ds.getParent(tmp)) {
			depth++;
		}

		return depth;
	}
}
