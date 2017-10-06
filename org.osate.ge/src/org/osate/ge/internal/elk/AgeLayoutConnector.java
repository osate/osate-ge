package org.osate.ge.internal.elk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.HierarchyHandling;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortLabelPlacement;
import org.eclipse.elk.core.options.SizeConstraint;
import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.graph.ElkConnectableShape;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.osate.ge.graphics.Point;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.StyleBuilder;
import org.osate.ge.graphics.internal.AgeConnection;
import org.osate.ge.graphics.internal.AgeShape;
import org.osate.ge.graphics.internal.Label;
import org.osate.ge.internal.AgeDiagramProvider;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

// TODO: Set size and position of nodes so that the information will be available to the layout algorithm if needed.
// TODO: Labels
// TODO: Insets
// TODO: User configuration using the layout view
public class AgeLayoutConnector implements IDiagramLayoutConnector {
	// TODO: Rename
	@Override
	public LayoutMapping buildLayoutGraph(final IWorkbenchPart workbenchPart, final Object diagramPart) {
		System.err.println("CREATING GRAPH LAYOUT");

		// TODO: Move setting properties to the util handler?
		// Determine the root node to layout
		final DiagramNode rootDiagramNode;
		if (diagramPart == null) {
			final AgeDiagram diagram = getDiagram(workbenchPart);
			rootDiagramNode = diagram;
		} else if (diagramPart instanceof DiagramNode) {
			rootDiagramNode = (DiagramNode) diagramPart;
		} else {
			throw new RuntimeException("Unsupported case. Diagram part: " + diagramPart);
		}

		// Create the graph
		final LayoutMapping mapping = new LayoutMapping(workbenchPart);
		final ElkNode rootNode = ElkGraphUtil.createGraph();
		rootNode.setProperty(AgeProperties.LAYOUT_MAPPING, mapping);
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

//	if (LayoutDiagramHandler.firstPass) {
		// TODO: Take away all docked diagram elmeents which are docked to parents (such as feature group children)
//		final TestPropertyValue testValue = new TestPropertyValue();
//		testValue.layoutMapping = mapping;
//		testProcessPorts(rootNode, testValue);
//		testProcessEdges(rootNode, testValue);
//		rootNode.setProperty(LayoutDiagramHandler.TEST_PROPERTY, testValue);
		// }

		// TODO: Another hack.. Need null check).
		// rootNode.getChildren().get(0).setProperty(LayoutDiagramHandler.TEST_PROPERTY, testValue);
		return mapping;
	}

	private static void testProcessPorts(final ElkNode parentNode, final TestPropertyValue testValue) {
		for (final ElkNode child : parentNode.getChildren()) {
			testProcessPorts(child, testValue);
		}

		// TODO: Cleanup
		final ArrayList<ElkPort> portsToRemove = new ArrayList<>();
		for (final ElkPort port : parentNode.getPorts()) {
			final Object potentialDe = testValue.layoutMapping.getGraphMap().get(port);
			if (potentialDe instanceof DiagramElement) {
				final DiagramElement de = (DiagramElement) potentialDe;
				if (de.getDockArea() == DockArea.GROUP) {
					portsToRemove.add(port);
				}
			}
		}

		// Avoid concurrent modification
		for (final ElkPort port : portsToRemove) {
			testValue.portInfoMap.put(port, new TestInfo(port, parentNode));
			EcoreUtil.remove(port);
			// System.err.println("REMOVED PORT");
		}
	}

	// TODO: Assumes port info map has been populated
	private static void testProcessEdges(final ElkNode parentNode, final TestPropertyValue testValue) {
		for (final ElkNode child : parentNode.getChildren()) {
			testProcessEdges(child, testValue);
		}

		// TODO: What is a contained edge?
		final List<ElkEdge> edgesToRemove = new ArrayList<>();
		for (final ElkEdge edge : parentNode.getContainedEdges()) {
			if (Stream.concat(edge.getSources().stream(), edge.getTargets().stream())
					.anyMatch(cs -> testValue.portInfoMap.containsKey(cs))) {
				edgesToRemove.add(edge);
			}
		}

		for (final ElkEdge edge : edgesToRemove) {
			testValue.edgeInfoMap.put(edge, new TestInfo(edge, edge.getContainingNode()));
			EcoreUtil.remove(edge);
			// System.err.println("REMOVED EDGE");
		}
	}

