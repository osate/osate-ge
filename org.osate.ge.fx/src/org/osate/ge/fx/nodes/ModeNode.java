package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class ModeNode extends Region implements HasBackgroundColor, HasOutlineColor, HasLineWidth {
	private final static double initialModeEllipseRadius = 5;
	private final static double spacingBetweenInitialModeEllipseAndMode = 10;
	private final static double initialModeAreaHeight = 2.0 * initialModeEllipseRadius
			+ spacingBetweenInitialModeEllipseAndMode;
	private final int arrowSize = 8;

	private final Path outline = new Path();
	private final Circle initialModeCircle = new Circle(initialModeEllipseRadius);
	private final CubicCurve initialModeCurve = new CubicCurve();
	private final Path initialModeArrow = new Path();
	private BooleanProperty initialModeProperty = new SimpleBooleanProperty();
	private BooleanProperty initialModeIndicatorInBoundsProperty = new SimpleBooleanProperty();

	public ModeNode() {
		// Set initial settings and visibility for the initial mode indicator
		initialModeCircle.setStroke(null);
		initialModeCircle.setFill(Color.BLACK);
		initialModeCircle.setVisible(false);

		initialModeCurve.setStroke(Color.BLACK);
		initialModeCurve.setFill(null);
		initialModeCurve.setVisible(false);

		initialModeArrow.setStroke(null);
		initialModeArrow.setFill(Color.BLACK);
		initialModeArrow.setVisible(false);

		// Add Children
		this.getChildren().addAll(outline, initialModeCircle, initialModeCurve, initialModeArrow);

		// Set initial style
		setLineWidth(2);
		setBackgroundColor(Color.WHITE);
		setOutlineColor(Color.BLACK);
		setInitialMode(false);
		setInitialModeIndicatorInBounds(true);

		initialModeProperty.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			final boolean newValuePrim = newValue.booleanValue();
			initialModeCircle.setVisible(newValuePrim);
			initialModeCurve.setVisible(newValuePrim);
			initialModeArrow.setVisible(newValuePrim);

			// Call a resize to relayout and configure the internal nodes
			resize(getWidth(), getHeight());
		});

		initialModeIndicatorInBoundsProperty
		.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			// Call a resize to relayout and configure the internal nodes
			resize(getWidth(), getHeight());
		});
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void resize(double width, double height) {
		super.resize(width, height);

		final double modeTop;
		final double modeHeight;
		if (initialModeProperty.get() && initialModeIndicatorInBoundsProperty.get()) {
			modeTop = initialModeAreaHeight;
			modeHeight = Math.max(0, height - initialModeAreaHeight);
		} else {
			modeTop = 0.0;
			modeHeight = height;
		}

		outline.getElements().setAll(new MoveTo(0, modeTop + modeHeight * 0.5), new LineTo(width * 0.25, modeTop),
				new HLineTo(width * 0.75), new LineTo(width, modeTop + modeHeight * 0.5),
				new LineTo(width * 0.75, modeTop + modeHeight), new HLineTo(width * 0.25),
				new LineTo(0, modeTop + modeHeight * 0.5));

		initialModeCircle
		.setCenterX(Math.max(0, Math.max(width / 2 - 50, width * 0.25) + initialModeCircle.getRadius()));
		initialModeCircle.setCenterY(initialModeCircle.getRadius()
				+ (initialModeIndicatorInBoundsProperty.get() ? 0.0 : -initialModeAreaHeight));

		initialModeCurve.setStartX(initialModeCircle.getCenterX() + initialModeCircle.getRadius());
		initialModeCurve.setStartY(initialModeCircle.getCenterY());
		initialModeCurve.setEndX(width / 2.0);
		initialModeCurve.setEndY(modeTop - arrowSize);

		final double dx = initialModeCurve.getEndX() - initialModeCurve.getStartX();
		final double dy = initialModeCurve.getEndY() - initialModeCurve.getStartY();
		initialModeCurve.setControlX1(initialModeCurve.getStartX() + dx * 1.0);
		initialModeCurve.setControlY1(initialModeCurve.getStartY() + dy * 0.0);
		initialModeCurve.setControlX2(initialModeCurve.getStartX() + dx * 1.0);
		initialModeCurve.setControlY2(initialModeCurve.getStartY() + dy * 0.6);

		// Create arrow
		initialModeArrow.getElements().setAll(new MoveTo(-arrowSize / 2.0, 0), new LineTo(0, arrowSize),
				new LineTo(arrowSize / 2.0, 0), new ClosePath());
		initialModeArrow.setLayoutX(initialModeCurve.getEndX());
		initialModeArrow.setLayoutY(modeTop - arrowSize);
	}

	@Override
	public final void setBackgroundColor(final Color value) {
		outline.setFill(value);
	}

	@Override
	public final void setOutlineColor(final Color value) {
		outline.setStroke(value);
	}

	@Override
	public final void setLineWidth(final double value) {
		outline.setStrokeWidth(value);
		initialModeCircle.setStrokeWidth(value);
		initialModeCurve.setStrokeWidth(value);
	}

	public final void setInitialMode(final boolean value) {
		initialModeProperty.setValue(value);
	}

	public final void setInitialModeIndicatorInBounds(final boolean value) {
		initialModeIndicatorInBoundsProperty.set(value);
	}

	public static void main(final String[] args) {
		final ModeNode m1 = new ModeNode();
		final ModeNode m2 = new ModeNode();
		m2.setInitialMode(true);
		m2.setInitialModeIndicatorInBounds(true);
		NodeApplication.run(m1, m2);
	}
}
