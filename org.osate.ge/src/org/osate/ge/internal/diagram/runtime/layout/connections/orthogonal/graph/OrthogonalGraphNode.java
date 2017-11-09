package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import org.osate.ge.graphics.Point;

// TODO: Make generic.
// TODO: Accessors, etc
public class OrthogonalGraphNode<NodeTag, EdgeTag>
{
	public final Point position;
	public final NodeTag tag;
	private final EnumMap<OrthogonalDirection,
	OrthogonalGraphEdge<NodeTag, EdgeTag>> edges = new EnumMap<>(
			OrthogonalDirection.class);

	public OrthogonalGraphNode(final Point position) {
		this(position, null);
	}

	public OrthogonalGraphNode(final Point position, final NodeTag tag) {
		this.position = Objects.requireNonNull(position, "position must not be null");
		this.tag = tag;
	}

	public final Map<OrthogonalDirection, OrthogonalGraphEdge<NodeTag, EdgeTag>> getEdges() {
		return Collections.unmodifiableMap(edges);
	}

	public final OrthogonalGraphEdge<NodeTag, EdgeTag> getEdge(final OrthogonalDirection direction) {
		return edges.get(direction);
	}

	public final OrthogonalGraphNode<NodeTag, EdgeTag> getNeighbor(final OrthogonalDirection direction) {
		final OrthogonalGraphEdge<NodeTag, EdgeTag> edge = edges.get(direction);
		return edge == null ? null : edge.node;
	}

	public void setNeighbor(final OrthogonalDirection direction, OrthogonalGraphNode<NodeTag, EdgeTag> n) {
		setNeighbor(direction, n, null);
	}

	// Sets neighbor. Assumes the edge is bidirectional
	public void setNeighbor(final OrthogonalDirection direction, OrthogonalGraphNode<NodeTag, EdgeTag> n,
			final EdgeTag edgeTag) {
		edges.put(direction, new OrthogonalGraphEdge<>(direction, n, edgeTag));
		final OrthogonalDirection opposite = direction.opposite();
		n.edges.put(opposite, new OrthogonalGraphEdge<>(opposite, this, edgeTag));
	}
}