package org.osate.ge.internal.elk;

public class LayoutOptionsBuilder {
	private boolean interactive = false;
	private boolean lockTopLevelPorts = false;

	public final LayoutOptionsBuilder interactive() {
		this.interactive = true;
		return this;
	}

	public final LayoutOptionsBuilder lockTopLevelPorts() {
		this.lockTopLevelPorts = true;
		return this;
	}

	public final LayoutOptions build() {
		return new LayoutOptions(interactive, lockTopLevelPorts);
	}
}
