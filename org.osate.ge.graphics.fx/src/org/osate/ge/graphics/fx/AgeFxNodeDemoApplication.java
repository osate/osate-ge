package org.osate.ge.graphics.fx;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.osate.ge.fx.nodes.LabelNode;
import org.osate.ge.graphics.EllipseBuilder;
import org.osate.ge.graphics.Graphic;
import org.osate.ge.graphics.Point;
import org.osate.ge.graphics.PolyBuilder;
import org.osate.ge.graphics.RectangleBuilder;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.StyleBuilder;
import org.osate.ge.graphics.internal.BusGraphicBuilder;
import org.osate.ge.graphics.internal.DeviceGraphicBuilder;
import org.osate.ge.graphics.internal.FeatureGroupTypeGraphicBuilder;
import org.osate.ge.graphics.internal.FolderGraphicBuilder;
import org.osate.ge.graphics.internal.LabelBuilder;
import org.osate.ge.graphics.internal.MemoryGraphicBuilder;
import org.osate.ge.graphics.internal.ModeGraphicBuilder;
import org.osate.ge.graphics.internal.ParallelogramBuilder;
import org.osate.ge.graphics.internal.ProcessorGraphicBuilder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// TODO: AgeToFx if methods are single line, they should be inlined
// TODO: Remove rectangle. Use parallelogram node
// TODO: Condering renaming graphics and nodes...

// TODO: Do style rework/cleanup
// TODO: Cleanup setting default style. It's the same for every class. Have an internal method that sets it.

// TODO: LEARN
//  Understand stroke type better.. Seems to cause problems in some cases. Paths can be double drawn or not drawn at all.
//  Should just use line segments in that case?
//  Default styles for all shapes. Test without applying style

// TODO: Interface for styling.
//  Rename to something else?
//  Font size, line width, line style, and colors
//  Include rotation? Only 90 degrees?
//  Flag to set resizability? Part of creation process to disable resizing? Features could have a reasonable default.
//  Interface could be a general purpose interface for working with node. Have a getNode() method to avoid having to cast?

// TODO: Cleanup existing node implementation. KISS

// TODO: Annotation. Part of AgeGraphic so it should be part of node.
// TODO: Font Size should affect annotations.

// TODO: Nodes for AADL objects

// TODO: Node for actual editable objects.
//   Primary Label
//   Annotations
//   Layout
//   Docking
// TODO: Connections. Need to use GEF.
// TODO: Arrow Head
// TODO: Connection Decorators
// TODO: Age graphic should have a flag indicates which style fields it supports.

// Consider name of package.. Most of this will be FX and not GEF specific.

// TODO: Instead of trying to use GEF's geometry, stick with plain JavaFX. Create Java FX nodes for complex shapes.
// TODO: Create a region and scale appropriately. Could potentially have a region that contains multiple geometry nodes...
// TODO: Will need to resize like geometry node. Current implementations are much simpler. Is GeometryNode overly complicated or is this implementation overly simple?
// TODO: Will need to implement a GeometryOutlineProvider. Could use conversion method along with requiring all out shapes to implement and interface
// that provides an outline JavaFX shape.
// TODO: Will need to have an interface to support setting background color, outline color, and line width
// TODO: Should graphics have fixed offsets or should they be completely scalable. (Not including things like folder which need fixed height for tabs)
// TODO: At some point, folder tab size may need to be dynamic based on font size or name width.
// TODO: An aggregate object is needed because not all stroke and fill colors may be the same? Currently using hacks to give
// appearance of different colors. Having multiple nodes would provide the greatest flexibility in deciding what is filled, colors, etc
// and could potentially be simper.

// TODO: Consolidate AGE Graphics. Need to handle scaling properly. For example folders have fixed tab height and a calculation for tab width.
// (Although that may need to be dynamic based on font size?)
// TODO: Switch to double precision in graphics and other classes.

// TODO: Rename. GEF Demo. What is point of GeometryNode

public class AgeFxNodeDemoApplication extends Application {
	public static void main(final String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		final GridPane container = new GridPane();// VBox container = new VBox();
		container.setHgap(10.0);
		container.setVgap(10.0);
		container.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

		// TODO: Need with various style. Default and other. Columns?
		// TODO: Need polygon and polyline

		final Graphic[] graphics = new Graphic[] { RectangleBuilder.create().build(),
				RectangleBuilder.create().rounded().build(),
				EllipseBuilder.create().build(), FolderGraphicBuilder.create().build(),
				DeviceGraphicBuilder.create().build(), ParallelogramBuilder.create().horizontalOffset(20).build(),
				BusGraphicBuilder.create().build(),
				PolyBuilder.create().polygon().points(new Point(0.0, 1.0), new Point(1.0, 1.0), new Point(0.5, 0.0))
				.build(),
				PolyBuilder.create().polyline().points(new Point(0.0, 0.0), new Point(1.0, 0.5), new Point(0.0, 1.0))
				.build(),
				ProcessorGraphicBuilder.create().build(), MemoryGraphicBuilder.create().build(),
				FeatureGroupTypeGraphicBuilder.create().build(), ModeGraphicBuilder.create().build(),
				ModeGraphicBuilder.create().initialMode().build(), LabelBuilder.create().build() };

		final List<Node> nodes = Arrays.stream(graphics).map(AgeToFx::createNode).collect(Collectors.toList());

		// Set the text for label nodes
		for (final Node n : nodes) {
			if (n instanceof LabelNode) {
				((LabelNode) n).setText("This is a test");
			}
		}

		final Style style = StyleBuilder.create(Style.DEFAULT).fontSize(32.0)
				.outlineColor(org.osate.ge.graphics.Color.BLUE)
				.backgroundColor(org.osate.ge.graphics.Color.CYAN).build();

		// Add Nodes and Assign them to Rows and Columns
		final int numberOfColumns = 2;
		int row = 0, col = -1;
		for (final Node node : nodes) {
			AgeToFx.applyStyle(node, style); // TODO: Need to continually test without style.. Have option to disable?

			// Increment the row and column first so that the final values will be the indices of the last node
			col++;
			if (col > (numberOfColumns - 1)) {
				col = 0;
				row++;
			}

			container.add(node, col, row);
		}

		// Create Row and Column Constraints
		for (int i = 0; i < numberOfColumns; i++) {
			ColumnConstraints c = new ColumnConstraints(100, 100, Double.MAX_VALUE);
			c.setHgrow(Priority.SOMETIMES);
			container.getColumnConstraints().add(c);
		}

		for (int i = 0; i <= row; i++) {
			RowConstraints rc = new RowConstraints(100, 100, Double.MAX_VALUE);
			rc.setVgrow(Priority.SOMETIMES);
			container.getRowConstraints().add(rc);
		}

		primaryStage.setScene(new Scene(container));

		// Setup the stage
		primaryStage.setResizable(true);
		primaryStage.setWidth(640);
		primaryStage.setHeight(900);
		primaryStage.setTitle("GEF Graphics");
		primaryStage.show();
	}
}
