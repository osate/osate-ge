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
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.EdgeTagCreator;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.OrthogonalVisibilityGraphFactory.NodeTagCreator;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.Rectangle;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.VerticalSegment;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy.HierarchicalEdgeTag;
import org.osate.ge.internal.diagram.runtime.layout.connections.orthogonal.visibilityGraph.hierarchy.HierarchicalNodeTag;

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
		final OrthogonalSegmentsFactoryDataSource<ExampleModel.TestElement> ds = ExampleModel.createDataSource();
		startWithDataSource(primaryStage, ds, HierarchicalNodeTag.createHierarchicalNodeTagCreator(ds),
				HierarchicalEdgeTag.createHierarchicalEdgeTagCreator(ds));
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
		for (final T obj : ds.getElements()) {
			drawObjectIfBounded(gc, ds, obj);
		}

		// Draw Segments
		gc.setStroke(Color.GREY);
		gc.setLineWidth(1);
		gc.setLineDashes(1.0, 5.0);
		for (final HorizontalSegment<T> hs : segments.getHorizontalSegments()) {
			gc.strokeLine(Math.max(0, hs.getMinX()), hs.getY(), Math.min(1000, hs.getMaxX()), hs.getY());
		}

		for (final VerticalSegment<T> vs : segments.getVerticalSegments()) {
			gc.strokeLine(vs.x, Math.max(0, vs.minY), vs.x, Math.min(1000, vs.maxY));
		}

		gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));

		// Draw Nodes
		final boolean drawNodesAndEdges = true;
		if (drawNodesAndEdges) {
			for (final OrthogonalGraphNode<?, ?> n : graph.getNodes()) {
				gc.setFill(Color.BLUE);
				gc.fillOval(n.getPosition().x - halfNodeIndicatorSize, n.getPosition().y - halfNodeIndicatorSize, nodeIndicatorSize,
						nodeIndicatorSize);
			}

			// Draw Edge
			gc.setLineDashes(0.0);
			for (final OrthogonalGraphNode<?, ?> n : graph.getNodes()) {
				gc.setStroke(Color.RED);
				final OrthogonalGraphEdge<?, ?> rightEdge = n.getEdge(OrthogonalDirection.RIGHT);
				if (rightEdge != null) {
					drawEdge(gc, n.getPosition().x + halfNodeIndicatorSize, n.getPosition().y,
							rightEdge.getNode().getPosition().x - halfNodeIndicatorSize, rightEdge.getNode().getPosition().y,
							rightEdge.getTag());
				}

				gc.setStroke(Color.GREEN);
				final OrthogonalGraphEdge<?, ?> downEdge = n.getEdge(OrthogonalDirection.DOWN);
				if (downEdge != null) {
					drawEdge(gc, n.getPosition().x, n.getPosition().y + halfNodeIndicatorSize, downEdge.getNode().getPosition().x,
							downEdge.getNode().getPosition().y - halfNodeIndicatorSize, downEdge.getTag());
				}
			}
		}
	}

	private void drawEdge(final GraphicsContext gc, final double x1, final double y1, final double x2, final double y2,
			Object tag) {
		gc.strokeLine(x1, y1, x2, y2);

		if (tag instanceof HierarchicalEdgeTag) {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.fillText(Integer.toString(((HierarchicalEdgeTag) tag).getLevelsCrossed()), (x1 + x2) / 2.0, (y1 + y2) / 2.0);
		}
	}

	private <T> void drawObjectIfBounded(final GraphicsContext gc, final OrthogonalSegmentsFactoryDataSource<T> ds,
			final T obj) {
		final Rectangle bounds = ds.getBounds(obj);
		if (bounds != null) {
			gc.setStroke(Color.BLACK);
			gc.setLineWidth(4);
			gc.strokeRect(bounds.getMin().x, bounds.getMin().y, bounds.getMax().x - bounds.getMin().x, bounds.getMax().y - bounds.getMin().y);
		}
	}

}
