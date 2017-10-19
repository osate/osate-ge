package org.osate.ge.internal.diagram.runtime.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.HierarchyHandling;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortLabelPlacement;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.StyleBuilder;
import org.osate.ge.graphics.internal.AgeConnection;
import org.osate.ge.graphics.internal.AgeShape;
import org.osate.ge.graphics.internal.Label;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramNode;

class ElkGraphBuilder {
	// TODO: Rename
	// TODO: Cleanup Rename.. Differnet return value?
	static LayoutMapping buildLayoutGraph(final DiagramNode rootDiagramNode) {
		System.err.println("CREATING GRAPH LAYOUT");

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

	private static void createElkGraphElementsForNonLabelChildShapes(final DiagramNode parentNode, final ElkNode parent,
			final LayoutMapping mapping) {
		createElkGraphElementsForElements(parentNode.getDiagramElements(), parent, mapping);
	}

	private static void createElkGraphElementsForElements(final Collection<DiagramElement> elements,
			final ElkNode parent, final LayoutMapping mapping) {
		// TODO: Share predicate
		elements.stream().filter(de -> de.getGraphic() instanceof AgeShape && !(de.getGraphic() instanceof Label))
		.forEachOrdered(de -> {
			createElkGraphElementForNonLabelShape(de, parent, mapping)
			.ifPresent(newLayoutElement -> createElkLabels(de, newLayoutElement, mapping));
		});
	}

	private static Optional<ElkGraphElement> createElkGraphElementForNonLabelShape(final DiagramElement de,
			final ElkNode layoutParent, final LayoutMapping mapping) {
		if (de.getDockArea() == null) {
			final ElkNode newNode = ElkGraphUtil.createNode(layoutParent);
			mapping.getGraphMap().put(newNode, de);
			setShapePositionAndSize(newNode, de);

			newNode.setProperty(CoreOptions.PORT_LABELS_PLACEMENT, PortLabelPlacement.INSIDE);

			// newNode.setProperty(LayeredOptions.NODE_SIZE_CONSTRAINTS, SizeConstraint.free()); // TODO: Should be configurable. Allows layout
			final EnumSet<SizeConstraint> nodeSizeConstraints = EnumSet.of(SizeConstraint.PORTS,
					SizeConstraint.MINIMUM_SIZE, SizeConstraint.NODE_LABELS);
			newNode.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, nodeSizeConstraints); // TODO: Should include port labels?

			// TODO: MInimum size may not be an Issue
			// TODO: SHouldn't have to set minimum size if ports are being taken into account and labels are the correct size
			newNode.setProperty(CoreOptions.NODE_SIZE_MINIMUM, new KVector(200, 100));

			newNode.setProperty(CoreOptions.INSIDE_SELF_LOOPS_ACTIVATE, true);

			// Create Children
			createElkGraphElementsForNonLabelChildShapes(de, newNode, mapping);

			return Optional.ofNullable(newNode);
		} else { // Docked
			// Create Port
			final ElkPort newPort = ElkGraphUtil.createPort(layoutParent);
			mapping.getGraphMap().put(newPort, de);
			setShapePositionAndSize(newPort, de);

			// Don't create graph elements for children. ELK port cannot have child ports.

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

	private static void createElkLabels(final DiagramElement parentElement, final ElkGraphElement parentLayoutElement,
			final LayoutMapping mapping) {
		// TODO: Sizing
		// TODO: Connection labels are in incorrect position
		// TODO: Feature labels are in incorrect position. PortLabelPosition property...

		// Create Primary Label
		if (parentElement.getName() != null) {
			// TODO: Need completeness indicator.. Share with GraphitiAgeDiagram
			createElkLabel(parentLayoutElement, parentElement.getName());
			// TODO: Need some sort of mapping. Will be needed for connection labels
		}

		// Create Secondary Labels
		parentElement.getDiagramElements().stream().filter(c -> c.getGraphic() instanceof Label)
		.forEachOrdered(labelElement -> {
			mapping.getGraphMap().put(createElkLabel(parentLayoutElement, labelElement.getName()),
					labelElement);
		});

		;

		// TODO: Have helper?
		final Style style = StyleBuilder
				.create(parentElement.getStyle(), parentElement.getGraphicalConfiguration().style, Style.DEFAULT)
				.build();

		if (parentLayoutElement instanceof ElkNode) {
			// TODO: Need to get final style
			parentLayoutElement.setProperty(CoreOptions.NODE_LABELS_PLACEMENT, getNodeLabelPlacement(style));
		}
	}

	private static EnumSet<NodeLabelPlacement> getNodeLabelPlacement(final Style s) {
		final EnumSet<NodeLabelPlacement> nodeLabelPlacement = EnumSet.noneOf(NodeLabelPlacement.class);

		// TODO: Adjust API and AFTER_GRAPHIC and BEFORE_GRAPHIC to align with ELK's INSIDE and OUTSIDE
		// TOOD: Check for null
		// System.err.println(s.getHorizontalLabelPosition());
		// System.err.println(s.getVerticalLabelPosition());

		// TODO: Have some sort of default if one or both are null?

		if (s.getHorizontalLabelPosition() != null) {
			switch (s.getHorizontalLabelPosition()) {
			case BEFORE_GRAPHIC:
				nodeLabelPlacement.add(NodeLabelPlacement.H_LEFT);
				break;
				// Use center for all of these to avoid the layout algorithm from allocating a separate layer for the label
			case GRAPHIC_BEGINNING:
			case GRAPHIC_CENTER:
			case GRAPHIC_END:
				nodeLabelPlacement.add(NodeLabelPlacement.H_CENTER);
				nodeLabelPlacement.add(NodeLabelPlacement.H_CENTER);
				nodeLabelPlacement.add(NodeLabelPlacement.H_CENTER);
				break;
			case AFTER_GRAPHIC:
				nodeLabelPlacement.add(NodeLabelPlacement.H_RIGHT);
				break;
			default:
				break;
			}
		}

		// TOOD: Check for null
		if (s.getVerticalLabelPosition() != null) {
			switch (s.getVerticalLabelPosition()) {
			case BEFORE_GRAPHIC:
				nodeLabelPlacement.add(NodeLabelPlacement.V_TOP);
				break;
			case GRAPHIC_BEGINNING:
				nodeLabelPlacement.add(NodeLabelPlacement.V_TOP);
				break;
			case GRAPHIC_CENTER:
				nodeLabelPlacement.add(NodeLabelPlacement.V_CENTER);
				break;
			case GRAPHIC_END:
				nodeLabelPlacement.add(NodeLabelPlacement.V_BOTTOM);
				break;
			case AFTER_GRAPHIC:
				nodeLabelPlacement.add(NodeLabelPlacement.V_BOTTOM);
				break;
			default:
				break;
			}
		}

		// TODO: Support outside and priority
		nodeLabelPlacement.add(NodeLabelPlacement.INSIDE); // TODO: Not orientation specific

		return nodeLabelPlacement;
	}

	private static ElkLabel createElkLabel(final ElkGraphElement parentLayoutElement, final String txt) {
		final ElkLabel newLabel = ElkGraphUtil.createLabel(parentLayoutElement);

		// TODO
		newLabel.setX(0);
		newLabel.setY(0);

		newLabel.setWidth(100);
		newLabel.setHeight(20);

		// TODO:
		// TODO: Require running in UI Thread?

		// Display.getDefault().
		// TODO: Use appropriate font. Only initialize once for entire layout process.. Abstract out into handler.. Need to map style to font
		final Font f = new Font(Display.getDefault(), "Arial", 14, SWT.NONE);
		GC gc = null;
		try {
			gc = new GC(Display.getDefault());
			gc.setFont(f);
			final org.eclipse.swt.graphics.Point p = gc.stringExtent(txt);
			newLabel.setWidth(p.x);
			newLabel.setHeight(p.y);
		} finally {
			if (gc != null) {
				gc.dispose();
			}

			if (f != null) {
				f.dispose();
			}
		}

		newLabel.setText(txt); // TODO

		return newLabel;
	}

	private static void createElkGraphElementsForConnections(final DiagramNode dn, final LayoutMapping mapping) {
		for (final DiagramElement de : dn.getDiagramElements()) {
			// TODO: Understand the multiple sources and targets... Need to group connections from the same element together?
			// TODO: Understand edge vs edge section
			// TODO: Read Edge documentation. GraphUtil needed to assign to appropriate container?

			if (de.getGraphic() instanceof AgeConnection) {
				final Object edgeStart = mapping.getGraphMap().inverse().get(de.getStartElement());
				final Object edgeEnd = mapping.getGraphMap().inverse().get(de.getEndElement());
				if (edgeStart instanceof ElkConnectableShape && edgeEnd instanceof ElkConnectableShape) {
					final ElkConnectableShape start = (ElkConnectableShape) edgeStart;
					final ElkConnectableShape end = (ElkConnectableShape) edgeEnd;


//							final ElkEdge newEdge = ElkGraphUtil.createEdge(null);
//							newEdge.getSources().add(start);
//							newEdge.getTargets().add(end);
					//
//							final ElkEdgeSection s = ElkGraphUtil.createEdgeSection(newEdge);
//							// TODO: Backwards?
//							s.setOutgoingShape(start);
//							s.setIncomingShape(end);
//							ElkGraphUtil.updateContainment(newEdge);

					// TODO: Remove this. This is ignores node to node connections
					// if (start instanceof ElkPort && end instanceof ElkPort) {
					final ElkEdge newEdge = ElkGraphUtil.createSimpleEdge(start, end);// ElkGraphUtil.createEdge(elkParentNode); // TODO: Coordinate system.
					// Read documentation
					newEdge.setProperty(CoreOptions.INSIDE_SELF_LOOPS_YO, true); // TODO: SHould be set on the edge?

					// TODO: Set control points

					// TODO: Disable bendpoints for curved edges.
					mapping.getGraphMap().put(newEdge, de);

					createElkLabels(de, newEdge, mapping);

				}

				// TODO: Connection to Connections ...
			}

			createElkGraphElementsForConnections(de, mapping);
		}
	}
}
