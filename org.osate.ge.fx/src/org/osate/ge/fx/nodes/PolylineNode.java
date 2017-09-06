package org.osate.ge.fx.nodes;

import java.util.Objects;

import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

// TODO: Rename?
public class PolylineNode extends Region implements HasOutlineColor, HasLineWidth {
	private final javafx.scene.shape.Polyline poly = new Polyline();
	private double[] points; // TOOD: Document meaning/range

	public PolylineNode(final double... points) {
		this.points = Objects.requireNonNull(points, "points must not be null").clone();
		this.getChildren().addAll(poly);

		// TODO: Require multiple of 2
		// TODO: Set points?

		setLineWidth(2);
		setOutlineColor(Color.BLACK);
	}
	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void resize(double width, double height) {
		super.resize(width, height);

		// Transform the points based on the specified size
		Double[] sizedPoints = new Double[points.length];
		for (int i = 0; i < sizedPoints.length; i += 2) {
			sizedPoints[i] = points[i] * width;
			sizedPoints[i + 1] = points[i + 1] * height;
		}

		poly.getPoints().setAll(sizedPoints);
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
