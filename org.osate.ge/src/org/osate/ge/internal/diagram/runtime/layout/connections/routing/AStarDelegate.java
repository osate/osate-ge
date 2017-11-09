package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

import java.util.Collection;

public interface AStarDelegate<NodeType, EdgeType, CostType> {
	/**
	 * Returns the actual cost between two nodes. The specified nodes are guaranteed to be neighbors.
	 * @param from is the starting point. The edge specified in the pair is the edge was used to reach the from node.
	 * @param to is the neighbor. The edge contained in the pair is the edge that will be used to traversed to the neighbor.
	 * @return
	 */
	CostType getActualCost(NodeEdgePair<NodeType, EdgeType> from, NodeEdgePair<NodeType, EdgeType> to);

	/**
	 * Returns the estimated cost to reach the target node from the specified node.
	 * To obtain the optimal path, this function must never underestimate the cost and it must be monotonic.
	 * Overestimated values will result in less accurate but faster results.
	 * @param from is the starting point.
	 * @param to
	 * @return
	 */
	CostType getEstimatedCost(NodeType from, NodeType to);

	Collection<NodeEdgePair<NodeType, EdgeType>> getNeighbors(NodeType n);

	CostType add(CostType c1, CostType c2);

	CostType getZeroCost();

	CostType getInfiniteCost();
}