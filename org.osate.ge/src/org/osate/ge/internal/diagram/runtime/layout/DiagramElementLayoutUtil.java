package org.osate.ge.internal.diagram.runtime.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.elk.core.IGraphLayoutEngine;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IGraphElementVisitor;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.ui.IEditorPart;
import org.osate.ge.DockingPosition;
import org.osate.ge.graphics.Point;
import org.osate.ge.graphics.internal.AgeConnection;
import org.osate.ge.graphics.internal.AgeShape;
import org.osate.ge.graphics.internal.Label;
import org.osate.ge.internal.Activator;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramElementPredicates;
import org.osate.ge.internal.diagram.runtime.DiagramModification;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.diagram.runtime.styling.StyleCalculator;
import org.osate.ge.internal.diagram.runtime.styling.StyleProvider;
import org.osate.ge.internal.preferences.Preferences;
import org.osate.ge.internal.query.Queryable;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

public class DiagramElementLayoutUtil {
	private static final String incrementalLayoutLabel = "Incremental Layout";
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

		diagram.modify(label, m -> layout(m, nodesToLayout,
				new StyleCalculator(diagram.getConfiguration(), StyleProvider.EMPTY), options));
	}

	private static void layout(final DiagramModification m,
			final Collection<? extends DiagramNode> nodesToLayout, final StyleProvider styleProvider,
			final LayoutOptions options) {
		Objects.requireNonNull(nodesToLayout, "nodesToLayout must not be null");

		// Layout the nodes
		final IGraphLayoutEngine layoutEngine = new RecursiveGraphLayoutEngine(); // TODO: Move
		for (final DiagramNode dn : nodesToLayout) {
			final LayoutMapping mapping = ElkGraphBuilder.buildLayoutGraph(dn, styleProvider);

			mapping.getLayoutGraph().setProperty(CoreOptions.ALGORITHM, layoutAlgorithm);

			// Apply properties for the initial layout
			applyInitialProperties(mapping, options);

			// TODO: Only during development
			LayoutDebugUtil.saveGraphToDebugProject(mapping.getLayoutGraph());

			// Perform the layout
			layoutEngine.layout(mapping.getLayoutGraph(), new BasicProgressMonitor());

			// Note the current layout algorithm doesn't support nested docked diagram elements. The support should eventually be added by manually adding
			// nested docked diagram elements and related connections after the first layout and then routing connections in a second layout operation.
			// As of 2017-10-12 ELK does not support such a capability.

			applyLayout(mapping, m);

			// TODO: Only during development
			LayoutDebugUtil.showGraphInLayoutGraphView(mapping.getLayoutGraph());
		}
	}

	/**
	 * Performs layout on elements in the specified diagram which have not been layed out.
	 * @param diagram
	 * @param mod
	 */
	public static void layoutIncrementally(final AgeDiagram diagram, final DiagramModification mod) {
		final IncrementalLayoutMode currentLayoutMode = IncrementalLayoutMode
				.getById(Activator.getDefault().getPreferenceStore().getString(Preferences.INCREMENTAL_LAYOUT_MODE))
				.orElse(IncrementalLayoutMode.LAYOUT_CONTENTS);

		final boolean preferLayoutContainer = currentLayoutMode != IncrementalLayoutMode.LAYOUT_CONTENTS;
		final List<DiagramNode> nodesToLayout = DiagramElementLayoutUtil
				.filterUnusedNodes(getNodesToLayoutIncrementally(diagram, preferLayoutContainer, new HashSet<>()));

		if (nodesToLayout.size() == 0) {
			return;
		}

		if (currentLayoutMode == IncrementalLayoutMode.LAYOUT_DIAGRAM) {
			DiagramElementLayoutUtil.layout(incrementalLayoutLabel, diagram, new LayoutOptionsBuilder().build());
		} else {
			DiagramElementLayoutUtil.layout(mod, nodesToLayout,
					new StyleCalculator(diagram.getConfiguration(), StyleProvider.EMPTY),
					new LayoutOptionsBuilder().build());

			// Set Position. Need to do this when just laying out contents
			// TODO: Improve algorithm.
			for (final DiagramNode dn : nodesToLayout) {
				if (dn instanceof DiagramElement) {
					final DiagramElement de = (DiagramElement) dn;
					if (!de.hasPosition()) {
						if (de.getDockArea() == null) {
							mod.setPosition(de, new Point(0.0, 0.0));
						} else if (de.getDockArea() != DockArea.GROUP && de.getParent() instanceof DiagramElement) {
							final DiagramElement parent = (DiagramElement) de.getParent();
							final DockingPosition defaultDockingPosition = de
									.getGraphicalConfiguration().defaultDockingPosition;
							final DockArea defaultDockArea = defaultDockingPosition.getDockArea();

							if (parent.hasSize()) {
								// TODO: Rename?
								final Stream<DiagramElement> others = parent.getDiagramElements().stream().filter(
										c -> c.hasPosition() && c.hasSize() && c.getDockArea() == defaultDockArea);

								double t; // TODO: Rename
								if (defaultDockingPosition == DockingPosition.TOP
										|| defaultDockingPosition == DockingPosition.BOTTOM) {
									t = others.max(Comparator.comparingDouble(c -> c.getY()))
											.map(c -> c.getX() + c.getWidth()).orElse(0.0);
								} else {
									t = others.max(Comparator.comparingDouble(c -> c.getY()))
											.map(c -> c.getY() + c.getHeight()).orElse(0.0);
								}

								// TODO: Need padding
								// TODO: Will this cause parent to resize?

								// Set position based on the docking position
								switch (defaultDockingPosition) {
								case TOP:
									mod.setPosition(de, new Point(t, 0));
									break;
								case BOTTOM:
									mod.setPosition(de, new Point(t, parent.getHeight()));
									break;
								case LEFT:
									mod.setPosition(de, new Point(0, t));
									break;
								case RIGHT:
									mod.setPosition(de, new Point(parent.getWidth(), t));
									break;
								default:
									break;
								}
							}

							mod.setDockArea(de, defaultDockArea);
						}
					}
				}
			}
		}
	}

	// TODO: Rename. Similar to what is in LayoutUtil
	// TODO: Document what alwaysLayoutContainer is. Could pass mode instead? Rename? prefer?
	private static Set<DiagramNode> getNodesToLayoutIncrementally(final DiagramNode node,
			final boolean alwaysLayoutContainer, final Set<DiagramNode> results) {

		for (final DiagramElement child : node.getDiagramElements()) {
			// TODO: Handle case there the node is added after the child is.. Don't want to layout both the child and the parent.
			if (DiagramElementPredicates.isShape(child)) {
				// TODO: Rename
				final boolean positionIsSet = child.hasPosition() || !DiagramElementPredicates.isMoveable(child);
				final boolean sizeIsSet = child.hasSize() || !DiagramElementPredicates.isResizeable(child);

				// The position is set but the size isn't, then layout the child.
				// This occurs when a user has created an element using the palette
				if (positionIsSet && !sizeIsSet) {
					results.add(child);
				} else {
					if (sizeIsSet && positionIsSet) {
						getNodesToLayoutIncrementally(child, alwaysLayoutContainer, results);
					} else {
						// If always layout container is specified, layout container
						// If container does not have any layed out shapes, layout container.
						final boolean layoutContainer = alwaysLayoutContainer
								|| !hasLayedOutShapes(node.getDiagramElements());
						if (layoutContainer) {
							results.add(node);
							break;
						} else {
							results.add(child);
						}
					}
				}
			} else if (DiagramElementPredicates.isConnection(child) && alwaysLayoutContainer
					&& child.getStartElement() != null && child.getEndElement() != null) {
				// Only layout the connection if its bendpoints have not been set regardless of whether it has any bendpoints.
				if (!child.isBendpointsSet()) {
					final Optional<Queryable> ancestor = Queryable.getFirstCommonAncestor(
							child.getStartElement().getContainer(), child.getEndElement().getContainer());
					if (ancestor.isPresent()) {
						results.add((DiagramNode) ancestor.get());
					}
				}
			}
		}

		return results;
	}

	private static boolean hasLayedOutShapes(final Collection<DiagramElement> diagramElements) {
		return diagramElements.stream().anyMatch(de -> (de.hasPosition() || !DiagramElementPredicates.isMoveable(de))
				&& (de.hasSize() || !DiagramElementPredicates.isResizeable(de)));
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


							final Point midpoint = findMidpoint(newBendpoints);

							// Handle labels
							// TODO: Primary labels.
							// TODO: Separate labels
							for (final ElkLabel edgeLabel : edge.getLabels()) {

								// TODO: Need offset for labels and points?
								// Position relative to the start
								final double lx = edgeLabel.getX() - midpoint.x;
								final double ly = edgeLabel.getY() - midpoint.y + edgeLabel.getHeight() / 2.0;

								// TODO: Understand coordinate system
								m.setConnectionPrimaryLabelPosition(de2,
										new Point(lx, ly)); // Connection label position relative to center?
							}
						}
					}
				}
			}
		}
	}

	private static Point findMidpoint(final List<Point> points) {
		if (points.size() < 2) {
			throw new RuntimeException("At least two points must be specified");
		}

		final double totalLength = length(points);
		double lengthToTarget = totalLength / 2.0;

		for (int i = 1; i < points.size(); i++) {
			final Point p1 = points.get(i - 1);
			final Point p2 = points.get(i);
			final double segmentLength = length(p1, points.get(i));
			if (lengthToTarget > segmentLength) {
				System.err.println("A");
				lengthToTarget -= segmentLength;
			} else {
				System.err.println("B");
				final double frac = lengthToTarget / segmentLength;
				return new Point(p1.x + (p2.x - p1.x) * frac, p1.y + (p2.y - p1.y) * frac);
			}
		}

		throw new RuntimeException("Unexpected case: midpoint not found");
	}

	private static double length(final List<Point> points) {
		double totalLength = 0;
		for (int i = 1; i < points.size(); i++) {
			totalLength += length(points.get(i - 1), points.get(i));
		}

		return totalLength;
	}

	private static double length(final Point p1, final Point p2) {
		final double dx = p1.x - p2.x;
		final double dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
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
