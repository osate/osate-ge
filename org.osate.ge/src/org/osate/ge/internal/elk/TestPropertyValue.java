package org.osate.ge.internal.elk;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkPort;

// TODO: Rework, rename, etc
// Immutable. Constructors, etc..
class TestPropertyValue {
	public LayoutMapping layoutMapping;
	public final Map<ElkPort, TestInfo> portInfoMap = new HashMap<>();
	public final Map<ElkEdge, TestInfo> edgeInfoMap = new HashMap<>();
}
