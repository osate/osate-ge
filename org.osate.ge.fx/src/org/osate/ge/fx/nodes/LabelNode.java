package org.osate.ge.fx.nodes;

import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasFontColor;
import org.osate.ge.fx.styling.HasFontFont;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

// TODO: Finish implementing. Need to avoid duplicate calculations.

public class LabelNode extends Group implements HasBackgroundColor, HasFontColor, HasFontFont {
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

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		// TODO: Understand layout bounds
		final Bounds textBounds = text.getLayoutBounds();
		background.setWidth(textBounds.getWidth());
		background.setHeight(textBounds.getHeight());
		text.setLayoutY(-textBounds.getMinY());

	}

	@Override
	public final void setBackgroundColor(final Color value) {
		background.setFill(value);
	}

	@Override
	public void setFont(final Font font) {
		text.setFont(font);
		requestLayout();
	}

	@Override
	public void setFontColor(final Color value) {
		text.setFill(value);
	}

	public void setText(final String value) {
		text.setText(value);
		requestLayout();
	}

	public static void main(final String[] args) {
		NodeApplication.run(() -> new Node[] { new LabelNode("This is a test") }); // Use the supplier variant because setting the text doesn't work unless
		// JavaFX has been initialized.
	}
}
