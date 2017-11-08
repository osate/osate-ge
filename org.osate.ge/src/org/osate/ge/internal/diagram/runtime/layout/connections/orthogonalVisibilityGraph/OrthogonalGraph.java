package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonalVisibilityGraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.osate.ge.graphics.Point;

public class OrthogonalGraph {
	private final Map<Point, OrthogonalGraphNode> positionToNodeMap;

	public OrthogonalGraph(final Map<Point, OrthogonalGraphNode> positionToNodeMap) {
		this.positionToNodeMap = Collections.unmodifiableMap(positionToNodeMap);
	}

	public Collection<OrthogonalGraphNode> getNodes() {
		return this.positionToNodeMap.values();
	}

	public OrthogonalGraphNode getNode(final Point position) {
		return positionToNodeMap.get(position);
	}
}