	private static void createElkGraphElementsForNonLabelChildShapes(final DiagramNode parentNode, final ElkNode parent,
			final LayoutMapping mapping) {
		createElkGraphElementsForElements(parentNode.getDiagramElements(), parent, mapping);
	}

	private static void createElkGraphElementsForElements(final Collection<DiagramElement> elements,
			final ElkNode parent,
			final LayoutMapping mapping) {
		// TODO: Share predicate
		elements.stream()
		.filter(de -> de.getGraphic() instanceof AgeShape && !(de.getGraphic() instanceof Label))
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

//			if (!LayoutDiagramHandler.firstPass) {
//				// newNode.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
//			}

			// newNode.setProperty(LayeredOptions.NODE_SIZE_CONSTRAINTS, SizeConstraint.free()); // TODO: Should be configurable. Allows layout
			final EnumSet<SizeConstraint> nodeSizeConstraints = EnumSet.of(SizeConstraint.PORTS,
					SizeConstraint.MINIMUM_SIZE,
					SizeConstraint.NODE_LABELS);
			newNode.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, nodeSizeConstraints); // TODO: Should include port labels?

			// TODO: MInimum size may not be an Issue
			// TODO: SHouldn't have to set minimum size if ports are being taken into account and labels are the correct size
			newNode.setProperty(CoreOptions.NODE_SIZE_MINIMUM, new KVector(200, 100));

			// System.err.println("SET MINIMUM SIZE: " + newNode);

			newNode.setProperty(CoreOptions.INSIDE_SELF_LOOPS_ACTIVATE, true);

			// Create Children
			createElkGraphElementsForNonLabelChildShapes(de, newNode, mapping);

