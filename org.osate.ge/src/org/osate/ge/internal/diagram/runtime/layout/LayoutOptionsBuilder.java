package org.osate.ge.internal.diagram.runtime.layout;

/**
 * Builder for LayoutOptions.
 *
 */
public class LayoutOptionsBuilder {
	private boolean assignPortsToDefaultSides = false;

	public LayoutOptionsBuilder assignPortsToDefaultSides(final boolean value) {
		assignPortsToDefaultSides = value;
		return this;
	}

	public final LayoutOptions build() {
		return new LayoutOptions(assignPortsToDefaultSides);
	}
}
