package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// TODO: Rename
public class RectangleNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final javafx.scene.shape.Rectangle rect = new Rectangle();

	public RectangleNode() {
		this(false);
	}

	public RectangleNode(final boolean rounded) {
		this.getChildren().addAll(rect);

		setLineWidth(2);
		setBackgroundColor(Color.WHITE);
		setOutlineColor(Color.BLACK);
		setRounded(rounded);
	}

	public void setRounded(final boolean value) {
		final double roundedArcSize = value ? 25.0 : 0;
		rect.setArcWidth(roundedArcSize);
		rect.setArcHeight(roundedArcSize);
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void resize(double width, double height) {
		super.resize(width, height);
		rect.setWidth(width);
		rect.setHeight(height);
	}

	@Override
	public final void setBackgroundColor(final Color value) {
		rect.setFill(value);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		rect.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		rect.setStrokeWidth(value);
	}

	public static void main(final String[] args) {
		NodeApplication.run(new RectangleNode(), new RectangleNode(true));
	}
}
