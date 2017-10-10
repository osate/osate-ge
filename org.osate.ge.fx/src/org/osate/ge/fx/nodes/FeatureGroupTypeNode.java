package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;

public class FeatureGroupTypeNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final Path path = new Path();

	public FeatureGroupTypeNode() {
		this.getChildren().addAll(path);

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

		final double halfEllipseOuterWidth = 2.0 * width / 3.0;
		final double halfEllipseOuterRadiusY = height / 2.0;
		final double halfEllipseInnerWidth = (halfEllipseOuterWidth + halfEllipseOuterWidth * 0.2) / 2.0;
		final double halfEllipseInnerRadiusY = (halfEllipseOuterRadiusY + halfEllipseOuterRadiusY * 0.2) / 2.0;
		final double halfEllipseYOffset = (height - 2 * halfEllipseInnerRadiusY) / 2.0;

		final double innerEllipseCenterX = halfEllipseOuterWidth;
		final double innerEllipseRadiusX = width / 3.0;
		final double innerEllipseRadiusY = height / 4.0;
		final double innerEllipseTopY = (height / 2.0) - innerEllipseRadiusY;

		path.getElements().setAll(
				// Outer Half Ellipse
				new MoveTo(halfEllipseOuterWidth, 0.0),
				new ArcTo(halfEllipseOuterWidth, height / 2.0, 0.0, halfEllipseOuterWidth,
						height,
						false, false),
				new VLineTo(height - halfEllipseYOffset),
				new ArcTo(halfEllipseInnerWidth, halfEllipseInnerRadiusY, 0.0, halfEllipseOuterWidth, halfEllipseYOffset,
						false,
						true),
				new VLineTo(0.0),
				// Inner Ellipse
				new MoveTo(innerEllipseCenterX + 1, innerEllipseTopY), new ArcTo(innerEllipseRadiusX,
						innerEllipseRadiusY, 0.0, innerEllipseCenterX, innerEllipseTopY, true, true),
				new ClosePath()
				);
	}

	@Override
	public final void setBackgroundColor(final Color value) {
		path.setFill(value);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		path.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		path.setStrokeWidth(value);
	}

	public static void main(final String[] args) {
		NodeApplication.run(new FeatureGroupTypeNode());
	}
}
