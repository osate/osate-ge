package org.osate.ge.graphics.fx;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import org.osate.ge.fx.nodes.BusNode;
import org.osate.ge.fx.nodes.DeviceNode;
import org.osate.ge.fx.nodes.EllipseNode;
import org.osate.ge.fx.nodes.FolderNode;
import org.osate.ge.fx.nodes.ParallelogramNode;
import org.osate.ge.fx.nodes.PolygonNode;
import org.osate.ge.fx.nodes.PolylineNode;
import org.osate.ge.fx.nodes.RectangleNode;
import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasFontColor;
import org.osate.ge.fx.styling.HasFontSize;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;
import org.osate.ge.graphics.Graphic;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.internal.BusGraphic;
import org.osate.ge.graphics.internal.DeviceGraphic;
import org.osate.ge.graphics.internal.Ellipse;
import org.osate.ge.graphics.internal.FolderGraphic;
import org.osate.ge.graphics.internal.Parallelogram;
import org.osate.ge.graphics.internal.Poly;
import org.osate.ge.graphics.internal.Rectangle;

import com.google.common.collect.ImmutableMap;

import javafx.scene.Node;
import javafx.scene.paint.Color;

// TODO: Rename.. Stop using Age prefix?
public class AgeToFx {
	private static final Map<Class<? extends Graphic>, Function<? extends Graphic, Node>> creatorMap;

	private static <G extends Graphic> void addCreator(
			ImmutableMap.Builder<Class<? extends Graphic>, Function<? extends Graphic, Node>> mapBuilder, Class<G> c,
			Function<G, Node> creator) {
		mapBuilder.put(c, creator);
	}

	static {
		final ImmutableMap.Builder<Class<? extends Graphic>, Function<? extends Graphic, Node>> mapBuilder = new ImmutableMap.Builder<>();
		addCreator(mapBuilder, Rectangle.class, AgeToFx::createNodeForRectangle);
		addCreator(mapBuilder, Ellipse.class, AgeToFx::createNodeForEllipse);
		addCreator(mapBuilder, FolderGraphic.class, AgeToFx::createNodeForFolder);
		addCreator(mapBuilder, DeviceGraphic.class, AgeToFx::createNodeForDevice);
		addCreator(mapBuilder, Poly.class, poly -> {
			final double[] points = Arrays.stream(poly.getPoints()).flatMapToDouble(p -> DoubleStream.of(p.x, p.y)).toArray();
			switch (poly.type) {
			case POLYGON:
				return new PolygonNode(points);

			case POLYLINE:
				return new PolylineNode(points);

			default:
				throw new RuntimeException("Unhandled type: " + poly.type);
			}
		});
		addCreator(mapBuilder, Parallelogram.class, AgeToFx::createNodeForParallelogram);
		addCreator(mapBuilder, BusGraphic.class, bg -> new BusNode());
//		addGraphicsAlgorithmCreator(map, ProcessorGraphic.class,
//				AgeGraphitiGraphicsUtil::createGraphicsAlgorithmForProcessor);
//		addGraphicsAlgorithmCreator(map, MemoryGraphic.class,
//				AgeGraphitiGraphicsUtil::createGraphicsAlgorithmForMemory);
//		addGraphicsAlgorithmCreator(map, FeatureGroupTypeGraphic.class,
//				AgeGraphitiGraphicsUtil::createGraphicsAlgorithmForFeatureGroupType);

//		addGraphicsAlgorithmCreator(map, ModeGraphic.class, AgeGraphitiGraphicsUtil::createGraphicsAlgorithmForMode);
//		addGraphicsAlgorithmCreator(map, Label.class, AgeGraphitiGraphicsUtil::createGraphicsAlgorithmForLabel);
//		addGraphicsAlgorithmCreator(map, FeatureGraphic.class,

		creatorMap = mapBuilder.build();

	}

