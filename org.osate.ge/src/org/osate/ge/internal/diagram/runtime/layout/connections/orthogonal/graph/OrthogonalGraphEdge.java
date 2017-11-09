package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph;

import java.util.Objects;

public class OrthogonalGraphEdge<NodeTag, EdgeTag> {
	public final OrthogonalDirection direction;
	public final OrthogonalGraphNode<NodeTag, EdgeTag> node;
	public final EdgeTag tag;

	public OrthogonalGraphEdge(final OrthogonalDirection direction, final OrthogonalGraphNode<NodeTag, EdgeTag> node,
			final EdgeTag tag) {
		this.direction = Objects.requireNonNull(direction, "direction must not be null");
		this.node = Objects.requireNonNull(node, "node must not be null");
		this.tag = tag;
	}
}
