package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph;

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

	public boolean contains(final Point p) {
		return p.x >= min.x && p.x <= max.x && p.y >= min.y && p.y <= max.y;
	}

	public boolean borderContains(final Point p) {
		return ((p.x == min.x || p.x == max.x) && p.y >= min.y && p.y <= max.y)
				|| ((p.y == min.y || p.y == max.y) && p.x >= min.x && p.x <= max.x);
	}

	public boolean isPoint() {
		return min.equals(max);
	}
}