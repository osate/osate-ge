package org.osate.ge.gef.nodes;

import org.eclipse.gef.fx.nodes.Connection;
import org.osate.ge.fx.nodes.BusNode;

import javafx.beans.value.ChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

// TODO
public class AgeShapeNode extends Region {
	private final Region shape = new BusNode(); // TODO: Rework
	private final Label label;
	private double shapeWidth = -1;
	private double shapeHeight = -1;
	// TODO: Use group to manage child features?
	// private final Pane childContainer = new Pane();

	public AgeShapeNode() {
		shape.setManaged(false);

		// Label experiment
		label = new Label("GEF5 Test");
		label.setLayoutX(-50);
		label.setManaged(false);

		getChildren().addAll(shape, label);

		// TODO: How to remove listener.
		// TODO: Could use invalidation listener?
		layoutBoundsProperty().addListener((ChangeListener<Bounds>) (observable, oldValue, newValue) -> {
			if(newValue.getWidth() > 2 && newValue.getHeight() > 2) {
				shape.resize(newValue.getWidth(), newValue.getHeight());
			}
		});
	}

	// TODO: Rename. Not a shape. Shouldn't be exposed?
	public Region getInnerShape() {
		return shape;
	}

	@Override
	protected void layoutChildren() {
		// //we directly layout our children from within resize
		// Size the label
		label.resize(label.prefWidth(-1), label.prefHeight(-1));
		super.layoutChildren();

		// TODO: Get bounds in parent is including unmanaged shapes such as the label...
		// TODO: Instead of using localToParent, just add translation and layout offset manually? This will introduce scaling which is
		// typically not done as part of layout? Could just document. Shouldn't cause problems.
		double minX = 0;
		double minY = 0;
		for (final Node child : getChildren()) {
			if (child.isManaged()) {
				// TODO: Need to determine the proper way to do this. The idea is to get the bounds of the shape before the layout
				// X and Y translation has been added. This will allow the shape shrink based on it's preferred size.
				final Bounds layoutBoundsInParentFinal = child.localToParent(child.getLayoutBounds());
				final double offsetX = child.getLayoutX();
				final double offsetY = child.getLayoutY();
				final Bounds layoutBoundsInParent = new BoundingBox(layoutBoundsInParentFinal.getMinX() - offsetX,
						layoutBoundsInParentFinal.getMinY() - offsetY, layoutBoundsInParentFinal.getWidth(),
						layoutBoundsInParentFinal.getHeight());
				minX = Math.min(minX, layoutBoundsInParent.getMinX());
				minY = Math.min(minY, layoutBoundsInParent.getMinY());
			}
		}

		if (minX < 0 || minY < 0) {
			for (final Node child : getChildren()) {
				// TODO: Assume connections will be marked as unmanaged
				if (child.isManaged()) {
					child.setLayoutX(-minX);
					child.setLayoutY(-minY);
				}
			}
		}

		// TODO: Appropriate/Necessary?
		requestLayout();

		// getParent().requestLayout();
		// TODO: When to request layout?

		// TODO: Position and size other children

		// TODO: Position and size label

		// shape.setLayoutX(0);
		// shape.setPrefWidth(200); // TODO: How should this work?
		// shape.setPrefWidth(value);

		// super.layoutChildren();

		// this.getLay

		// TODO: Use translateX and Y to handle negative coordinates from children? Would message up feature groups?
		// TOOD: In any case, decide how to handle

	};

	// TODO: Why use getBoundsInParent() for some things and getLayoutBounds()
	// TODO: getBoundsInParent includes unmanaged
	// TODO: Will be needed when nested?
	@Override
	protected double computePrefWidth(final double height) {
		double w = 100; // TODO
		for (final Node child : getChildren()) {
			// TODO: Connections should be unmanaged?
			if (child.isManaged() && !(child instanceof Connection) && child != shape) {
				w = Math.max(w, child.getBoundsInParent().getMaxX());
			}
		}

		w = Math.max(w, shapeWidth);

		return w;
	}

	@Override
	protected double computePrefHeight(final double width) {
		double h = 100; // TODO
		for (final Node child : getChildren()) {
			// TODO: Connections should be unmanaged?
			if (child.isManaged() && !(child instanceof Connection) && child != shape) {
				h = Math.max(h, child.getBoundsInParent().getMaxY());
			}
		}

		h = Math.max(h, shapeHeight);

		return h;
	}

	// TODO: Change behavior. Should just be getChildren() but we inherit from Group
	public void addInnerChild(final Node child) {
		/* childContainer. */getChildren().add(child);
	}

	public void removeInnerChild(final Node child) {
		/* childContainer. */getChildren().remove(child);
	}

	// Sets the preferred size of the shape. The actual size may be different depending on children.
	public final void setPreferredShapeSize(final double width, final double height) {
		shapeWidth = width;
		shapeHeight = height;

		requestLayout();
	}
}
