package org.osate.ge.internal.diagram.runtime.layout.connections;

class VerticalSegment<T> {
	public final double x;
	public final double minY;
	public final double maxY;
	public final T tag;

	public VerticalSegment(final double x, final double minY, final double maxY, final T tag) {
		this.x = x;
		this.minY = minY;
		this.maxY = maxY;
		// TODO: Assert max > min
		this.tag = tag;
	}

	@Override
	public String toString() {
		return "VerticalSegment {" + x + "," + minY + " -> " + maxY + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		temp = Double.doubleToLongBits(x);
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
		VerticalSegment other = (VerticalSegment) obj;
		if (Double.doubleToLongBits(maxY) != Double.doubleToLongBits(other.maxY)) {
			return false;
		}
		if (Double.doubleToLongBits(minY) != Double.doubleToLongBits(other.minY)) {
			return false;
		}
		if (tag == null) {
			if (other.tag != null) {
				return false;
			}
		} else if (!tag.equals(other.tag)) {
			return false;
		}
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		return true;
	}

}