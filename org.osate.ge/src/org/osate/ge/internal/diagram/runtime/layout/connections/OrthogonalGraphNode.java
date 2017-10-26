package org.osate.ge.internal.diagram.runtime.layout.connections;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.osate.ge.graphics.Point;

class OrthogonalGraphNode
{
	public final Point position;
	private final EnumMap<OrthogonalDirection, OrthogonalGraphNode> neighbors = new EnumMap<>(OrthogonalDirection.class);

	public OrthogonalGraphNode(final Point position) {
		this.position = position;
	}

	public final Map<OrthogonalDirection, OrthogonalGraphNode> getNeighbors() {
		return Collections.unmodifiableMap(neighbors);
	}

	public final OrthogonalGraphNode getNeighbor(final OrthogonalDirection direction) {
		return neighbors.get(direction);
	}

	// Sets neighbor. Assumes the edge is bidirectional
	public void setNeighbor(final OrthogonalDirection direction, OrthogonalGraphNode n) {
		neighbors.put(direction, n);
		n.neighbors.put(direction.opposite(), this);
	}
}