			return Optional.ofNullable(newNode);
		} else {
			// Docked
			// TODO: Share predicate
			final boolean hasNonLabelChildren = de.getDiagramElements().stream()
					.anyMatch(c -> !(c.getGraphic() instanceof Label));

			if (hasNonLabelChildren) {
				// TODO: Special handling. Will need create several ports which have a fixed position.
				// TODO: Will need to manually specify position

				// TODO: Share between other branch
				final ElkPort newPort = ElkGraphUtil.createPort(layoutParent);
				mapping.getGraphMap().put(newPort, de);
				setShapePositionAndSize(newPort, de);
// PortLabelPlacement.INSIDE

//				if (!LayoutDiagramHandler.firstPass) {
//					createElkGraphElementsForNonLabelChildShapes(de, layoutParent, mapping);
//				}

// TODO: labels for children
			} else {
				// TODO: Need to set a minimize size....

				// Create Port
				final ElkPort newPort = ElkGraphUtil.createPort(layoutParent);
				mapping.getGraphMap().put(newPort, de);
				setShapePositionAndSize(newPort, de);
				// TODO: Need to specify port position offset?

				// LayeredOptions.PORT_CONSTRAINTS
				// TODO: Will need to fix position of feature group children.. And feature groups too if they contain childrne?
				// TODO: FIXED_POS and FIXED_RATIO?

				return Optional.ofNullable(newPort);
			}

		}

		return Optional.empty();
	}

	private static void setShapePositionAndSize(final ElkShape shape, final DiagramElement de) {
		if (de.hasPosition()) {
			shape.setLocation(de.getX(), de.getY());
		}

		if (de.hasSize()) {
			if (shape instanceof ElkPort) {
				shape.setDimensions(de.getWidth(), de.getHeight());
			} else {
				shape.setDimensions(de.getWidth() + 500, de.getHeight() + 500); // TODO: Padding seems to help when using fixed ports
			}
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

		// TODO: Should be part of creating node?
		// System.err.println("PARENT: " + parentElement.getName());

		// TODO: Have helper?
		final Style style = StyleBuilder
				.create(parentElement.getStyle(), parentElement.getGraphicalConfiguration().style, Style.DEFAULT)
				.build();
		// TODO: Need to get final style
		parentLayoutElement.setProperty(CoreOptions.NODE_LABELS_PLACEMENT,
				getNodeLabelPlacement(style));
		// newLabel.setProperty(CoreOptions.PORT_LABELS_PLACEMENT, PortLabelPlacement.INSIDE);
		// parentLayoutElement.setProperty(CoreOptions.NODE_LABELS_PLACEMENT, NodeLabelPlacement.insideTopCenter());
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
			// System.err.println("SIZE: " + p.x + " : " + p.y);
			newLabel.setWidth(p.x);
			newLabel.setHeight(p.y);
		} finally {
			if (gc != null) {
				gc.dispose();
			}

			if(f != null) {
				f.dispose();
			}
		}

		newLabel.setText(txt); // TODO

		return newLabel;
	}

	private void createElkGraphElementsForConnections(final DiagramNode dn, final LayoutMapping mapping) {
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

//					final ElkEdge newEdge = ElkGraphUtil.createEdge(null);
//					newEdge.getSources().add(start);
//					newEdge.getTargets().add(end);
//
//					final ElkEdgeSection s = ElkGraphUtil.createEdgeSection(newEdge);
//					// TODO: Backwards?
//					s.setOutgoingShape(start);
//					s.setIncomingShape(end);
//					ElkGraphUtil.updateContainment(newEdge);

					// TODO: Remove this. This is ignores node to node connections
					// if (start instanceof ElkPort && end instanceof ElkPort) {
					final ElkEdge newEdge = ElkGraphUtil.createSimpleEdge(start, end);// ElkGraphUtil.createEdge(elkParentNode); // TODO: Coordinate system.
					// Read documentation
					newEdge.setProperty(CoreOptions.INSIDE_SELF_LOOPS_YO, true); // TODO: SHould be set on the edge?

					// System.err
					// .println("SECTIONS: " + newEdge.getSections().size() + " : " + edgeStart + " : " + edgeEnd);
					// TODO: Disable bendpoints for curved edges.
					mapping.getGraphMap().put(newEdge, de);

					createElkLabels(de, newEdge, mapping);
					// }

				}

				// TODO: Connection to Connections ...
			}

			createElkGraphElementsForConnections(de, mapping);
		}
	}

	private static AgeDiagram getDiagram(final IWorkbenchPart workbenchPart) {
		final AgeDiagramEditor editor = ((AgeDiagramEditor) workbenchPart);
		final AgeDiagramProvider diagramProvider = (AgeDiagramProvider) Objects
				.requireNonNull(editor.getAdapter(AgeDiagramProvider.class), "Unable to get Age Diagram Provider");
		return Objects.requireNonNull(diagramProvider.getAgeDiagram(), "Unable to retrieve diagram");
	}

	@Override
	public void applyLayout(final LayoutMapping mapping, final IPropertyHolder settings) {
		final AgeDiagram diagram = getDiagram(mapping.getWorkbenchPart());
		diagram.modify("Layout", m -> {
			// Modify shapes
			for (Entry<ElkGraphElement, Object> entry1 : mapping.getGraphMap().entrySet()) {
				final ElkGraphElement elkElement1 = entry1.getKey();
				final DiagramNode dn1 = (DiagramNode) entry1.getValue();

				// "Root" elements are elements that don't have a grandparent since all graphs have a root element which contain everything.
				final boolean isRoot = elkElement1 instanceof ElkNode
						? ((ElkNode) elkElement1).getParent().getParent() == null
						: false;

				if (elkElement1 instanceof ElkEdge) {
					// System.err.println("APPLY TO EDGE: " + dn1.getBusinessObject());
				}
				// TODO: Is there a modified flag for elk element. One appeared to be mentioned

				if (dn1 instanceof DiagramElement) {
					final DiagramElement de1 = (DiagramElement) dn1;
					if (de1.getGraphic() instanceof AgeShape) {
						// TODO: Handle labels. At least handle connection labels. Other labels aren't positionable.
						if (!(de1.getGraphic() instanceof Label)) {
							// TODO: Check if things are sizable
							if (elkElement1 instanceof ElkShape) {
								final ElkShape elkShape1 = (ElkShape) elkElement1;
								// Set Position. Don't set the position of root elements
								if (!isRoot) {
									// TODO: Need to handle nested shapes and need to set parent first?
									if (de1.getDockArea() == DockArea.GROUP) {
										// TODO: Fix. This assumes parent is a non-docked element
										// TODO: Fix cast
										final ElkPort parentPort = (ElkPort) mapping.getGraphMap().inverse()
												.get(de1.getParent());

										// System.err.println(elkShape1.getY() + " : " + parentPort.getY());

										m.setPosition(de1, new Point(elkShape1.getX() - parentPort.getX(),
												elkShape1.getY() - parentPort.getY()));
									} else {
										m.setPosition(de1, new Point(elkShape1.getX(), elkShape1.getY()));
									}
								}

								m.setSize(de1, new Dimension(elkShape1.getWidth(), elkShape1.getHeight()));
							}
						}
					}
				}
			}

			int cCount = 0; // TODO: Remove
			int cwsCount = 0; // TODO: Remove

			// Modify Connections
			for (Entry<ElkGraphElement, Object> entry2 : mapping.getGraphMap().entrySet()) {
				final ElkGraphElement elkElement2 = entry2.getKey();
				final DiagramNode dn2 = (DiagramNode) entry2.getValue();

				// TODO: Is there a modified flag for elk element. One appeared to be mentioned

				if (dn2 instanceof DiagramElement) {
					final DiagramElement de2 = (DiagramElement) dn2;
					if (de2.getGraphic() instanceof AgeConnection) {
						// TODO: Hierarchical edges aren't being handled. Because of the layout algorithm?

						// System.err.println("C");
						if (elkElement2 instanceof ElkEdge) {
							final ElkEdge edge = (ElkEdge) elkElement2;
							cCount++;

							if (edge.getSections().size() > 0) {
								// TODO: Check that it is 1. Edges that represent multiple connections?
								final ElkEdgeSection es = edge.getSections().get(0);
								// TODO: Understand edge section fields.
								// TODO: Coordinate transformations.

								final Point parentPosition = getAbsolutePosition(de2.getContainer());
								final Point elkContainerPosition = getAbsolutePosition(
										(DiagramNode) mapping.getGraphMap().get(edge.getContainingNode()));
								final double offsetX = elkContainerPosition.x;// - parentPosition.x; // TODO: Double
								final double offsetY = elkContainerPosition.y;// - parentPosition.y; // TODO: Double
								/*
								 * System.err.println("BENDPOINTS " + es.getBendPoints().size());
								 * System.err.println(es.getStartX() + " : " + es.getStartY() + " : " + offsetX + ", " + offsetY + " : " +
								 * elkContainerPosition.y + " : " + parentPosition.y);
								 * for(final ElkBendPoint bp : es.getBendPoints()) {
								 * System.err.println("BP: " + bp.getX() + " : " + bp.getY());
								 * }
								 */

//								System.err.println(es.getStartX() + " : " + es.getStartY() + " : " + es.getEndX()
//								+ " : " + es.getEndY());

								final List<Point> newBendpoints;
								newBendpoints = new ArrayList<>(2 + es.getBendPoints().size());
								// TODO: Could have no bendpoints but have a start and end point..
								// TODO: Starting and ending points. Usage allows more accurately using ELK layout but causes problems with connection
								// endings. Fix!

// TODO: Need to have a offset for the start and end points... Need to work if there are no bendpoints.

								newBendpoints.add(new Point(es.getStartX() + offsetX, es.getStartY() + offsetY));

								es.getBendPoints().stream()
								.map(bp -> new Point(bp.getX() + offsetX, bp.getY() + offsetY))
								.forEachOrdered(newBendpoints::add);

								newBendpoints.add(new Point(es.getEndX() + offsetX, es.getEndY() + offsetY));
								newBendpoints.set(0, getAdjacentPoint(newBendpoints.get(0), newBendpoints.get(1), 4));
								newBendpoints.set(newBendpoints.size() - 1,
										getAdjacentPoint(newBendpoints.get(newBendpoints.size() - 1),
												newBendpoints.get(newBendpoints.size() - 2), 4));
								m.setBendpoints(de2, newBendpoints);

								cwsCount++;
							}
						}
					}
				}
			}

			// System.err.println("CCOUNT: " + cCount + " : " + cwsCount);
		});
	}

	/**
	 * Returns a point next to p1 which is on the line segment between p2 and p1.
	 * @param p1
	 * @param p2
	 * @return
	 */
	private static Point getAdjacentPoint(final Point p1, final Point p2, final double d) {
		final double dx = p2.x - p1.x;
		final double dy = p2.y - p1.y;
		final double l = Math.sqrt(dx * dx + dy * dy);
		final double ux = dx / l;
		final double uy = dy / l;
		// TODO: Adjust d to be the minimum the spacing between the points?

		return new Point(p1.x + d * ux, p1.y + d * uy);
	}

	private static Point getAbsolutePosition(final DiagramNode dn) {
		int x = 0;
		int y = 0;
		for (DiagramNode tmp = dn; tmp instanceof DiagramElement; tmp = tmp.getParent()) {
			final DiagramElement tmpDe = (DiagramElement) tmp;
			if (tmpDe.getGraphic() instanceof AgeShape) { // Ignore connections in case the X and Y values are not 0.
				x += tmpDe.getX();
				y += tmpDe.getY();
			}
		}

		return new Point(x, y);
	}
}
