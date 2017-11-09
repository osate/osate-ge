package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

import java.util.Collection;
import java.util.stream.Collectors;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphEdge;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;

public class SimpleAStarDelegate<N, E>
implements
AStarDelegate<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>, Double> {
	@Override
	public Double getActualCost(
			NodeEdgePair<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>> ne1,
			NodeEdgePair<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>> ne2) {
		return distance(ne1.node.position, ne2.node.position);
	}

	@Override
	public Double getEstimatedCost(final OrthogonalGraphNode<N, E> from,
			final OrthogonalGraphNode<N, E> to) {
		return distance(from.position, to.position);
	}

	@Override
	public Collection<NodeEdgePair<OrthogonalGraphNode<N, E>, OrthogonalGraphEdge<N, E>>> getNeighbors(
			final OrthogonalGraphNode<N, E> n) {
		return n.getEdges().values().stream().map(v -> new NodeEdgePair<>(v.node, v)).collect(Collectors.toList());
	}

	@Override
	public Double add(final Double c1, final Double c2) {
		return c1 + c2;
	}

	@Override
	public Double getZeroCost() {
		return Double.valueOf(0);
	}

	@Override
	public Double getInfiniteCost() {
		return Double.valueOf(Double.POSITIVE_INFINITY);
	}

	private static <NodeTag, EdgeTag> double distance(final Point p1, final Point p2) {
		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
	}
}