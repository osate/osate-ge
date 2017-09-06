package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

// TODO: Rename
public class ParallelogramNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final javafx.scene.shape.Polygon poly = new Polygon();
	private final double horizontalOffset;

	public ParallelogramNode(final double horizontalOffset) {
		this.horizontalOffset = horizontalOffset;
		this.getChildren().addAll(poly);

		setLineWidth(2);
		setBackgroundColor(Color.WHITE);
		setOutlineColor(Color.BLACK);
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void resize(double width, double height) {
		super.resize(width, height);

		poly.getPoints().setAll(horizontalOffset, 0.0, width, 0.0,
				width - horizontalOffset, height,
				0.0, height);

	}

	@Override
	public final void setBackgroundColor(final Color value) {
		poly.setFill(value);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		poly.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		poly.setStrokeWidth(value);
	}
}