	// TODO: Document what graphics are supported
	public static Node createNode(final Graphic graphic) {
		@SuppressWarnings("unchecked")
		final Function<Graphic, Node> c = (Function<Graphic, Node>) creatorMap.get(graphic.getClass());
		if (c == null) {
			throw new RuntimeException("Unsupported object: " + graphic);
		}

		return c.apply(graphic);
	}

	// TODO: Cleanup. Some of this should be handled outside.. Such as setting the stroke type
	private static Node createNodeForEllipse(final Ellipse e) {
		final EllipseNode node = new EllipseNode();
		// TODO: Support initial style... Should be unified with other methods... Graphics could return a default style...
		// TODO: In most cases fields will be null. Line width and dash array might be different

//		shape.setStrokeWidth(r.lineWidth);
//		shape.getStrokeDashArray().setAll(20.0, 20.0); // TODO: Need to handle line style
//		// public final LineStyle lineStyle;
//

		return node;
	}

	private static Node createNodeForRectangle(final Rectangle r) {
		final RectangleNode node = new RectangleNode();
		node.setRounded(r.rounded);

		// TODO: Support initial style... Should be unified with other methods... Graphics could return a default style...
		// TODO: In most cases fields will be null. Line width and dash array might be different

//		shape.setStrokeWidth(r.lineWidth);
//		shape.getStrokeDashArray().setAll(20.0, 20.0); // TODO: Need to handle line style
//		// public final LineStyle lineStyle;
//

		return node;
	}

	private static Node createNodeForFolder(final FolderGraphic fg) {
		// TODO: Cleanup
		final FolderNode node = new FolderNode();
		// TODO: Set settings based on folder graphic
//		shape.setStrokeWidth(r.lineWidth);
//		shape.getStrokeDashArray().setAll(20.0, 20.0); // TODO: Need to handle line style

		return node;
	}

	private static Node createNodeForDevice(final DeviceGraphic dg) {
		// TODO: Cleanup
		final DeviceNode node = new DeviceNode();
		// TODO: Set settings based on folder graphic
//		shape.setStrokeWidth(r.lineWidth);
//		shape.getStrokeDashArray().setAll(20.0, 20.0); // TODO: Need to handle line style

		return node;
	}

	private static Node createNodeForParallelogram(final Parallelogram parallelogram) {
		return new ParallelogramNode(parallelogram.horizontalOffset);
	}

	// TODO
	// Ideally applying a style is as simple as :
	// AgeToFx.applyStyle(node, style);
	// This should work for:
	// Shape Nodes. Including those with annotations.
	// Diagram Element Nodes. However, it should not affect children. Just parts that are inherent.
	// Connections
	// TODO: Should have different definitions that restrict types to those actually supported
	public static void applyStyle(final Node node, final Style style) {
		if (!style.isComplete()) {
			throw new RuntimeException("Specified style must be complete");
		}

		if (node instanceof HasBackgroundColor) {
			((HasBackgroundColor) node).setBackgroundColor(ageToFxColor(style.getBackgroundColor()));
		}

		if (node instanceof HasOutlineColor) {
			((HasOutlineColor) node).setOutlineColor(ageToFxColor(style.getOutlineColor()));
		}

		if (node instanceof HasFontColor) {
			((HasFontColor) node).setFontColor(ageToFxColor(style.getFontColor()));
		}

		// TODO: Should be has font?
		if (node instanceof HasFontSize) {
			((HasFontSize) node).setFontSize(style.getFontSize());
		}

		if (node instanceof HasLineWidth) {
			((HasLineWidth) node).setLineWidth(style.getLineWidth());
		}

		// TODO: Line DashOffset
		// TODO: Label Position
		// s.getStrokeDashArray().setAll(20.0, 20.0); // TODO: Need to handle line style
	}

	private static Color ageToFxColor(final org.osate.ge.graphics.Color color) {
		return Color.rgb(color.getRed(), color.getGreen(), color.getBlue());
	}
}
