package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* search Algorithm implementation.
 * Based on the description and pseudocode on wikipedia.
 */
public class AStar {
	/**
	 * Finds a path  between two nodes.
	 * @param startNode
	 * @param goal
	 * @param delegate
	 * @return the path from startNode to goal. Returns null if a path could not be found.
	 */
	public static <NodeType, EdgeType, CostType extends Comparable<CostType>> List<NodeEdgePair<NodeType, EdgeType>> findPath(
			final NodeType startNode, final NodeType goal,
			final AStarDelegate<NodeType, EdgeType, CostType> delegate) {
		final CostType zeroCost = delegate.getZeroCost();
		final CostType infiniteCost = delegate.getInfiniteCost();

		final Set<NodeEdgePair<NodeType, EdgeType>> closedSet = new HashSet<>();
		final NodeEdgePair<NodeType, EdgeType> start = new NodeEdgePair<>(startNode, null);
		final Map<NodeEdgePair<NodeType, EdgeType>, NodeEdgePair<NodeType, EdgeType>> cameFrom = new HashMap<>();
		final Map<NodeEdgePair<NodeType, EdgeType>, CostType> nodeToCurrentBestTotalCostMap = new HashMap<>();
		nodeToCurrentBestTotalCostMap.put(start, zeroCost);

		final Map<NodeEdgePair<NodeType, EdgeType>, CostType> nodeToHeuristicCostToGoalMap = new HashMap<>();
		nodeToHeuristicCostToGoalMap.put(start, delegate.getEstimatedCost(startNode, startNode));

		final PriorityQueue<NodeEdgePair<NodeType, EdgeType>> nextEdgeToTraverseQueue = new PriorityQueue<>(
				(n1, n2) -> nodeToHeuristicCostToGoalMap.getOrDefault(n1, infiniteCost).compareTo(nodeToHeuristicCostToGoalMap.getOrDefault(n2, infiniteCost)));
		nextEdgeToTraverseQueue.add(new NodeEdgePair<>(startNode, null));

		while (!nextEdgeToTraverseQueue.isEmpty()) {
			final NodeEdgePair<NodeType, EdgeType> current = nextEdgeToTraverseQueue.poll();
			if (current.node == goal) {
				// Build the path
				final LinkedList<NodeEdgePair<NodeType, EdgeType>> path = new LinkedList<>();
				NodeEdgePair<NodeType, EdgeType> tmp = current;
				do {
					path.addFirst(tmp);
					tmp = cameFrom.get(tmp);
				} while (tmp != null);

				return path;
			}

			closedSet.add(current);

			for (final NodeEdgePair<NodeType, EdgeType> neighborAndEdge : delegate.getNeighbors(current.node)) {
				// Ignore the neighbor and edge if it has already been processed
				if (closedSet.contains(neighborAndEdge)) {
					continue;
				}

				// Use this path if the cost is
				boolean needToAdd;
				final CostType costToNeighbor = delegate.add(nodeToCurrentBestTotalCostMap.getOrDefault(current, infiniteCost),
						delegate.getActualCost(current, neighborAndEdge));
				if (costToNeighbor.compareTo(nodeToCurrentBestTotalCostMap.getOrDefault(neighborAndEdge.node, infiniteCost)) <= 0) {
					cameFrom.put(neighborAndEdge, current);
					nodeToCurrentBestTotalCostMap.put(neighborAndEdge, costToNeighbor);
					nodeToHeuristicCostToGoalMap.put(neighborAndEdge, delegate.add(costToNeighbor,
							delegate.getEstimatedCost(neighborAndEdge.node, goal)));


					// Remove the edge if it was already in the queue. We need to remove and are-add since the cost has been updated
					nextEdgeToTraverseQueue.remove(neighborAndEdge);

					needToAdd = true;
				} else {
					needToAdd = !nextEdgeToTraverseQueue.contains(neighborAndEdge);
				}

				if (needToAdd) {
					// Add the neighbor and edge pair to the queue.
					nextEdgeToTraverseQueue.add(neighborAndEdge);
				}
			}
		}

		// Failure
		return null;
	}
}
