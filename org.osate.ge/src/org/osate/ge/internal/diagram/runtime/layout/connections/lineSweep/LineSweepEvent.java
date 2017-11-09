package org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep;

import java.util.Objects;

import org.osate.ge.internal.diagram.runtime.layout.connections.lineSweep.LineSweeper.EventType;

/**
 * An event which is processed by the sweep line algorithm
 *
 * @param <Tag> is the type of the user-defined data associated with the event.
 */
public class LineSweepEvent<Tag> {
	public final EventType type;

	public final double position; // Position along the axis being swept. For a sweep using a vertical line, this would be the x-value of the object.

	public final Tag tag;

	public LineSweepEvent(final EventType type, final double position, final Tag tag) {
		this.type = Objects.requireNonNull(type, "type must not be null");
		this.position = position;
		this.tag = tag;
	}
}