package org.osate.ge.internal.diagram.runtime.layout.connections.orthogonalVisibilityGraph.test;

import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalDirection;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraph;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphEdge;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.graph.OrthogonalGraphNode;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.HorizontalSegment;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalSegments;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalSegmentsFactory;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalSegmentsFactoryDataSource;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.Rectangle;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.VerticalSegment;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.EdgeTagCreator;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.NodeTagCreator;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy.EdgeHierarchy;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy.NodeHierarchy;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class TestApp extends Application {
	private static final double nodeIndicatorSize = 10;
	private static final double halfNodeIndicatorSize = nodeIndicatorSize / 2.0;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		final OrthogonalSegmentsFactoryDataSource<TestModel.TestElement> ds = TestModel.createDataSource();
		startWithDataSource(primaryStage, ds, NodeHierarchy.createNodeHierarchyCreator(ds),
				EdgeHierarchy.createEdgeHierarchyCreator(ds));
	}

	private <T, NodeTag, EdgeTag> void startWithDataSource(final Stage primaryStage,
			final OrthogonalSegmentsFactoryDataSource<T> ds, final NodeTagCreator<T, NodeTag> nodeTagCreator,
			final EdgeTagCreator<NodeTag, EdgeTag> edgeTagCreator) {
		// OrthogonalVisibilityGraph.create(testDataSource);
		final OrthogonalSegments<T> segments = OrthogonalSegmentsFactory.create(ds);
		final OrthogonalGraph<NodeTag, EdgeTag> graph = OrthogonalVisibilityGraphFactory.create(segments,
				nodeTagCreator, edgeTagCreator);

		primaryStage.setTitle("Connection Layout Test");
		final Group root = new Group();
		final Canvas canvas = new Canvas(800, 500);

		final GraphicsContext gc = canvas.getGraphicsContext2D();
		draw(gc, ds, graph, segments);
		root.getChildren().add(canvas);
		primaryStage.setScene(new Scene(root));
		primaryStage.show();

	}

	private <T> void draw(final GraphicsContext gc, final OrthogonalSegmentsFactoryDataSource<T> ds,
			final OrthogonalGraph<?, ?> graph,
			final OrthogonalSegments<T> segments) {
		// Draw the objects
		for (final T obj : ds.getObjects()) {
			drawObjectIfBounded(gc, ds, obj);
		}

		// Draw Segments
		gc.setStroke(Color.GREY);
		gc.setLineWidth(1);
		gc.setLineDashes(1.0, 5.0);
		for (final HorizontalSegment<T> hs : segments.horizontalSegments) {
			gc.strokeLine(Math.max(0, hs.minX), hs.y, Math.min(1000, hs.maxX), hs.y);
		}

		for (final VerticalSegment<T> vs : segments.verticalSegments) {
			gc.strokeLine(vs.x, Math.max(0, vs.minY), vs.x, Math.min(1000, vs.maxY));
		}

		gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

		// Draw Nodes
		final boolean drawNodesAndEdges = true;// false; // TODO: Reenable
		if (drawNodesAndEdges) {
			for (final OrthogonalGraphNode<?, ?> n : graph.getNodes()) {
				gc.setFill(Color.BLUE);
				gc.fillOval(n.position.x - halfNodeIndicatorSize, n.position.y - halfNodeIndicatorSize, nodeIndicatorSize,
						nodeIndicatorSize);
			}

			// Draw Edge
			gc.setLineDashes(0.0);
			for (final OrthogonalGraphNode<?, ?> n : graph.getNodes()) {
				gc.setStroke(Color.RED);
				final OrthogonalGraphEdge<?, ?> rightEdge = n.getEdge(OrthogonalDirection.RIGHT);
				if (rightEdge != null) {
					drawEdge(gc, n.position.x + halfNodeIndicatorSize, n.position.y,
							rightEdge.node.position.x - halfNodeIndicatorSize, rightEdge.node.position.y,
							rightEdge.tag);
				}

				gc.setStroke(Color.GREEN);
				final OrthogonalGraphEdge<?, ?> downEdge = n.getEdge(OrthogonalDirection.DOWN);
				if (downEdge != null) {
					drawEdge(gc, n.position.x, n.position.y + halfNodeIndicatorSize, downEdge.node.position.x,
							downEdge.node.position.y - halfNodeIndicatorSize, downEdge.tag);
				}
			}
		}
	}

	private void drawEdge(final GraphicsContext gc, final double x1, final double y1, final double x2, final double y2,
			Object tag) {
		gc.strokeLine(x1, y1, x2, y2);

		if (tag instanceof EdgeHierarchy) {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.fillText(Integer.toString(((EdgeHierarchy) tag).depth), (x1 + x2) / 2.0, (y1 + y2) / 2.0);
		}
	}

	private <T> void drawObjectIfBounded(final GraphicsContext gc, final OrthogonalSegmentsFactoryDataSource<T> ds,
			final T obj) {
		final Rectangle bounds = ds.getBounds(obj);
		if (bounds != null) {
			gc.setStroke(Color.BLACK);
			gc.setLineWidth(4);
			gc.strokeRect(bounds.min.x, bounds.min.y, bounds.max.x - bounds.min.x, bounds.max.y - bounds.min.y);
		}
	}

}
