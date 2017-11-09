package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

import java.util.Collection;
import java.util.stream.Collectors;

import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphEdge;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;

public class SimpleAStarDelegate<N, E>
implements
AStarDelegate<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>, Integer> {
	@Override
	public Integer getActualCost(
			NodeEdgePair<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>> ne1,
			NodeEdgePair<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>> ne2) {
		return Integer.valueOf(1);
	}

	@Override
	public Integer getEstimatedCost(final OrthogonalGraphNode<N, E> from,
			final OrthogonalGraphNode<N, E> to) {
		// TODO:
		return Integer.valueOf(from == to ? 0 : 1);
	}

	@Override
	public Collection<NodeEdgePair<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>>> getNeighbors(
			final OrthogonalGraphNode<N, E> n) {
		return n.getEdges().values().stream().map(v -> new NodeEdgePair<>(v.node, v)).collect(Collectors.toList());
	}

	@Override
	public Integer add(final Integer c1, final Integer c2) {
		return c1 + c2;
	}

	@Override
	public Integer getZeroCost() {
		return Integer.valueOf(0);
	}

	@Override
	public Integer getInfiniteCost() {
		return Integer.valueOf(Integer.MAX_VALUE);
	}
}