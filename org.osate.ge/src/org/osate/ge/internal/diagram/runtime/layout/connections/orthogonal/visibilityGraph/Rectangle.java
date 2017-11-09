package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph;

import org.osate.ge.graphics.Point;

public class Rectangle {
	private final Point min;
	private final Point max;

	public Rectangle(final Point min, final Point max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public final String toString() {
		return "Rect {" + getMin() + "," + getMax() + "}";
	}

	public final boolean contains(final Point p) {
		return p.x >= getMin().x && p.x <= getMax().x && p.y >= getMin().y && p.y <= getMax().y;
	}

	public final boolean borderContains(final Point p) {
		return ((p.x == getMin().x || p.x == getMax().x) && p.y >= getMin().y && p.y <= getMax().y)
				|| ((p.y == getMin().y || p.y == getMax().y) && p.x >= getMin().x && p.x <= getMax().x);
	}

	public final boolean isPoint() {
		return getMin().equals(getMax());
	}

	public final Point getMin() {
		return min;
	}

	public final Point getMax() {
		return max;
	}
}