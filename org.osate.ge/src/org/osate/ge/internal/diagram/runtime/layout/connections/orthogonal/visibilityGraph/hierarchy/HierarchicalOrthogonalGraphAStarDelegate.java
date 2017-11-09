package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy;

import java.util.Collection;
import java.util.stream.Collectors;

import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphEdge;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;
import org.osate.ge.internal.diagram.runtime.layout.connections.routing.AStarDelegate;
import org.osate.ge.internal.diagram.runtime.layout.connections.routing.NodeEdgePair;

/**
 * Implementation of the AStarDelegate interface intended for use with hierarchical graphs. Uses HierarchicalOrthogonalRouteCost for route costs.
 *
 * @param <NodeTag>
 * @param <EdgeTag>
 */
public class HierarchicalOrthogonalGraphAStarDelegate<NodeTag extends HierarchicalNodeTag<?>, EdgeTag extends HierarchicalEdgeTag>
implements
AStarDelegate<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>, HierarchicalOrthogonalRouteCost> {
	@Override
	public HierarchicalOrthogonalRouteCost getActualCost(
			NodeEdgePair<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>> ne1,
			NodeEdgePair<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>> ne2) {
		final int numberOfBends = (ne1.edge.getDirection() == ne2.edge.getDirection()) ? 0 : 1;
		return new HierarchicalOrthogonalRouteCost(ne2.edge.getTag().getLevelsCrossed(), numberOfBends, distance(ne1.node, ne2.node));
	}

	@Override
	public HierarchicalOrthogonalRouteCost getEstimatedCost(
			final OrthogonalGraphNode<NodeTag, EdgeTag> from,
			final OrthogonalGraphNode<NodeTag, EdgeTag> to) {
		final boolean mustHaveBend = from.getPosition().x != to.getPosition().y && from.getPosition().y != to.getPosition().y;
		final int estNumberOfBends = mustHaveBend ? 1 : 0;
		final int estHierarchyCrossings = Math.abs(from.getTag().getDepth() - to.getTag().getDepth());
		return new HierarchicalOrthogonalRouteCost(estHierarchyCrossings, estNumberOfBends,
				distance(from, to));
	}

	private static <NodeTag, EdgeTag> double distance(final OrthogonalGraphNode<NodeTag, EdgeTag> n1,
			final OrthogonalGraphNode<NodeTag, EdgeTag> n2) {
		return Math.abs(n1.getPosition().x - n2.getPosition().x) + Math.abs(n1.getPosition().y - n2.getPosition().y);
	}

	@Override
	public Collection<NodeEdgePair<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>>> getNeighbors(
			final OrthogonalGraphNode<NodeTag, EdgeTag> n) {
		return n.getEdges().values().stream().map(v -> new NodeEdgePair<>(v.getNode(), v))
				.collect(Collectors.toList());
	}

	@Override
	public HierarchicalOrthogonalRouteCost add(final HierarchicalOrthogonalRouteCost c1, final HierarchicalOrthogonalRouteCost c2) {
		return HierarchicalOrthogonalRouteCost.add(c1, c2);
	}

	@Override
	public HierarchicalOrthogonalRouteCost getZeroCost() {
		return HierarchicalOrthogonalRouteCost.ZERO;
	}

	@Override
	public HierarchicalOrthogonalRouteCost getInfiniteCost() {
		return HierarchicalOrthogonalRouteCost.INFINITY;
	}
}