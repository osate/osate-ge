package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

public class NodeEdgePair<NodeType, EdgeType> {
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