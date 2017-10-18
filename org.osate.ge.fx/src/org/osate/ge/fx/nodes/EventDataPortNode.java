package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.geometry.Point2D;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class EventDataPortNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private static final Point2D dataSymbolInOutPadding = new Point2D(10, 4);
	private static final Point2D dataSymbolDirectionalPadding = new Point2D(8, 4);

	private final EventPortNode eventPort = new EventPortNode();
	private final DataPortNode dataPort = new DataPortNode();

	public EventDataPortNode() {
		this.getChildren().addAll(eventPort, dataPort);

		setLineWidth(2);
		setBackgroundColor(Color.BLACK);
		setOutlineColor(Color.BLACK);
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void resize(final double width, final double height) {
		super.resize(width, height);

		// TODO: Directions
		final Point2D dataSymbolPadding = dataSymbolDirectionalPadding; // TODO: Choose base on direction

		// TODO
		dataPort.resize(width - dataSymbolPadding.getX(), height - 2 * dataSymbolPadding.getY());
		eventPort.resize(width, height);
		dataPort.setLayoutX(0);
		dataPort.setLayoutY(dataSymbolPadding.getY());

	}

	@Override
	public final void setBackgroundColor(final Color value) {
		dataPort.setBackgroundColor(value);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		dataPort.setOutlineColor(value);
		eventPort.setOutlineColor(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		dataPort.setLineWidth(value);
		eventPort.setLineWidth(value);
	}

	public static void main(final String[] args) {
		final EventDataPortNode port = new EventDataPortNode();
		NodeApplication.run(port);
	}
}
