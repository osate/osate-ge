package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonalVisibilityGraph;

public enum OrthogonalDirection {
	UP {
		@Override
		public OrthogonalDirection opposite() {
			return DOWN;
		}
	}, DOWN {
		@Override
		public OrthogonalDirection opposite() {
			return UP;
		}
	}, LEFT {
		@Override
		public OrthogonalDirection opposite() {
			return RIGHT;
		}
	}, RIGHT {
		@Override
		public OrthogonalDirection opposite() {
			return LEFT;
		}
	};

	public abstract OrthogonalDirection opposite();
}