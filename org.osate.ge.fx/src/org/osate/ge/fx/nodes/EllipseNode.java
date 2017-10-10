package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

// TODO: Rename
public class EllipseNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final javafx.scene.shape.Ellipse ellipse = new Ellipse();

	public EllipseNode() {
		this.getChildren().addAll(ellipse);

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
		ellipse.setCenterX(width / 2.0);
		ellipse.setRadiusX(width / 2.0);
		ellipse.setCenterY(height / 2.0);
		ellipse.setRadiusY(height / 2.0);
	}

	@Override
	public final void setBackgroundColor(final Color value) {
		ellipse.setFill(value);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		ellipse.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		ellipse.setStrokeWidth(value);
	}

	public static void main(final String[] args) {
		NodeApplication.run(new EllipseNode());
	}
}
