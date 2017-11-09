package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

import java.util.List;

import org.osate.ge.graphics.Point;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalDirection;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphEdge;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;

/**
 * Simple example of usage of the AStar class
 *
 */
public class AStarExample {
	public static void main(String[] args) {
		final OrthogonalGraphNode<Object, Object> n1 = new OrthogonalGraphNode<>(new Point(0.0, 0.0));
		final OrthogonalGraphNode<Object, Object> n2 = new OrthogonalGraphNode<>(new Point(100.0, 0.0));
		final OrthogonalGraphNode<Object, Object> n3 = new OrthogonalGraphNode<>(new Point(0.0, 100.0));
		final OrthogonalGraphNode<Object, Object> n4 = new OrthogonalGraphNode<>(new Point(100.0, 100.0));
		final OrthogonalGraphNode<Object, Object> n5 = new OrthogonalGraphNode<>(new Point(200.0, 100.0));
		final OrthogonalGraphNode<Object, Object> n6 = new OrthogonalGraphNode<>(new Point(0.0, 200.0));
		final OrthogonalGraphNode<Object, Object> n7 = new OrthogonalGraphNode<>(new Point(200.0, 200.0));
		final OrthogonalGraphNode<Object, Object> n8 = new OrthogonalGraphNode<>(new Point(100.0, 50.0));
		final OrthogonalGraphNode<Object, Object> n9 = new OrthogonalGraphNode<>(new Point(200.0, 50.0));
		final OrthogonalGraphNode<Object, Object> n10 = new OrthogonalGraphNode<>(new Point(300.0, 200.0));
		final OrthogonalGraphNode<Object, Object> n11 = new OrthogonalGraphNode<>(new Point(300.0, 200.0));

		n1.setNeighbor(OrthogonalDirection.RIGHT, n2);
		n1.setNeighbor(OrthogonalDirection.DOWN, n3);
		n2.setNeighbor(OrthogonalDirection.DOWN, n8);
		n3.setNeighbor(OrthogonalDirection.RIGHT, n4);
		n3.setNeighbor(OrthogonalDirection.DOWN, n6);
		n4.setNeighbor(OrthogonalDirection.RIGHT, n5);
		n5.setNeighbor(OrthogonalDirection.DOWN, n7);
		n9.setNeighbor(OrthogonalDirection.UP, n8);
		n6.setNeighbor(OrthogonalDirection.RIGHT, n7);
		n7.setNeighbor(OrthogonalDirection.RIGHT, n11);
		n8.setNeighbor(OrthogonalDirection.RIGHT, n9);
		n9.setNeighbor(OrthogonalDirection.RIGHT, n10);
		n10.setNeighbor(OrthogonalDirection.DOWN, n11);

		final List<NodeEdgePair<OrthogonalGraphNode<Object, Object>, OrthogonalGraphEdge<Object, Object>>> path = AStar
				.findPath(n1, n11,
						new SimpleAStarDelegate<Object, Object>());
		if (path == null) {
			System.out.println("Failure");
		} else {
			// Path
			System.out.println("Path:");
			for (final NodeEdgePair<OrthogonalGraphNode<Object, Object>, OrthogonalGraphEdge<Object, Object>> ne : path) {
				System.out.println(ne.node.position + " : " + (ne.edge == null ? "" : ne.edge.direction));
			}
		}
	}
}
