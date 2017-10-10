package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasFontColor;
import org.osate.ge.fx.styling.HasFontSize;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

// TODO: Finish implementing. Need to avoid duplicate calculations.

public class LabelNode extends Group implements HasBackgroundColor, HasFontColor, HasFontSize {
	private final Rectangle background = new Rectangle();
	private final Text text = new Text();

	public LabelNode(final String txt) {
		this.getChildren().addAll(background, text);
		setBackgroundColor(Color.WHITE);
		setBackgroundColor(Color.RED);
		setFontColor(Color.BLACK);
		setText(txt);
		setAutoSizeChildren(false);
	}

	public LabelNode() {
		this("");
	}

	// TODO: Opacity
	@Override
	public final void setBackgroundColor(final Color value) {
		background.setFill(value);
	}


	@Override
	public void setFontSize(double size) {
		text.setFont(new Font("Arial", size)); // TODO: Avoid hardcoding font...
		layoutTest(); // TODO: Finish
	}

	@Override
	public void setFontColor(final Color value) {
		text.setFill(value);
	}

	public void setText(final String value) {
		text.setText(value);
		layoutTest(); // TODO: Finish
	}

	// TODO: Would prefer to to this only once after all the font size changes and text have been set. Could have custom layout?
	private void layoutTest() { // TODO: Rename.
		// TODO: Understand layout bounds
		final Bounds textBounds = text.getLayoutBounds();
		background.setWidth(textBounds.getWidth());
		background.setHeight(textBounds.getHeight());
		text.setLayoutY(-textBounds.getMinY());
	}

	public static void main(final String[] args) {
		NodeApplication.run(() -> new Node[] { new LabelNode("This is a test") }); // Use the supplier variant because setting the text doesn't work unless
		// JavaFX has been initialized.
	}
}
