package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

// TODO: Rename
public class BusNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final javafx.scene.shape.Polygon poly = new javafx.scene.shape.Polygon();

	public BusNode() {
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
	public void resize(final double width, final double height) {
		super.resize(width, height);
		final double arrowHeadWidth = Math.max(Math.min(width, height) / 4.0, 20.0);
		final double arrowHeadVerticalExtensionSize = height / 4;
		poly.getPoints().setAll(0.0, height / 2.0, arrowHeadWidth, 0.0, arrowHeadWidth, arrowHeadVerticalExtensionSize,
				width - arrowHeadWidth, arrowHeadVerticalExtensionSize, width - arrowHeadWidth, 0.0, width,
				height / 2.0, width - arrowHeadWidth, height, width - arrowHeadWidth,
				height - arrowHeadVerticalExtensionSize, arrowHeadWidth,
				height - arrowHeadVerticalExtensionSize, arrowHeadWidth, height);
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
