package org.osate.ge.internal.elk;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.elk.alg.layered.options.CycleBreakingStrategy;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.options.LayeringStrategy;
import org.eclipse.elk.alg.layered.options.NodePlacementStrategy;
import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.service.DiagramLayoutEngine;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorPart;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramElementPredicates;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

public class LayoutUtil {
	public static void layout(final String label, final IEditorPart editor, final List<DiagramElement> diagramElements,
			final LayoutOptions options) {
		if (!(editor instanceof AgeDiagramEditor)) {
			throw new RuntimeException("Editor must be an " + AgeDiagramEditor.class.getName());
		}

		// Create configurators which will change the graph based on layout options
		final String layoutAlgorithm = "org.eclipse.elk.layered";
		final DiagramLayoutEngine.Parameters params = new DiagramLayoutEngine.Parameters();

		// Call setOverrideDiagramConfig(false) so that the specified layout configurator won't be replaced by the diagram one.
		final LayoutConfigurator config = params.setOverrideDiagramConfig(false).addLayoutRun(new LayoutConfigurator() {
			@Override
			public void visit(final ElkGraphElement element) {
				// Fix the position of the top level ports if the lock top level ports option is set.
				if(element instanceof ElkNode) {
					final ElkNode n = (ElkNode) element;
					PortConstraints portConstraints = PortConstraints.FIXED_SIDE;
					if (options.lockTopLevelPorts) {
						final boolean isRoot = n.getParent() == null || n.getParent().getParent() == null; // TODO: Share with layout connector
						if (isRoot) {
							portConstraints = PortConstraints.FIXED_POS;
						}
					}

					n.setProperty(CoreOptions.PORT_CONSTRAINTS, portConstraints);
				} else if (element instanceof ElkPort) {
					final ElkPort p = (ElkPort) element;
					final LayoutMapping layoutMapping = getLayoutMapping(p);
					final DiagramElement de = (DiagramElement) layoutMapping.getGraphMap().get(p);

					// These properties are set here instead of in the layout connector because they need to be set based on the specific layout being
					// performed.

					// TODO: When handling feature groups, will need to populate these properties for added ports.
					// Determine the port side
					final PortSide portSide;
					if (options.interactive || isTopLevel(p)) {
						// Don't change port sides if trying to avoid significant changes.
						portSide = getPortSide(de);
					} else {
						// Otherwise change the port side based on the diagram element's default docking configuration
						portSide = getPortSideForNonGroupDockArea(
								de.getGraphicalConfiguration().defaultDockingPosition.getDockArea());
					}

					p.setProperty(CoreOptions.PORT_SIDE, portSide);

					// Set the port border offset based on the port side
					if (PortSide.SIDES_NORTH_SOUTH.contains(portSide)) {
						p.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getHeight());
					} else {
						p.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getWidth());
					}
				}
			}
		});// configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM, layoutAlgorithm);
		// final IPropertyHolder ph =
		final IPropertyHolder ph = config.configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM,
				layoutAlgorithm);
		if (options.interactive) {
			ph.setProperty(LayeredOptions.CYCLE_BREAKING_STRATEGY, CycleBreakingStrategy.INTERACTIVE); // Must be interactive if layering is interactive?
			ph.setProperty(LayeredOptions.LAYERING_STRATEGY, LayeringStrategy.INTERACTIVE);
			ph.setProperty(LayeredOptions.NODE_PLACEMENT_STRATEGY, NodePlacementStrategy.INTERACTIVE);
			// This has produced odd connection routing in some cases. Disabling for now.
			// ph.setProperty(LayeredOptions.CROSSING_MINIMIZATION_STRATEGY, CrossingMinimizationStrategy.INTERACTIVE);
		}

		// TODO: Need a check to determine whether a second layout is actually needed. Only used for feature groups
		// TODO: Will need to share some of the property setting behavior with the first configurator. Such as setting port sides, etc

		// TODO: Second layout causing issues?
