package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class EventPortNode extends Region implements HasOutlineColor, HasLineWidth {
	private final javafx.scene.shape.Polyline poly = new javafx.scene.shape.Polyline();

	public EventPortNode() {
		this.getChildren().addAll(poly);

		setLineWidth(2);
		setOutlineColor(Color.BLACK);
		poly.setFill(Color.WHITE);
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void resize(final double width, final double height) {
		super.resize(width, height);
		poly.getPoints().setAll(0.0, 0.0, width, height / 2.0, 0.0, height);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		poly.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		poly.setStrokeWidth(value);
	}

	public static void main(final String[] args) {
		NodeApplication.run(new EventPortNode());
	}
}
