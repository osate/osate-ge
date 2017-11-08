package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* search Algorithm
 * Based on the description and pseudocode on wikipedia.
 */
public class AStar {
	public static class NodeEdgePair<NodeType, EdgeType> {
		public final NodeType node;
		public final EdgeType edge;

		public NodeEdgePair(final NodeType node, final EdgeType edge) {
			this.node = node;
			this.edge = edge;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((edge == null) ? 0 : edge.hashCode());
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			NodeEdgePair<?, ?> other = (NodeEdgePair<?, ?>) obj;
			if (edge == null) {
				if (other.edge != null) {
					return false;
				}
			} else if (!edge.equals(other.edge)) {
				return false;
			}
			if (node == null) {
				if (other.node != null) {
					return false;
				}
			} else if (!node.equals(other.node)) {
				return false;
			}
			return true;
		}
	}

	public interface Delegate<NodeType, EdgeType, CostType> {
		/**
		 * Returns the actual cost between two nodes. The specified nodes are guaranteed to be neighbors.
		 * @param from is the starting point. The edge specified in the pair is the edge was used to reach the from node.
		 * @param to is the neighbor. The edge contained in the pair is the edge that will be used to traversed to the neighbor.
		 * @return
		 */
		CostType getActualCost(NodeEdgePair<NodeType, EdgeType> from, NodeEdgePair<NodeType, EdgeType> to);

		/**
		 * Returns the estimated cost to reach the target node from the specified node.
		 * To obtain the optimal path, this function must never overestimate the cost the minimum cost and it must be monotonic.
		 * Overestimated values will result in less accurate but faster results.
		 * @param from is the starting point. The edge contained in the pair is the edge was used to reach the from node.
		 * @param to
		 * @return
		 */
		CostType getEstimatedCost(NodeEdgePair<NodeType, EdgeType> from, NodeType to);

		Collection<NodeEdgePair<NodeType, EdgeType>> getNeighbors(NodeType n);

		CostType add(CostType c1, CostType c2);

		CostType getZeroCost();

		CostType getInfiniteCost();
	}

	/**
	 * Finds a path  between two nodes.
	 * @param startNode
	 * @param goal
	 * @param delegate
	 * @return the path from startNode to goal. The list will be in reverse order. That is, the first node in the list will be the goal. Returns null if a path could not be found.
	 */
	public static <NodeType, EdgeType, CostType extends Comparable<CostType>> List<NodeType> findPath(
			final NodeType startNode, final NodeType goal,
			final Delegate<NodeType, EdgeType, CostType> delegate) {
		final CostType zeroCost = delegate.getZeroCost();
		final CostType infiniteCost = delegate.getInfiniteCost();

		final Set<NodeEdgePair<NodeType, EdgeType>> closedSet = new HashSet<>();
		final NodeEdgePair<NodeType, EdgeType> start = new NodeEdgePair<>(startNode, null);
		final Map<NodeEdgePair<NodeType, EdgeType>, NodeEdgePair<NodeType, EdgeType>> cameFrom = new HashMap<>();
		final Map<NodeEdgePair<NodeType, EdgeType>, CostType> nodeToCurrentBestTotalCostMap = new HashMap<>();
		nodeToCurrentBestTotalCostMap.put(start, zeroCost);

		final Map<NodeEdgePair<NodeType, EdgeType>, CostType> nodeToHeuristicCostToGoalMap = new HashMap<>();
		nodeToHeuristicCostToGoalMap.put(start, delegate.getEstimatedCost(start, startNode));

		final PriorityQueue<NodeEdgePair<NodeType, EdgeType>> nextEdgeToTraverseQueue = new PriorityQueue<>(
				(n1, n2) -> nodeToHeuristicCostToGoalMap.getOrDefault(n1, infiniteCost).compareTo(nodeToHeuristicCostToGoalMap.getOrDefault(n2, infiniteCost)));
		nextEdgeToTraverseQueue.add(new NodeEdgePair<>(startNode, null));

		while (!nextEdgeToTraverseQueue.isEmpty()) {
			final NodeEdgePair<NodeType, EdgeType> current = nextEdgeToTraverseQueue.poll();
			if (current.node == goal) {
				// Build the path
				final List<NodeType> path = new ArrayList<>();
				NodeEdgePair<NodeType, EdgeType> tmp = current;
				do {
					path.add(tmp.node);
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
							delegate.getEstimatedCost(neighborAndEdge, goal)));


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