//		// The second layout pass. This pass is responsible for expanding feature groups.
//		params.addLayoutRun(new LayoutConfigurator() {
//			@Override
//			public void visit(final ElkGraphElement element) {
//				System.err.println("Z2");
//				// System.err.println(element.getProperties());
//			}
//		}).configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM, layoutAlgorithm);
		// TODO: Need to deicde whether to clear the properties.. What properties are those? Wouldn't that overrite the ones we are using to transfer
		// information?

		// Modify the diagram.
		final AgeDiagram diagram = ((AgeDiagramEditor) editor).getAgeDiagram();
		diagram.modify(label, m -> {
			// Determine the diagram nodes to layout
			final List<DiagramNode> nodesToLayout;
			if (diagramElements == null) {
				nodesToLayout = Collections.singletonList(diagram);
			} else {
				// Only layout shapes. Also filter out any descendants of specified diagram elements
				nodesToLayout = diagramElements.stream()
						.filter(de -> DiagramElementPredicates.isShape(de) && !containsAnyAncestor(diagramElements, de))
						.collect(Collectors.toList());
			}

			// Passing the modification isn't necessary because new modifications are automatically part of the active modification
			for (final DiagramNode dn : nodesToLayout) {
				DiagramLayoutEngine.invokeLayout(editor, dn, params);
			}
		});
	}

	private static boolean isTopLevel(final ElkPort port) {
		return isTopLevel(port.getParent());
	}

	private static boolean isTopLevel(final ElkNode n) {
		return n.getParent() == null || n.getParent() == null;
	}

	private static LayoutMapping getLayoutMapping(final ElkGraphElement ge) {
		// Get the root element
		EObject root = ge;
		while(root.eContainer() != null) {
			root = root.eContainer();
		}

		if (!(root instanceof ElkNode)) {
			return null;
		}

		return ((ElkNode) root).getProperty(AgeProperties.LAYOUT_MAPPING);
	}

	/**
	 * Returns true if the specified collection contains any ancestor for the specified diagram node
	 * @param c
	 * @param e
	 * @return
	 */
	private static boolean containsAnyAncestor(final Collection<DiagramElement> c, final DiagramNode n) {
		for (DiagramNode ancestor = n.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
			if (c.contains(ancestor)) {
				return true;
			}
		}

		return false;
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
		case GROUP:
			return getPortSide(de.getParent());

		default:
			return getPortSideForNonGroupDockArea(dockArea);
		}
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

// OLD: CLEANUP
// General Idea:
// TODO: Cleanup
// ELK doesn't support nested features. In order to properly support feature groups, the diagram is layed out twice.
// The first does not include feature group children but *will*(TODO) contain dummy connections between there parents. This will ensure
// that nodes are sized properly and allow the layout algorithm to set the position of the ports.
// After the first pass, the location of ports will be locked and ports and edges will be added for nested features. Dummy
// connections will be removed. The second layout will do the actual layout which is used.
// Layout the diagram in multiple passes.
// The first pass will

// Open questions:
// Understand what clear layout does. If set to true on the second pass, the spacing properties set as part of visit() doesn't seem to be processed.
// Why?
//public final static IProperty<TestPropertyValue> TEST_PROPERTY = new Property<>(
//	"org.osate.ge.elk.nodesToProcess");
//
//// TODO: REname
//public static boolean firstPass = true;

//	final IEditorPart editorPart = Objects.requireNonNull(HandlerUtil.getActiveEditor(event),
//			"unable to retrieve active editor");
//
//	// TODO: Some options should be set for both passes. Don't set in connector because sometimes settings get cleared?
//	// Set using LayoutConfigurator
//
//	// TODO: Support progress bar for layout. There is a parameter ins the layout engine. Test.
//	final DiagramLayoutEngine.Parameters params = new DiagramLayoutEngine.Parameters();
//	//params.addLayoutRun().configure(ElkNode.class)
//	params.addLayoutRun().configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM,
//			"org.eclipse.elk.layered");

