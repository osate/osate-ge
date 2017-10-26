package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.Collection;
import java.util.stream.Collectors;

import org.osate.ge.internal.diagram.runtime.layout.connections.AStar.NodeEdgePair;

public class DefaultAStarDelegate implements AStar.Delegate<OrthogonalGraphNode, OrthogonalDirection, RouteCost> {
	@Override
	public RouteCost getActualCost(NodeEdgePair<OrthogonalGraphNode, OrthogonalDirection> ne1, NodeEdgePair<OrthogonalGraphNode, OrthogonalDirection> ne2) {
		final int numberOfBends = (ne1.edge == ne2.edge) ? 0 : 1;
		return new RouteCost(numberOfBends, distance(ne1.node, ne2.node));
	}

	@Override
	public RouteCost getEstimatedCost(final NodeEdgePair<OrthogonalGraphNode, OrthogonalDirection> from,
			final OrthogonalGraphNode to) {
		final boolean mustHaveBend = from.node.position.x != to.position.y && from.node.position.y != to.position.y;
		return new RouteCost(mustHaveBend ? 1 : 0, distance(from.node, to));
	}

	private static double distance(final OrthogonalGraphNode n1, final OrthogonalGraphNode n2) {
		return Math.abs(n1.position.x - n2.position.x) + Math.abs(n1.position.y - n2.position.y);
	}

	@Override
	public Collection<NodeEdgePair<OrthogonalGraphNode, OrthogonalDirection>> getNeighbors(final OrthogonalGraphNode n) {
		return n.getNeighbors().entrySet().stream().map(e -> new AStar.NodeEdgePair<>(e.getValue(), e.getKey()))
				.collect(Collectors.toList());
	}

	@Override
	public RouteCost add(final RouteCost c1, final RouteCost c2) {
		return RouteCost.add(c1, c2);
	}

	@Override
	public RouteCost getZeroCost() {
		return RouteCost.ZERO;
	}

	@Override
	public RouteCost getInfiniteCost() {
		return RouteCost.INFINITY;
	}
}