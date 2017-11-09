package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy;

import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalSegmentsFactoryDataSource;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.NodeTagCreator;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.Rectangle;

/**
 * Class used for a tag for nodes when working with hierarchical graphs.
 *
 */
public class HierarchicalNodeTag<ModelElement> {
	private final ModelElement container;
	private final ModelElement borderElement; // Will only be set if the node is on the border of a model element
	private final int depth;

	public HierarchicalNodeTag(final ModelElement container, final ModelElement borderElement, final int depth) {
		this.container = container;
		this.borderElement = borderElement;
		this.depth = depth;
	}

	public final ModelElement getContainer() {
		return container;
	}

	public final ModelElement getBorderElement() {
		return borderElement;
	}

	public final int getDepth() {
		return depth;
	}

	/**
	 * Creates a node tag creator which creates a HierarchicalNodeTag object.
	 * @param ds
	 * @return
	 */
	public static <SegmentTag> NodeTagCreator<SegmentTag, HierarchicalNodeTag<SegmentTag>> createHierarchicalNodeTagCreator(
			final OrthogonalSegmentsFactoryDataSource<SegmentTag> ds) {
		return (nodePosition, hs, vs) -> {
			SegmentTag borderElement = null;

			// Start with the first common ancestor
			SegmentTag st = getFirstCommonAncestor(ds, hs.getTag(), vs.tag);
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
				while (bounds.borderContains(nodePosition) && st != null) {
					borderElement = st;
					st = ds.getParent(st);
					if (st != null) {
						bounds = ds.getBounds(st);
					}
				}
			}

			return new HierarchicalNodeTag<SegmentTag>(st, borderElement, getDepth(ds, st));
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
