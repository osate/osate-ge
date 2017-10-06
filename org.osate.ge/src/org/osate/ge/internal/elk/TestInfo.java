package org.osate.ge.internal.elk;

import org.eclipse.elk.graph.ElkGraphElement;

// TODO: Rename
class TestInfo {
	public final ElkGraphElement layoutElement;
	public final ElkGraphElement layoutParent;

	public TestInfo(final ElkGraphElement layoutElement, final ElkGraphElement layoutParent) {
		// TODO: Require non-null
		this.layoutElement = layoutElement;
		this.layoutParent = layoutParent;
	}
}
