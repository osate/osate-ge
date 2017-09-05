package org.osate.ge.internal.elk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortSide;
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
import org.eclipse.ui.IWorkbenchPart;
import org.osate.ge.graphics.Point;
import org.osate.ge.internal.AgeDiagramProvider;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.graphics.AgeConnection;
import org.osate.ge.internal.graphics.AgeShape;
import org.osate.ge.internal.graphics.Label;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.osate.ge.internal.ui.handlers.LayoutDiagramHandler;

// TODO: Labels
// TODO: Insets
// TODO: User configuration using the layout view
public class AgeLayoutConnector implements IDiagramLayoutConnector {
	// TODO: Rename
	@Override
	public LayoutMapping buildLayoutGraph(final IWorkbenchPart workbenchPart, final Object diagramPart) {
		System.err.println("CREATING GRAPH LAYOUT");

		// TODO: Support part of the diagram.
		if(diagramPart != null) {
			// TODO
			throw new RuntimeException("Unhandled case. Only laying out the entire editor is supported.");
		}

		final AgeDiagram diagram = getDiagram(workbenchPart);

		final LayoutMapping mapping = new LayoutMapping(workbenchPart);

		final ElkNode rootNode = ElkGraphUtil.createGraph();

		// rootNode.setProperty(CoreOptions.SPACING_COMPONENT_COMPONENT, 100.0);

		// rootNode.setProperty(org.eclipse.elk.alg.layered.properties.LayeredOptions.SPACING_NODE_NODE_BETWEEN_LAYERS,
		// 400.0);
		// rootNode.setProperty(org.eclipse.elk.alg.layered.properties.LayeredOptions.SPACING_NODE_NODE, 400.0);

		rootNode.setProperty(CoreOptions.DIRECTION, Direction.RIGHT);

		// rootNode.setProperty(CoreOptions.SPACING_NODE_NODE, 300.0);
		// rootNode.setProperty(LayeredOptions.DIRECTION, Direction.RIGHT); // TODO: Does this have any affect?

		// Prevents exception in some cases but breaks connection layout. Something to do with empty elements? Similiar exception experienced with empty nodes..
		// TODO: Experiment with dummy nodes?
		//rootNode.setProperty(LayeredOptions.CROSSING_MINIMIZATION_GREEDY_SWITCH_TYPE, GreedySwitchType.OFF);
		// rootNode.setProperty(CoreOptions.HIERARCHY_HANDLING, HierarchyHandling.INCLUDE_CHILDREN); // TODO: Needed but enabling causes exceptions in some
		// cases.
		mapping.getGraphMap().put(rootNode, diagram);

		//	How to set. Algorithm... on the config store? (CoreOptions.ALGORITHM, "org.eclipse.elk.layered"); // TODO: Is this the proper way to do it?

		createElkGraphElementsForNonLabelChildShapes(diagram, rootNode, mapping);

		// TODO: Creature features? Create feature groups as unmoveable ports.

		// System.err.println("CREATING CONNECTIONS...");
		createElkGraphElementsForConnections(diagram, mapping);

		// TODO: Connections. Could be a separate pass. Or could eagerly create things. eager may be better because connections could connect to connections?
		// Does ELK allow edges to connect? Doesn't look like it.

		mapping.setLayoutGraph(rootNode);

//	if (LayoutDiagramHandler.firstPass) {
		// TODO: Take away all docked diagram elmeents which are docked to parents (such as feature group children)
		final TestPropertyValue testValue = new TestPropertyValue();
		testValue.layoutMapping = mapping;
		testProcessPorts(rootNode, testValue);
		testProcessEdges(rootNode, testValue);
		rootNode.setProperty(LayoutDiagramHandler.TEST_PROPERTY, testValue);
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
				if(de.getDockArea() == DockArea.GROUP) {
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
		// TODO: Share predicate
		parentNode.getDiagramElements().stream()
		.filter(de -> de.getGraphic() instanceof AgeShape && !(de.getGraphic() instanceof Label))
		.forEachOrdered(de -> {
			createElkGraphElementForNonLabelShape(de, parent, mapping)
			.ifPresent(newLayoutElement -> createElkLabels(de, newLayoutElement, mapping));
		});
	}

	private static Optional<ElkGraphElement> createElkGraphElementForNonLabelShape(final DiagramElement de,
			final ElkNode layoutParent,
			final LayoutMapping mapping) {
		if (de.getDockArea() == null) {
			final ElkNode newNode = ElkGraphUtil.createNode(layoutParent);
			mapping.getGraphMap().put(newNode, de);
			setShapePositionAndSize(newNode, de);

			if (!LayoutDiagramHandler.firstPass) {
				// newNode.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
			}

			// newNode.setProperty(LayeredOptions.NODE_SIZE_CONSTRAINTS, SizeConstraint.free()); // TODO: Should be configurable. Allows layout
			newNode.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, SizeConstraint.minimumSizeWithPorts()); // TODO: Should include port labels?

			// TODO: MInimum size may not be an Issue
			// TODO: SHouldn't have to set minimum size if ports are being taken into account and labels are the correct size
			newNode.setProperty(CoreOptions.NODE_SIZE_MINIMUM, new KVector(200, 100));

			System.err.println("SET MINIMUM SIZE: " + newNode);

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
				newPort.setProperty(CoreOptions.PORT_SIDE, getPortSide(de));
				newPort.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getWidth());
				// newPort.setProperty(CoreOptions.PORT_LABELS_PLACEMENT, PortLabelPlacement.INSIDE);
// PortLabelPlacement.INSIDE

				if (!LayoutDiagramHandler.firstPass) {
					createElkGraphElementsForNonLabelChildShapes(de, layoutParent, mapping);
				}

// TODO: labels for children
			} else {
				// TODO: Need to set a minimize size....

				// Create Port
				final ElkPort newPort = ElkGraphUtil.createPort(layoutParent);
				mapping.getGraphMap().put(newPort, de);
				setShapePositionAndSize(newPort, de);

				// Position the ports inside of the the container
				newPort.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getWidth());
				// TODO: Need to specify port position offset?

