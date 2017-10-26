package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.List;

import org.osate.ge.graphics.Point;

// TODO: Turn into a unit test.
public class AStarTest {
	public static void main(String[] args) {
		final OrthogonalGraphNode n1 = new OrthogonalGraphNode(new Point(0.0, 0.0));
		final OrthogonalGraphNode n2 = new OrthogonalGraphNode(new Point(100.0, 0.0));
		final OrthogonalGraphNode n3 = new OrthogonalGraphNode(new Point(0.0, 100.0));
		final OrthogonalGraphNode n4 = new OrthogonalGraphNode(new Point(100.0, 100.0));
		final OrthogonalGraphNode n5 = new OrthogonalGraphNode(new Point(200.0, 100.0));
		final OrthogonalGraphNode n6 = new OrthogonalGraphNode(new Point(0.0, 200.0));
		final OrthogonalGraphNode n7 = new OrthogonalGraphNode(new Point(200.0, 200.0));
		final OrthogonalGraphNode n8 = new OrthogonalGraphNode(new Point(100.0, 50.0));
		final OrthogonalGraphNode n9 = new OrthogonalGraphNode(new Point(200.0, 50.0));
		final OrthogonalGraphNode n10 = new OrthogonalGraphNode(new Point(300.0, 200.0));
		final OrthogonalGraphNode n11 = new OrthogonalGraphNode(new Point(300.0, 200.0));

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

		// TODO: How to consider hierarchy when routing

		final List<OrthogonalGraphNode> path = AStar.findPath(n1, n11, new DefaultAStarDelegate());
		if (path == null) {
			System.out.println("Failure");
		} else {
			// Path
			System.out.println("Path ");
			for (final OrthogonalGraphNode n : path) {
				System.out.println(n.position);
			}
		}
	}
}
