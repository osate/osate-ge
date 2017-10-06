package org.osate.ge.internal.elk;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.elk.alg.layered.options.CycleBreakingStrategy;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.options.LayeringStrategy;
import org.eclipse.elk.alg.layered.options.NodePlacementStrategy;
import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.service.DiagramLayoutEngine;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.ui.IEditorPart;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramElementPredicates;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

public class LayoutUtil {
	public static void layout(final String label, final IEditorPart editor, final List<DiagramElement> diagramElements,
			final LayoutOptions options) {
		if (!(editor instanceof AgeDiagramEditor)) {
			throw new RuntimeException("Editor must be an " + AgeDiagramEditor.class.getName());
		}

		// TODO: Filter out connections. Have a setEnabled() implementation that checks for shapes?
		// Only pass in top level objects to algorithm.. Don't pass in children...

		// TODO: Handle options

		// TODO: Should set port constraints option regardless...
		System.err.println("ZZZ: " + options.lockTopLevelPorts);

		final String layoutAlgorithm = "org.eclipse.elk.layered";
		final DiagramLayoutEngine.Parameters params = new DiagramLayoutEngine.Parameters();

//		params.addLayoutRun().configure(ElkGraphElement.class)
//		.setProperty(CoreOptions.ALGORITHM, layoutAlgorithm);

		final LayoutConfigurator config = params.addLayoutRun(new LayoutConfigurator() {
//			@Override
//			public void visit(final ElkGraphElement element) {
//				System.err.println("Z");
//				// Fix the position of the top level ports if the lock top level ports option is set.
//				if (options.lockTopLevelPorts) {
//					System.err.println("A");
//					if (element instanceof ElkNode) {
//						System.err.println("B");
//						final ElkNode n = (ElkNode) element;
//						final boolean isRoot = n.getParent() == null || n.getParent().getParent() == null; // TODO: Share with layout connector
//						System.err.println("C: " + isRoot);
//						if (isRoot) {
//							System.err.println("SETTING");
//							n.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
//						}
//					}
//				}
//			}
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

		config.configure(ElkNode.class).setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS); // TODO: Limit scope

		// TODO: Need a check to determine whether a run is actually needed

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

		final AgeDiagram diagram = ((AgeDiagramEditor) editor).getAgeDiagram();

		// Modify the diagram.
		diagram.modify(label, m -> {
			// Passing the modification isn't necessary because new modifications are automatically part of the active modification
			if (diagramElements == null) {
				DiagramLayoutEngine.invokeLayout(editor, diagram, params);
			} else {
				// Only layout shapes. Also filter out any descendants of specified diagram elements
				final List<DiagramElement> elementsToLayout = diagramElements.stream()
						.filter(de -> DiagramElementPredicates.isShape(de) && !containsAnyAncestor(diagramElements, de))
						.collect(Collectors.toList());
				for (final DiagramElement de : elementsToLayout) {
					DiagramLayoutEngine.invokeLayout(editor, de, params);
				}
			}
		});
		// TODO: Multipass
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
