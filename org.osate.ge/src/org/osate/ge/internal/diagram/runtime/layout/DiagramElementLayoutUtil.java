package org.osate.ge.internal.diagram.runtime.layout;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.elk.core.IGraphLayoutEngine;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.HierarchyHandling;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortLabelPlacement;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.options.SizeConstraint;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IGraphElementVisitor;
import org.eclipse.elk.graph.ElkConnectableShape;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.osate.ge.graphics.Point;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.StyleBuilder;
import org.osate.ge.graphics.internal.AgeConnection;
import org.osate.ge.graphics.internal.AgeShape;
import org.osate.ge.graphics.internal.Label;
import org.osate.ge.internal.AgeDiagramProvider;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramElementPredicates;
import org.osate.ge.internal.diagram.runtime.DiagramModification;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

public class DiagramElementLayoutUtil {
	private static final String layoutAlgorithm = "org.eclipse.elk.layered";

	public static void layout(final String label, final IEditorPart editor,
			final Collection<? extends DiagramNode> diagramNodes,
			final LayoutOptions options) {
		if (!(editor instanceof AgeDiagramEditor)) {
			throw new RuntimeException("Editor must be an " + AgeDiagramEditor.class.getName());
		}

		layout(label, ((AgeDiagramEditor) editor).getAgeDiagram(), diagramNodes, options);
	}

	public static void layout(final String label, final AgeDiagram diagram,
			final LayoutOptions options) {
		layout(label, diagram, null, options);
	}

	public static void layout(final String label, final AgeDiagram diagram,
			final Collection<? extends DiagramNode> diagramNodes,
			final LayoutOptions options) {
		// Determine the diagram nodes to layout
		final List<DiagramNode> nodesToLayout;
		if (diagramNodes == null) {
			nodesToLayout = Collections.singletonList(diagram);
		} else {
			// Only layout shapes. Also filter out any descendants of specified diagram elements
			nodesToLayout = filterUnusedNodes(diagramNodes);
		}

		if (nodesToLayout.isEmpty()) {
			return;
		}

		diagram.modify(label, m -> layout(m, nodesToLayout, options));
	}

	public static void layout(final DiagramModification m,
			final Collection<? extends DiagramNode> nodesToLayout, final LayoutOptions options) {
		Objects.requireNonNull(nodesToLayout, "nodesToLayout must not be null");

		// Layout the nodes
		final IGraphLayoutEngine layoutEngine = new RecursiveGraphLayoutEngine(); // TODO: Move
		for (final DiagramNode dn : nodesToLayout) {
			final LayoutMapping mapping = buildLayoutGraph(dn);

			mapping.getLayoutGraph().setProperty(CoreOptions.ALGORITHM, layoutAlgorithm);

			// Apply properties for the initial layout
			applyInitialProperties(mapping, options);

			// TODO: Only during development
			saveGraphToDebugProject(mapping.getLayoutGraph());

			// Perform the layout
			layoutEngine.layout(mapping.getLayoutGraph(), new BasicProgressMonitor());

			// Note the current layout algorithm doesn't support nested docked diagram elements. The support should eventually be added by manually adding
			// nested docked diagram elements and related connections after the first layout and then routing connections in a second layout operation.
			// As of 2017-10-12 ELK does not support such a capability.

			applyLayout(mapping, m);

			// TODO: Only during development
			showGraphInLayoutGraphView(mapping.getLayoutGraph());
		}
	}

