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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.VLineTo;

// TODO: Rename
public class DeviceNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	// TODO: Cleanup. Use collection? Can set property on nodes?
	private final Rectangle outline = new Rectangle();
	private final Path path = new Path(); // TODO: Rename
	private final Path lineSegments = new Path(); // TODO: Rename
	private final Path shadedPath = new Path();

	public DeviceNode() {
		lineSegments.setStrokeLineCap(StrokeLineCap.BUTT);
		this.getChildren().addAll(outline, shadedPath, path, lineSegments);// outline, innerRect, line1, line2, line3, line4);

		setLineWidth(2);
		setBackgroundColor(Color.WHITE);
		setOutlineColor(Color.BLACK);

		// TODO: Set default style
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void resize(double width, double height) {
		super.resize(width, height);

		final double padding = 14;

		outline.setWidth(width);
		outline.setHeight(height);

		path.getElements().setAll(new MoveTo(0, 0), new HLineTo(width), new VLineTo(height), new HLineTo(0),
				new VLineTo(0), new MoveTo(padding, padding), new HLineTo(width - padding),
				new VLineTo(height - padding),
				new HLineTo(padding), new VLineTo(padding));

		lineSegments.getElements().setAll(new MoveTo(0, 0), new LineTo(padding, padding), new MoveTo(width, 0),
				new LineTo(width - padding, padding), new MoveTo(0, height), new LineTo(padding, height - padding),
				new MoveTo(width - padding, height - padding), new LineTo(width, height));

		shadedPath.getElements().setAll(new MoveTo(width, 0), new LineTo(width - padding, padding),
				new VLineTo(height - padding), new LineTo(width, height), new MoveTo(0, height),
				new LineTo(padding, height - padding), new HLineTo(width - padding), new LineTo(width, height));
	}

	@Override
	public final void setBackgroundColor(final Color value) {
		outline.setFill(value);
		shadedPath.setFill(value.darker());
	}

	@Override
	public final void setOutlineColor(final Color value) {
		path.setStroke(value);
		lineSegments.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		path.setStrokeWidth(value);
		lineSegments.setStrokeWidth(value);
	}

}
