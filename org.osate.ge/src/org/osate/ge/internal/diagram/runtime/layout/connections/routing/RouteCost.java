package org.osate.ge.internal.diagram.runtime.layout.connections.routing;

class RouteCost implements Comparable<RouteCost> {
	public static RouteCost ZERO = new RouteCost(0, 0);
	public static RouteCost INFINITY = new RouteCost(Integer.MAX_VALUE, Double.POSITIVE_INFINITY);

	public final int numberOfBends;
	public final double length;

	public RouteCost(final int numberOfBends, final double length) {
		this.numberOfBends = numberOfBends;
		this.length = length;
	}

	@Override
	public int compareTo(final RouteCost c2) {
		int result = Integer.compare(numberOfBends, c2.numberOfBends);
		if (result == 0) {
			return Double.compare(length, c2.length);
		} else {
			return result;
		}
	}

	public static RouteCost add(final RouteCost c1, final RouteCost c2) {
		return new RouteCost(c1.numberOfBends + c2.numberOfBends, c1.length + c2.length);
	}
}