	private static void saveGraphToDebugProject(final ElkNode g) {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("debug");
		if(project != null && project.exists()) {
			final URI uri = URI.createPlatformResourceURI(project.getFile("layout_graph.elkg").getFullPath().toString(),
					true);

			// Save the resource
			final ResourceSet rs = new ResourceSetImpl();
			final Resource resource = rs.createResource(uri);
			resource.getContents().add(g);
			try {
				resource.save(Collections.emptyMap());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static void showGraphInLayoutGraphView(final ElkNode n) {
		Display.getCurrent().syncExec(() -> {
			try {
				final IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView("org.eclipse.elk.debug.graphView");
				if (viewPart != null) {
					final Method updateWithGraphMethod = viewPart.getClass().getMethod("updateWithGraph",
							ElkNode.class);
					updateWithGraphMethod.invoke(null, n);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});

	}

// TODO: rename
	private static void applyInitialProperties(final LayoutMapping layoutMapping, final LayoutOptions options) {
		// TODO: Cleanup
		final IGraphElementVisitor visitor = element -> {
			// Fix the position of the top level ports if the lock top level ports option is set.
			if(element instanceof ElkNode) {
				final ElkNode n = (ElkNode) element;

				PortConstraints portConstraints = PortConstraints.FIXED_SIDE;
				if (n.getPorts().size() == 0) {
					// Don't constrain ports if there aren't any. As of 2017-10-11, FIXED_POS can affect the layout even if the node does not contain ports.
					// TODO: What about FIXED_SIDE?
					portConstraints = PortConstraints.FREE;
				}
				// TODO: Don't fix side? Algorithm seems to do it automatically and this allows for exceptions in the case of provides and requires.
				// PortConstraints portConstraints = PortConstraints.FREE;

				n.setProperty(CoreOptions.PORT_CONSTRAINTS, portConstraints);

				// TODO: Set the minimum size
				// The node size constraints for labels doesn't seem to work properly
				// TODO: Create test case and report
				double minWidth = 200;
				double minHeight = 100;

				// TODO: Only do this based on node placements
				for (final ElkLabel l : n.getLabels()) {
					// TODO: TBD padding. Sometimes labels aren't shifted all the way to the edge by ELK so padding is needed. Property to adjust.
					// Is this due to being positioned away from ports? Any ways to tell it to clear area for labels?
					minWidth = Math.max(minWidth, l.getWidth() + 50);
					minHeight = Math.max(minHeight, l.getHeight());
				}

				// TODO: Remove the other setting of the minimum node size
				n.setProperty(CoreOptions.NODE_SIZE_MINIMUM, new KVector(minWidth, minHeight));

			} else if (element instanceof ElkPort) {
				final ElkPort p = (ElkPort) element;
				final DiagramElement de = (DiagramElement) layoutMapping.getGraphMap().get(p);

				// These properties are set here instead of in the layout connector because they need to be set based on the specific layout being
				// performed.

				// TODO: When handling feature groups, will need to populate these properties for added ports.
				// Determine the port side
				final PortSide portSide;

				// Otherwise change the port side based on the diagram element's default docking configuration
				portSide = getPortSideForNonGroupDockArea(
						de.getGraphicalConfiguration().defaultDockingPosition.getDockArea());

				p.setProperty(CoreOptions.PORT_SIDE, portSide);

				// Set the port border offset based on the port side
				if (PortSide.SIDES_NORTH_SOUTH.contains(portSide)) {
					p.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getHeight());
				} else {
					p.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getWidth());
				}
			}
		};

		ElkUtil.applyVisitors(layoutMapping.getLayoutGraph(),visitor);

	}

	private static boolean isTopLevel(final ElkGraphElement ge) {
		if(ge instanceof ElkPort) {
			final ElkPort p = (ElkPort) ge;
			return p.getParent() == null || p.getParent().getParent() == null;
		} else if(ge instanceof ElkNode) {
			final ElkNode n = (ElkNode) ge;
			return n.getParent() == null || n.getParent().getParent() == null;
		} else {
			return false;
		}
	}

	// TODO: Rename? Takes any collection. Creates list.
	/**
	 * Returns a list which contains the specified diagram nodes with unused nodes removed. It removes nodes which are not shapes or that have an ancestor in the specified list.
	 * @param diagramNodes
	 * @return
	 */
	static List<DiagramNode> filterUnusedNodes(final
			Collection<? extends DiagramNode> diagramNodes)
	{
		return diagramNodes.stream().filter(dn -> dn instanceof AgeDiagram || (dn instanceof DiagramElement
				&& DiagramElementPredicates.isShape((DiagramElement) dn) && !containsAnyAncestor(diagramNodes, dn)))
				.collect(Collectors.toList());
	}

	/**
	 * Returns true if the specified collection contains any ancestor for the specified diagram node
	 * @param c
	 * @param e
	 * @return
	 */
	private static boolean containsAnyAncestor(final Collection<? extends DiagramNode> c, final DiagramNode n) {
		for (DiagramNode ancestor = n.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
			if (c.contains(ancestor)) {
				return true;
			}
		}

		return false;
	}

	private static PortSide getPortSideForNonGroupDockArea(final DockArea dockArea) {
		switch (dockArea) {
		case TOP:
			return PortSide.NORTH;

		case BOTTOM:
			return PortSide.SOUTH;

		case LEFT:
			return PortSide.WEST;

		case RIGHT:
			return PortSide.EAST;

		default:
			throw new RuntimeException("Unexpected dock area: " + dockArea);
		}
	}

// TODO: Rename
// TODO: Cleanup Rename.. Differnet return value?
	private static LayoutMapping buildLayoutGraph(final DiagramNode rootDiagramNode) {
		System.err.println("CREATING GRAPH LAYOUT");

		// Create the graph
		final LayoutMapping mapping = new LayoutMapping(null);
		final ElkNode rootNode = ElkGraphUtil.createGraph();
		rootNode.setProperty(CoreOptions.DIRECTION, Direction.RIGHT);
		rootNode.setProperty(CoreOptions.HIERARCHY_HANDLING, HierarchyHandling.INCLUDE_CHILDREN);
		// rootNode.setProperty(CoreOptions.HIERARCHY_HANDLING, HierarchyHandling.SEPARATE_CHILDREN);

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

//						final ElkEdge newEdge = ElkGraphUtil.createEdge(null);
//						newEdge.getSources().add(start);
//						newEdge.getTargets().add(end);
					//
//						final ElkEdgeSection s = ElkGraphUtil.createEdgeSection(newEdge);
//						// TODO: Backwards?
//						s.setOutgoingShape(start);
//						s.setIncomingShape(end);
//						ElkGraphUtil.updateContainment(newEdge);

					// TODO: Remove this. This is ignores node to node connections
					// if (start instanceof ElkPort && end instanceof ElkPort) {
					final ElkEdge newEdge = ElkGraphUtil.createSimpleEdge(start, end);// ElkGraphUtil.createEdge(elkParentNode); // TODO: Coordinate system.
					// Read documentation
					newEdge.setProperty(CoreOptions.INSIDE_SELF_LOOPS_YO, true); // TODO: SHould be set on the edge?

					// TODO: Disable bendpoints for curved edges.
					mapping.getGraphMap().put(newEdge, de);

					createElkLabels(de, newEdge, mapping);

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

// TODO: Cleanup
	private static void applyLayout(final LayoutMapping mapping, final DiagramModification m) {
		// Modify shapes
		for (Entry<ElkGraphElement, Object> entry1 : mapping.getGraphMap().entrySet()) {
			final ElkGraphElement elkElement1 = entry1.getKey();
			final DiagramNode dn1 = (DiagramNode) entry1.getValue();

			final boolean isTopLevelElement = isTopLevel(elkElement1);
			// TODO: Is there a modified flag for elk element. One appeared to be mentioned

			if (dn1 instanceof DiagramElement) {
				final DiagramElement de1 = (DiagramElement) dn1;
				if (de1.getGraphic() instanceof AgeShape) {
					// TODO: Handle labels. At least handle connection labels. Other labels aren't positionable.
					if (!(de1.getGraphic() instanceof Label)) {
						// TODO: Check if things are sizable
						if (elkElement1 instanceof ElkShape) {
							final ElkShape elkShape1 = (ElkShape) elkElement1;
							// Set Position. Don't set the position of top level elements
							if (!isTopLevelElement) {
								// TODO: Need to handle nested shapes and need to set parent first?
								if (de1.getDockArea() == DockArea.GROUP) {
									// TODO: Fix. This assumes parent is a non-docked element
									// TODO: Fix cast
									final ElkPort parentPort = (ElkPort) mapping.getGraphMap().inverse()
											.get(de1.getParent());

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

							final List<Point> newBendpoints;
							newBendpoints = new ArrayList<>(2 + es.getBendPoints().size());
							// TODO: Could have no bendpoints but have a start and end point..
							// TODO: Starting and ending points. Usage allows more accurately using ELK layout but causes problems with connection
							// endings. Fix!

							// TODO: Need to have a offset for the start and end points... Need to work if there are no bendpoints.

							newBendpoints.add(new Point(es.getStartX() + offsetX, es.getStartY() + offsetY));

							es.getBendPoints().stream().map(bp -> new Point(bp.getX() + offsetX, bp.getY() + offsetY))
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
