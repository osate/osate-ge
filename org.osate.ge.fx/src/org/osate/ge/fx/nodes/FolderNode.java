package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

// TODO: Rename
public class FolderNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final static double folderTabHeight = 9;
	private final static double folderMaxTabWidth = 100;
	private final static double folderTabOffsetAngle = 30.0;
	private final static double folderTopOfTabOffset = 0.3;

	private final javafx.scene.shape.Polygon poly = new javafx.scene.shape.Polygon();

	public FolderNode() {
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

		// Width of tab
		final double widthOfTab = Math.min(folderMaxTabWidth, (int) (width * folderTopOfTabOffset));
//		// The tab start and end slope
		final double tabOffset = (int) (Math.ceil(Math.tan(Math.toRadians(folderTabOffsetAngle)) * folderTabHeight));

		poly.getPoints().setAll(0.0, height, 0.0, folderTabHeight, tabOffset, 0.0, widthOfTab, 0.0,
				widthOfTab + tabOffset, folderTabHeight, width, folderTabHeight, width, height);
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

// TODO: Should adjust tab size based on font size?
//	@Override
//	public final void setFontSize(final double size) {
//	}

}
