package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.osate.ge.graphics.Point;

public class OrthogonalGraph<NodeTag, EdgeType> {
	private final Map<Point, OrthogonalGraphNode<NodeTag, EdgeType>> positionToNodeMap;

	public OrthogonalGraph(final Map<Point, OrthogonalGraphNode<NodeTag, EdgeType>> positionToNodeMap) {
		this.positionToNodeMap = Collections.unmodifiableMap(positionToNodeMap);
	}

	public final Collection<OrthogonalGraphNode<NodeTag, EdgeType>> getNodes() {
		return this.positionToNodeMap.values();
	}

	public final OrthogonalGraphNode<NodeTag, EdgeType> getNode(final Point position) {
		return positionToNodeMap.get(position);
	}
}