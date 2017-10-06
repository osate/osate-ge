package org.osate.ge.internal.elk;

public class LayoutOptions {
	public final boolean interactive; // True if changes should be minimized
	public final boolean lockTopLevelPorts;

	LayoutOptions(final boolean interactive, final boolean lockTopLevelPorts) {
		this.interactive = interactive;
		this.lockTopLevelPorts = lockTopLevelPorts;
	}
}