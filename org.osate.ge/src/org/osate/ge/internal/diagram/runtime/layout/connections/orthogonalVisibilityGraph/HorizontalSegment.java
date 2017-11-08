package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonalVisibilityGraph;

class HorizontalSegment<T> {
	public final double y;
	public final double minX;
	public final double maxX;
	public final T tag;

	public HorizontalSegment(final double y, final double minX, final double maxX, final T tag) {
		this.y = y;
		this.minX = minX;
		this.maxX = maxX;
		// TODO: Assert max > min
		this.tag = tag;
	}

	@Override
	public String toString() {
		return "HorizontalSegment {" + y + "," + minX + " -> " + maxX + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		temp = Double.doubleToLongBits(y);
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
		HorizontalSegment other = (HorizontalSegment) obj;
		if (Double.doubleToLongBits(maxX) != Double.doubleToLongBits(other.maxX)) {
			return false;
		}
		if (Double.doubleToLongBits(minX) != Double.doubleToLongBits(other.minX)) {
			return false;
		}
		if (tag == null) {
			if (other.tag != null) {
				return false;
			}
		} else if (!tag.equals(other.tag)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}
}