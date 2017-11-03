package org.osate.ge.internal.diagram.runtime.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.HierarchyHandling;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.SizeConstraint;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.graph.ElkConnectableShape;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.internal.AgeConnection;
import org.osate.ge.graphics.internal.AgeShape;
import org.osate.ge.graphics.internal.Label;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.diagram.runtime.styling.StyleProvider;

class ElkGraphBuilder {
	private final StyleProvider styleProvider;
	private final LayoutInfoProvider layoutInfoProvider;

	private ElkGraphBuilder(final StyleProvider styleProvider, final LayoutInfoProvider layoutInfoProvider) {
		this.styleProvider = Objects.requireNonNull(styleProvider, "styleProvider must not be null");
		this.layoutInfoProvider = Objects.requireNonNull(layoutInfoProvider, "layoutInfoProvider must not be null");
	}

	/**
	 *
	 * @param rootDiagramNode
	 * @param styleProvider is a style provider which provides the style for the diagram elements. The style provider is expected to return a final style. The style must not contain null values.
	 * @param layoutInfoProvider is the layout info provider which is used to determine label sizes.
	 * @return
	 */
	static LayoutMapping buildLayoutGraph(final DiagramNode rootDiagramNode, final StyleProvider styleProvider,
			final LayoutInfoProvider layoutInfoProvider) {
		final ElkGraphBuilder graphBuilder = new ElkGraphBuilder(styleProvider, layoutInfoProvider);
		return graphBuilder.buildLayoutGraph(rootDiagramNode);
	}

	private LayoutMapping buildLayoutGraph(final DiagramNode rootDiagramNode) {
		// Create the graph
		final LayoutMapping mapping = new LayoutMapping(null);
		final ElkNode rootNode = ElkGraphUtil.createGraph();
		rootNode.setProperty(CoreOptions.DIRECTION, Direction.RIGHT);
		rootNode.setProperty(CoreOptions.HIERARCHY_HANDLING, HierarchyHandling.INCLUDE_CHILDREN);

		if (rootDiagramNode instanceof AgeDiagram) {
			final ElkNode diagramElkNode = ElkGraphUtil.createNode(rootNode);
			mapping.getGraphMap().put(diagramElkNode, rootDiagramNode);
			createElkGraphElementsForNonLabelChildShapes(rootDiagramNode, diagramElkNode, mapping);
		} else if (rootDiagramNode instanceof DiagramElement) {
			createElkGraphElementsForElements(Collections.singleton((DiagramElement) rootDiagramNode), rootNode,
					mapping);
		}

		createElkGraphElementsForConnections(rootDiagramNode, mapping);

		mapping.setLayoutGraph(rootNode);

		return mapping;
	}

	private void createElkGraphElementsForNonLabelChildShapes(final DiagramNode parentNode, final ElkNode parent,
			final LayoutMapping mapping) {
		createElkGraphElementsForElements(parentNode.getDiagramElements(), parent, mapping);
	}

	private void createElkGraphElementsForElements(final Collection<DiagramElement> elements,
			final ElkNode parent, final LayoutMapping mapping) {
		elements.stream().filter(de -> de.getGraphic() instanceof AgeShape && !(de.getGraphic() instanceof Label))
		.forEachOrdered(de -> {
			createElkGraphElementForNonLabelShape(de, parent, mapping)
			.ifPresent(newLayoutElement -> createElkLabels(de, newLayoutElement, mapping));
		});
	}