				// LayeredOptions.PORT_CONSTRAINTS
				// TODO: Will need to fix position of feature group children.. And feature groups too if they contain childrne?
				// TODO: FIXED_POS and FIXED_RATIO?

				return Optional.ofNullable(newPort);
			}

		}

		return Optional.empty();
	}

	private static PortSide getPortSide(final DiagramNode dn) {
		if (!(dn instanceof DiagramElement)) {
			return null;
		}

		final DiagramElement de = ((DiagramElement) dn);
		final DockArea dockArea = de.getDockArea();
		if (dockArea == null) {
			return null;
		}

		switch (dockArea) {
		case TOP:
			return PortSide.NORTH;

		case BOTTOM:
			return PortSide.SOUTH;

		case LEFT:
			return PortSide.WEST;

		case RIGHT:
			return PortSide.EAST;

		case GROUP:
			return getPortSide(de.getParent());

		default:
			return null; // TODO: Proper behavior? Exception?
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
			createElkLabel(parentLayoutElement);
			// TODO: Need some sort of mapping. Will be needed for connection labels
		}

		// Create Secondary Labels
		parentElement.getDiagramElements().stream().filter(c -> c.getGraphic() instanceof Label)
		.forEachOrdered(labelElement -> {
			mapping.getGraphMap().put(createElkLabel(parentLayoutElement), labelElement);
		});
	}

	private static ElkLabel createElkLabel(final ElkGraphElement parentLayoutElement) {
		final ElkLabel newLabel = ElkGraphUtil.createLabel(parentLayoutElement);

		// TODO
		newLabel.setX(0);
		newLabel.setY(0);

		// TODO:
		newLabel.setWidth(100);
		newLabel.setHeight(20);

		// TODO:
		newLabel.setProperty(CoreOptions.NODE_LABELS_PLACEMENT, NodeLabelPlacement.insideTopCenter());

		// TOOD: Set minimum size of node based on label size

		return newLabel;
	}

	private void createElkGraphElementsForConnections(final DiagramNode dn, final LayoutMapping mapping) {
		for(final DiagramElement de : dn.getDiagramElements()) {
			// TODO: Understand the multiple sources and targets... Need to group connections from the same element together?
			// TODO: Understand edge vs edge section
			// TODO: Read Edge documentation. GraphUtil needed to assign to appropriate container?

			if(de.getGraphic() instanceof AgeConnection) {
				final Object edgeStart = mapping.getGraphMap().inverse().get(de.getStartElement());
				final Object edgeEnd = mapping.getGraphMap().inverse().get(de.getEndElement());
				if(edgeStart instanceof ElkConnectableShape &&
						edgeEnd instanceof ElkConnectableShape) {
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
		final AgeDiagramEditor editor = ((AgeDiagramEditor)workbenchPart);
		final AgeDiagramProvider diagramProvider = (AgeDiagramProvider)Objects.requireNonNull(editor.getAdapter(AgeDiagramProvider.class), "Unable to get Age Diagram Provider");
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

				if (elkElement1 instanceof ElkEdge) {
					System.err.println("APPLY TO EDGE: " + dn1.getBusinessObject());
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
								// TODO: Only set appropriate fields
								// TODO: Should runtime diagram use doubles?
								// System.err.println("POSITION: (" + de.getX() + ", " + de.getY() + ") -> (" + elkShape.getX() + ", " + elkShape.getY() + ")");

								if (de1.getDockArea() == null) {
									System.err.println("WIDTH: " + elkShape1.getWidth() + " : "
											+ elkShape1.getProperties().get(CoreOptions.NODE_SIZE_MINIMUM) + " : "
											+ elkShape1.getProperties().get(CoreOptions.NODE_SIZE_CONSTRAINTS) + " : "
											+ elkShape1);
								}

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
								m.setSize(de1, new Dimension(elkShape1.getWidth(), elkShape1.getHeight()));
							}
						} else {
							// TODO: For testing
							if (elkElement1 instanceof ElkShape) {
								final ElkShape elkShape2 = (ElkShape) elkElement1;
								// System.err.println(elkShape.getX() + " : " + elkShape.getWidth());
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
								final List<Point> newBendpoints;
								if (es.getBendPoints().size() == 0) {
									newBendpoints = null;
								} else {
									// TODO: Starting and ending points. Usage allows more accurately using ELK layout but causes problems with connection
									// endings
									// newBendpoints = new ArrayList<>(es.getBendPoints().size()+2);
									// newBendpoints.add(new Point((int)es.getStartX() + offsetX, (int)es.getStartY() + offsetY)); // TODO: doubles

									newBendpoints = es.getBendPoints().stream()
											.map(bp -> new Point(bp.getX() + offsetX, bp.getY() + offsetY)). // TODO: Use doubles for points and
											// dimensions
											collect(Collectors.toList());

									// newBendpoints.add(new Point((int)es.getEndX() + offsetX, (int)es.getEndY() + offsetY)); // TODO: doubles
								}

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

	private static Point getAbsolutePosition(final DiagramNode dn) {
		int x = 0;
		int y = 0;
		for(DiagramNode tmp = dn ; tmp instanceof DiagramElement; tmp = tmp.getParent()) {
			final DiagramElement tmpDe = (DiagramElement)tmp;
			if(tmpDe.getGraphic() instanceof AgeShape) { // Ignore connections in case the X and Y values are not 0.
				x += tmpDe.getX();
				y += tmpDe.getY();
			}
		}

		return new Point(x, y);
	}
}