//	params.addLayoutRun(new LayoutConfigurator() {
//		@Override
//		public void visit(final ElkGraphElement element) {
//			super.visit(element); // TODO: Call this afterwards?
//			final TestPropertyValue test = element.getProperty(TEST_PROPERTY);
//
//			// TODO: Needed because first pass removes properties?
//			// TODO: Could be set as part of the layout run's options?
//			if (element instanceof ElkNode) {
//				final EnumSet<SizeConstraint> nodeSizeConstraints = EnumSet.of(SizeConstraint.PORTS,
//						SizeConstraint.MINIMUM_SIZE, SizeConstraint.NODE_LABELS);
//				element.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, nodeSizeConstraints);
//
//				// TODO: Use node -> node spacing for spacing between objects inside a layer. When is that used?
//				element.setProperty(CoreOptions.DIRECTION, Direction.RIGHT);
//				element.setProperty(
//						LayeredOptions.SPACING_NODE_NODE_BETWEEN_LAYERS,
//						100.0);
//				element.setProperty(
//						LayeredOptions.SPACING_NODE_NODE,
//						100.0);
//
//				// element.setProperty(CoreOptions.SPACING_COMPONENT_COMPONENT, 100.0);
//			}
//
//			// System.err.println(element.getProperty(CoreOptions.NO_LAYOUT));
//			element.setProperty(CoreOptions.NO_LAYOUT, false);
//
//			if (test != null) {
//				// Set property to null. This prevents the root node from being processed multiple times.
//				element.setProperty(TEST_PROPERTY, null);
//
//				// System.err.println("B");
//				test.portInfoMap.values().forEach(ti -> {
//					// TODO: Cleanup to avoid casting
//					final ElkPort port = (ElkPort) ti.layoutElement;
//					final DiagramElement portDiagramElement = (DiagramElement) test.layoutMapping.getGraphMap()
//							.get(ti.layoutElement); // TODO: Check for null
//
//					((ElkNode) ti.layoutParent).getPorts().add(port);
//
//					// System.err.println("ADDED PORT");
//
//					// TODO: Set position relative to parent
//
//					// TODO: If multiple layer of nesting.. May need to sort the ordering of the ports..
//					// TODO: Need to have the appropriate offset for positioning ports...
//
//					// TODO: Does fixed position handle docking to the sides or do both x and y need to be set?
//					double portX = 0;
//					double portY = 0;
//
//					// TODO: Assign position to nested ports based on containers
//					// TODO: Need to lay children out instead of putting them on top of each other..
//					// TODO: Should use X and Y already used or should it auto assign new X and Y values?
//					for (DiagramNode tmp = portDiagramElement.getParent(); tmp instanceof DiagramElement; tmp = tmp
//							.getParent()) {
//						final DiagramElement tmpDe = (DiagramElement) tmp;
//
//						// TODO: check for type/node
//						portX += tmpDe.getX();
//						portY += tmpDe.getY();
//
//						// System.err.println("PARENT: " + tmpDe);
//						if (tmpDe.getDockArea() != DockArea.GROUP) {
//							break;
//						}
//					}
//
//					port.setLocation(portX, portY);
//					// System.err.println("SET: " + portX + " : " + portY);
//
//					// TODO: Need to set a side?
//				});
//
//				test.edgeInfoMap.values().forEach(ti -> {
//					((ElkNode) ti.layoutParent).getContainedEdges().add((ElkEdge) ti.layoutElement);
//					// System.err.println("ADDED EDGE");
//				});
//				// System.err.println("TEST: " + test);
//				// TODO. Need to configure nodes so ports aren't moved
//
//				// test.layoutMapping.getLayoutGraph().setProperty(CoreOptions.PORT_CONSTRAINTS,
//				// PortConstraints.FIXED_POS);
//
//				lockPortPositions(test.layoutMapping.getLayoutGraph());
//			}
//
//		}
//
//		// TODO: Rename
//		private void lockPortPositions(final ElkNode n) {
//			n.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
//
//			n.getChildren().forEach(c -> lockPortPositions(c));
//
//		}
//
//	})./* setClearLayout(true). */configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM,
//			"org.eclipse.elk.layered");
//
//	// TODO: Use algorithms directly instead of connector, etc?
//
//	firstPass = true;
//	DiagramLayoutEngine.invokeLayout(editorPart, null, params);
//	// firstPass = false;
//	// DiagramLayoutEngine.invokeLayout(editorPart, null, params);
}
