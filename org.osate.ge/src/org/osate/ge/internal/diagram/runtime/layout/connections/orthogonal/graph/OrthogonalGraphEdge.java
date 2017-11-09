package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph;

import java.util.Objects;

public class OrthogonalGraphEdge<NodeTag, EdgeTag> {
	private final OrthogonalDirection direction;
	private final OrthogonalGraphNode<NodeTag, EdgeTag> node;
	private final EdgeTag tag;

	public OrthogonalGraphEdge(final OrthogonalDirection direction, final OrthogonalGraphNode<NodeTag, EdgeTag> node,
			final EdgeTag tag) {
		this.direction = Objects.requireNonNull(direction, "direction must not be null");
		this.node = Objects.requireNonNull(node, "node must not be null");
		this.tag = tag;
	}

	public final OrthogonalDirection getDirection() {
		return direction;
	}

	public final EdgeTag getTag() {
		return tag;
	}

	public final OrthogonalGraphNode<NodeTag, EdgeTag> getNode() {
		return node;
	}
}
