package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.VLineTo;

// TODO: Rename
public class MemoryNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final Path outline = new Path();
	private final Path innerCurve = new Path();

	public MemoryNode() {
		innerCurve.setStrokeLineCap(StrokeLineCap.BUTT);
		this.getChildren().addAll(outline, innerCurve);

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

		final int ellipseHeight = 20;
		final double halfEllipseHeight = ellipseHeight / 2.0;

		outline.getElements().setAll(new MoveTo(width, halfEllipseHeight),
				new ArcTo(width / 2.0, halfEllipseHeight, 0.0, 0.0, halfEllipseHeight, false, false),
				new MoveTo(0, ellipseHeight / 2.0), new VLineTo(height - ellipseHeight),
				new ArcTo(width / 2.0, halfEllipseHeight, 0.0, width, height - ellipseHeight, false, false),
				new VLineTo(ellipseHeight / 2.0));
		innerCurve.getElements().setAll(new MoveTo(0, ellipseHeight / 2.0),
				new ArcTo(width / 2.0, halfEllipseHeight, 0.0, width, halfEllipseHeight, false, false));
	}

	@Override
	public final void setBackgroundColor(final Color value) {
		outline.setFill(value);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		outline.setStroke(value);
		innerCurve.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		outline.setStrokeWidth(value);
		innerCurve.setStrokeWidth(value);
	}

	public static void main(final String[] args) {
		NodeApplication.run(new MemoryNode());
	}
}
