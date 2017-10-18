package org.osate.ge.graphics.fx;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import org.osate.ge.fx.nodes.BusNode;
import org.osate.ge.fx.nodes.DeviceNode;
import org.osate.ge.fx.nodes.EllipseNode;
import org.osate.ge.fx.nodes.FeatureGroupTypeNode;
import org.osate.ge.fx.nodes.FolderNode;
import org.osate.ge.fx.nodes.LabelNode;
import org.osate.ge.fx.nodes.MemoryNode;
import org.osate.ge.fx.nodes.ModeNode;
import org.osate.ge.fx.nodes.ParallelogramNode;
import org.osate.ge.fx.nodes.PolygonNode;
import org.osate.ge.fx.nodes.PolylineNode;
import org.osate.ge.fx.nodes.ProcessorNode;
import org.osate.ge.fx.nodes.RectangleNode;
import org.osate.ge.fx.styling.HasBackgroundColor;
import org.osate.ge.fx.styling.HasFontColor;
import org.osate.ge.fx.styling.HasFontFont;
import org.osate.ge.fx.styling.HasLineWidth;
import org.osate.ge.fx.styling.HasOutlineColor;
import org.osate.ge.graphics.Graphic;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.internal.BusGraphic;
import org.osate.ge.graphics.internal.DeviceGraphic;
import org.osate.ge.graphics.internal.Ellipse;
import org.osate.ge.graphics.internal.FeatureGroupTypeGraphic;
import org.osate.ge.graphics.internal.FolderGraphic;
import org.osate.ge.graphics.internal.Label;
import org.osate.ge.graphics.internal.MemoryGraphic;
import org.osate.ge.graphics.internal.ModeGraphic;
import org.osate.ge.graphics.internal.Parallelogram;
import org.osate.ge.graphics.internal.Poly;
import org.osate.ge.graphics.internal.ProcessorGraphic;
import org.osate.ge.graphics.internal.Rectangle;

import com.google.common.collect.ImmutableMap;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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
		addCreator(mapBuilder, Rectangle.class, rg -> new RectangleNode(rg.rounded));
		addCreator(mapBuilder, Ellipse.class, eg -> new EllipseNode());
		addCreator(mapBuilder, FolderGraphic.class, fg -> new FolderNode());
		addCreator(mapBuilder, DeviceGraphic.class, dg -> new DeviceNode());
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
		addCreator(mapBuilder, Parallelogram.class, pg -> new ParallelogramNode(pg.horizontalOffset));
		addCreator(mapBuilder, BusGraphic.class, bg -> new BusNode());
		addCreator(mapBuilder, ProcessorGraphic.class, pg -> new ProcessorNode());
		addCreator(mapBuilder, MemoryGraphic.class, pg -> new MemoryNode());
		addCreator(mapBuilder, FeatureGroupTypeGraphic.class, fgtg -> new FeatureGroupTypeNode());
		addCreator(mapBuilder, ModeGraphic.class, mg -> {
			final ModeNode mn = new ModeNode();
			mn.setInitialMode(mg.isInitialMode);
			return mn;
		});
		addCreator(mapBuilder, Label.class, lg -> new LabelNode());
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
		if (node instanceof HasFontFont) {
			final Font font = new Font("Arial", style.getFontSize());
			((HasFontFont) node).setFont(font);
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
