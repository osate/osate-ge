package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.VLineTo;

// TODO: Rename
public class ProcessorNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final Path outlineFill = new Path();
	private final Path shadedArea = new Path();
	private final Path outline = new Path();
	private final Path lineSegments = new Path();

	public ProcessorNode() {
		lineSegments.setStrokeLineCap(StrokeLineCap.BUTT);
		outlineFill.setStroke(null);
		shadedArea.setStroke(null);
		this.getChildren().addAll(outlineFill, shadedArea, outline, lineSegments);

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

		final int horizontalOffset = 10;
		final int depth = 12;

		final PathElement[] outlinePathElements = new PathElement[] { new MoveTo(0, height), new VLineTo(depth),
				new LineTo(horizontalOffset, 0),
				new HLineTo(width), new VLineTo(height - depth), new LineTo(width - horizontalOffset, height),
				new HLineTo(0) };
		outlineFill.getElements().setAll(outlinePathElements);
		outline.getElements().setAll(outlinePathElements);

		lineSegments.getElements().setAll(new MoveTo(0, depth), new HLineTo(width - horizontalOffset),
				new LineTo(width, 0), new MoveTo(width - horizontalOffset, depth), new VLineTo(height));

		shadedArea.getElements().setAll(new MoveTo(width - horizontalOffset, depth), new LineTo(width, 0),
				new VLineTo(height - depth), new LineTo(width - horizontalOffset, height));
	}

	@Override
	public final void setBackgroundColor(final Color value) {
		outlineFill.setFill(value);
		shadedArea.setFill(value.darker());
	}

	@Override
	public final void setOutlineColor(final Color value) {
		outline.setStroke(value);
		lineSegments.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		outline.setStrokeWidth(value);
		lineSegments.setStrokeWidth(value);
	}

	public static void main(final String[] args) {
		NodeApplication.run(new ProcessorNode());
	}
}
