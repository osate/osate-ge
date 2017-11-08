package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonalVisibilityGraph;

import org.osate.ge.graphics.Point;

public class Rectangle {
	public final Point min;
	public final Point max;

	public Rectangle(final Point min, final Point max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public String toString() {
		return "Rect {" + min + "," + max + "}";
	}
}