package org.osate.ge.gef;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

// TODO: Move to other package
// TODO: Have a generic PaletteModel/ToolModel or something?
// Would be useful for programmatically activating tools, etc?

public class Palette extends VBox { // TODO: Extend something else?
	public Palette() {
		this.setId("palette");

		// TODO: Cleanup. Merger
		final EventHandler<ActionEvent> compartmentToggleHandler = event -> {
			final Node contents = ((Node) ((Node) event.getSource()).getUserData());
			contents.setVisible(!contents.isVisible());
		};

		// TODO: Turn into widget.
		// TODO: Turn compartment into node. Contains label and contents
		// TODO: Stylng - Item Style. Should look like old palette. Icon and label.
		// TODO: Styling - Compartment Header style. Look like old palette.
		// TODO: Compartments. Is is possible to use toggle buttons for items and for all compartment items to share the same toggle button.
		// TODO: Heading Style. Palette. Toggle Button, etc
		// TODO: Scrollable
		// TODO: Setting divider position based on some shown event on the split panel rather than after calling show on scene

		final ToggleButton compartment1 = new ToggleButton("Group 1");
		compartment1.setOnAction(compartmentToggleHandler);
		final VBox compartment1Contents = new VBox();
		compartment1.setUserData(compartment1Contents);
		compartment1Contents.managedProperty().bind(compartment1Contents.visibleProperty());
		compartment1Contents.getChildren().addAll(new Button("Item 1"), new Button("Item2"));
		// group1Contents.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
		compartment1Contents.setPadding(new Insets(0.0, 0.0, 0.0, 10.0));

		final ToggleButton compartment2 = new ToggleButton("Group 2");
		compartment2.setOnAction(compartmentToggleHandler);
		final VBox compartment2Contents = new VBox();
		compartment2.setUserData(compartment2Contents);
		compartment2Contents.managedProperty().bind(compartment2Contents.visibleProperty());
		compartment2Contents.getChildren().addAll(new Button("Item 1"), new Button("Item2"));
		compartment2Contents.setPadding(new Insets(0.0, 0.0, 0.0, 10.0));

		final ToggleButton compartment3 = new ToggleButton("Group 3");
		compartment3.setOnAction(compartmentToggleHandler);
		final VBox compartment3Contents = new VBox();
		compartment3.setUserData(compartment3Contents);
		compartment3Contents.managedProperty().bind(compartment3Contents.visibleProperty());
		compartment3Contents.getChildren().addAll(new Button("Item 1"), new Button("Item2"));
		compartment3Contents.setPadding(new Insets(0.0, 0.0, 0.0, 10.0));

		getChildren().addAll(compartment1, compartment1Contents, compartment2, compartment2Contents, compartment3,
				compartment3Contents);
	}
}