	private Optional<ElkGraphElement> createElkGraphElementForNonLabelShape(final DiagramElement de,
			final ElkNode layoutParent, final LayoutMapping mapping) {
		if (de.getDockArea() == null) {
			final ElkNode newNode = ElkGraphUtil.createNode(layoutParent);
			mapping.getGraphMap().put(newNode, de);
			setShapePositionAndSize(newNode, de);

			final EnumSet<SizeConstraint> nodeSizeConstraints = EnumSet.of(SizeConstraint.PORTS,
					SizeConstraint.MINIMUM_SIZE, SizeConstraint.NODE_LABELS);
			newNode.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, nodeSizeConstraints);
			newNode.setProperty(CoreOptions.INSIDE_SELF_LOOPS_ACTIVATE, true);

			// Create Children
			createElkGraphElementsForNonLabelChildShapes(de, newNode, mapping);

			return Optional.ofNullable(newNode);
		} else { // Docked
			// Create Port
			final ElkPort newPort = ElkGraphUtil.createPort(layoutParent);
			mapping.getGraphMap().put(newPort, de);
			setShapePositionAndSize(newPort, de);

			// Don't create graph elements for children. An ELK port cannot have child ports.

			return Optional.ofNullable(newPort);
		}
	}

	private static void setShapePositionAndSize(final ElkShape shape, final DiagramElement de) {
		if (de.hasPosition()) {
			shape.setLocation(de.getX(), de.getY());
		}

		if (de.hasSize()) {
			shape.setDimensions(de.getWidth(), de.getHeight());
		}
	}

	private void createElkLabels(final DiagramElement parentElement, final ElkGraphElement parentLayoutElement,
			final LayoutMapping mapping) {
		// Don't create labels for ElkPort. The bounds of the port contain their labels.
		if (parentLayoutElement instanceof ElkPort) {
			return;
		}

		final boolean isConnection = parentElement.getGraphic() instanceof AgeConnection;

		final Style style = styleProvider.getStyle(parentElement);
		if (style.getPrimaryLabelVisible()) {
			// Create Primary Label
			if (parentElement.getName() != null) {
				final ElkLabel elkLabel = createElkLabel(parentLayoutElement, parentElement.getName(),
						layoutInfoProvider.getPrimaryLabelSize(parentElement));
				if (isConnection) {
					mapping.getGraphMap().put(elkLabel, new PrimaryConnectionLabelReference(parentElement));
				}
			}
		}

		// Create Secondary Labels
		parentElement.getDiagramElements().stream().filter(c -> c.getGraphic() instanceof Label)
		.forEachOrdered(labelElement -> {
			final ElkLabel elkLabel = createElkLabel(parentLayoutElement, labelElement.getName(),
					labelElement.getSize());
			if (isConnection) {
				mapping.getGraphMap().put(elkLabel, new SecondaryConnectionLabelReference(labelElement));
			}
		});

		if (parentLayoutElement instanceof ElkNode) {
			parentLayoutElement.setProperty(CoreOptions.NODE_LABELS_PLACEMENT, getNodeLabelPlacement(style));
		}
	}

	private static EnumSet<NodeLabelPlacement> getNodeLabelPlacement(final Style s) {
		// Determine horizontal node label placement
		NodeLabelPlacement horizontalNodeLabelPlacement = NodeLabelPlacement.H_CENTER;
		if (s.getHorizontalLabelPosition() != null) {
			switch (s.getHorizontalLabelPosition()) {
			case BEFORE_GRAPHIC:
			case GRAPHIC_BEGINNING:
				horizontalNodeLabelPlacement = NodeLabelPlacement.H_LEFT;
				break;
			case GRAPHIC_CENTER:
				horizontalNodeLabelPlacement = NodeLabelPlacement.H_CENTER;
				break;
			case GRAPHIC_END:
			case AFTER_GRAPHIC:
				horizontalNodeLabelPlacement = NodeLabelPlacement.H_RIGHT;
				break;
			}
		}

		// Determine vertical node label placement
		NodeLabelPlacement verticalNodeLabelPlacement = NodeLabelPlacement.V_CENTER;
		if (s.getVerticalLabelPosition() != null) {
			switch (s.getVerticalLabelPosition()) {
			case BEFORE_GRAPHIC:
			case GRAPHIC_BEGINNING:
				verticalNodeLabelPlacement = NodeLabelPlacement.V_TOP;
				break;
			case GRAPHIC_CENTER:
				verticalNodeLabelPlacement = NodeLabelPlacement.V_CENTER;
				break;
			case GRAPHIC_END:
			case AFTER_GRAPHIC:
				verticalNodeLabelPlacement = NodeLabelPlacement.V_BOTTOM;
				break;
			}
		}

		// Build the node label placement set
		// Assume the placement of the nodes is inside because outside labels are only supported for docked shapes.
		// However, the ELK graph we build does not contain labels for ports, such labels are considered part of the port itself.
		return EnumSet.of(horizontalNodeLabelPlacement, verticalNodeLabelPlacement, NodeLabelPlacement.INSIDE);
	}

	private ElkLabel createElkLabel(final ElkGraphElement parentLayoutElement, final String txt,
			final Dimension labelSize) {
		final ElkLabel newLabel = ElkGraphUtil.createLabel(parentLayoutElement);
		newLabel.setText(txt);

		if (labelSize != null) {
			newLabel.setWidth(labelSize.width);
			newLabel.setHeight(labelSize.height);
		}

		return newLabel;
	}

	/**
	 * Creates ELK edges for connection diagram nodes which are descendants of the specified node.
	 * Even though the results of the ELK edge routing are not used, it is still important because it affects the placements of shapes.
	 */
	private void createElkGraphElementsForConnections(final DiagramNode dn, final LayoutMapping mapping) {
		for (final DiagramElement de : dn.getDiagramElements()) {
			if (de.getGraphic() instanceof AgeConnection) {
				final Object edgeStart = mapping.getGraphMap().inverse().get(de.getStartElement());
				final Object edgeEnd = mapping.getGraphMap().inverse().get(de.getEndElement());
				if (edgeStart instanceof ElkConnectableShape && edgeEnd instanceof ElkConnectableShape) {
					final ElkConnectableShape start = (ElkConnectableShape) edgeStart;
					final ElkConnectableShape end = (ElkConnectableShape) edgeEnd;

					final ElkEdge newEdge = ElkGraphUtil.createSimpleEdge(start, end);
					newEdge.setProperty(CoreOptions.INSIDE_SELF_LOOPS_YO, true);
					mapping.getGraphMap().put(newEdge, de);

					createElkLabels(de, newEdge, mapping);

				}
			}

			createElkGraphElementsForConnections(de, mapping);
		}
	}
}
