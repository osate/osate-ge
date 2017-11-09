package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph;

import static com.google.common.base.Preconditions.checkArgument;

public class HorizontalSegment<T> {
	private final double y;
	private final double minX;
	private final double maxX;
	private final T tag;

	public HorizontalSegment(final double y, final double minX, final double maxX, final T tag) {
		checkArgument(maxX >= minX, "Max X is less than min.");
		this.y = y;
		this.minX = minX;
		this.maxX = maxX;
		this.tag = tag;
	}

	@Override
	public String toString() {
		return "HorizontalSegment {" + getY() + "," + getMinX() + " -> " + getMaxX() + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(getMaxX());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getMinX());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((getTag() == null) ? 0 : getTag().hashCode());
		temp = Double.doubleToLongBits(getY());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HorizontalSegment<?> other = (HorizontalSegment<?>) obj;
		if (Double.doubleToLongBits(getMaxX()) != Double.doubleToLongBits(other.getMaxX())) {
			return false;
		}
		if (Double.doubleToLongBits(getMinX()) != Double.doubleToLongBits(other.getMinX())) {
			return false;
		}
		if (getTag() == null) {
			if (other.getTag() != null) {
				return false;
			}
		} else if (!getTag().equals(other.getTag())) {
			return false;
		}
		if (Double.doubleToLongBits(getY()) != Double.doubleToLongBits(other.getY())) {
			return false;
		}
		return true;
	}

	public final double getY() {
		return y;
	}

	public final double getMinX() {
		return minX;
	}

	public final double getMaxX() {
		return maxX;
	}

	public final T getTag() {
		return tag;
	}
}