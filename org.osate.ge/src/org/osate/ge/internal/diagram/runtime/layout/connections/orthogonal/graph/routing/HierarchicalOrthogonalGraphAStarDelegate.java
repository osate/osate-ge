package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.routing;

import java.util.Collection;
import java.util.stream.Collectors;

import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphEdge;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy.EdgeHierarchy;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy.NodeHierarchy;
import org.osate.ge.internal.diagram.runtime.layout.connections.routing.AStarDelegate;
import org.osate.ge.internal.diagram.runtime.layout.connections.routing.NodeEdgePair;

public class HierarchicalOrthogonalGraphAStarDelegate<NodeTag extends NodeHierarchy<?>, EdgeTag extends EdgeHierarchy>
implements
AStarDelegate<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>, HierarchicalOrthogonalRouteCost> {
	@Override
	public HierarchicalOrthogonalRouteCost getActualCost(
			NodeEdgePair<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>> ne1,
			NodeEdgePair<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>> ne2) {
		final int numberOfBends = (ne1.edge.direction == ne2.edge.direction) ? 0 : 1;
		return new HierarchicalOrthogonalRouteCost(ne2.edge.tag.depth, numberOfBends, distance(ne1.node, ne2.node));
	}

	@Override
	public HierarchicalOrthogonalRouteCost getEstimatedCost(
			final OrthogonalGraphNode<NodeTag, EdgeTag> from,
			final OrthogonalGraphNode<NodeTag, EdgeTag> to) {
		final boolean mustHaveBend = from.position.x != to.position.y && from.position.y != to.position.y;
		final int estNumberOfBends = mustHaveBend ? 1 : 0;
		final int estHierarchyCrossings = Math.abs(from.tag.depth - to.tag.depth);
		return new HierarchicalOrthogonalRouteCost(estHierarchyCrossings, estNumberOfBends,
				distance(from, to));
	}

	private static <NodeTag, EdgeTag> double distance(final OrthogonalGraphNode<NodeTag, EdgeTag> n1,
			final OrthogonalGraphNode<NodeTag, EdgeTag> n2) {
		return Math.abs(n1.position.x - n2.position.x) + Math.abs(n1.position.y - n2.position.y);
	}

	@Override
	public Collection<NodeEdgePair<OrthogonalGraphNode<NodeTag, EdgeTag>, OrthogonalGraphEdge<NodeTag, EdgeTag>>> getNeighbors(
			final OrthogonalGraphNode<NodeTag, EdgeTag> n) {
		return n.getEdges().values().stream().map(v -> new NodeEdgePair<>(v.node, v))
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