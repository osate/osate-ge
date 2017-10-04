package org.osate.ge.internal.ui.handlers;

import java.util.Objects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.SizeConstraint;
import org.eclipse.elk.core.service.DiagramLayoutEngine;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.elk.TestPropertyValue;

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

public class LayoutDiagramHandler extends AbstractHandler {
	public final static IProperty<TestPropertyValue> TEST_PROPERTY = new Property<>(
			"org.osate.ge.elk.nodesToProcess");

	// TODO: REname
	public static boolean firstPass = true;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editorPart = Objects.requireNonNull(HandlerUtil.getActiveEditor(event),
				"unable to retrieve active editor");

		// TODO: Some options should be set for both passes. Don't set in connector because sometimes settings get cleared?
		// Set using LayoutConfigurator

		// TODO: Support progress bar for layout. There is a parameter ins the layout engine. Test.
		final DiagramLayoutEngine.Parameters params = new DiagramLayoutEngine.Parameters();
		//params.addLayoutRun().configure(ElkNode.class)
		params.addLayoutRun().configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM,
				"org.eclipse.elk.layered");

		params.addLayoutRun(new LayoutConfigurator() {
			@Override
			public void visit(final ElkGraphElement element) {
				super.visit(element); // TODO: Call this afterwards?
				final TestPropertyValue test = element.getProperty(TEST_PROPERTY);

				// TODO: Needed because first pass removes properties?
				// TODO: Could be set as part of the layout run's options?
				if (element instanceof ElkNode) {
					element.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, SizeConstraint.minimumSizeWithPorts());

					// TODO: Use node -> node spacing for spacing between objects inside a layer. When is that used?
					element.setProperty(CoreOptions.DIRECTION, Direction.RIGHT);
					element.setProperty(
							LayeredOptions.SPACING_NODE_NODE_BETWEEN_LAYERS,
							100.0);
					element.setProperty(
							LayeredOptions.SPACING_NODE_NODE,
							100.0);

					// element.setProperty(CoreOptions.SPACING_COMPONENT_COMPONENT, 100.0);
				}

				// System.err.println(element.getProperty(CoreOptions.NO_LAYOUT));
				element.setProperty(CoreOptions.NO_LAYOUT, false);

				if (test != null) {
					// Set property to null. This prevents the root node from being processed multiple times.
					element.setProperty(TEST_PROPERTY, null);

					// System.err.println("B");
					test.portInfoMap.values().forEach(ti -> {
						// TODO: Cleanup to avoid casting
						final ElkPort port = (ElkPort) ti.layoutElement;
						final DiagramElement portDiagramElement = (DiagramElement) test.layoutMapping.getGraphMap()
								.get(ti.layoutElement); // TODO: Check for null

						((ElkNode) ti.layoutParent).getPorts().add(port);

						// System.err.println("ADDED PORT");

						// TODO: Set position relative to parent

						// TODO: If multiple layer of nesting.. May need to sort the ordering of the ports..
						// TODO: Need to have the appropriate offset for positioning ports...

						// TODO: Does fixed position handle docking to the sides or do both x and y need to be set?
						double portX = 0;
						double portY = 0;

						// TODO: Assign position to nested ports based on containers
						// TODO: Need to lay children out instead of putting them on top of each other..
						// TODO: Should use X and Y already used or should it auto assign new X and Y values?
						for (DiagramNode tmp = portDiagramElement.getParent(); tmp instanceof DiagramElement; tmp = tmp
								.getParent()) {
							final DiagramElement tmpDe = (DiagramElement) tmp;

							// TODO: check for type/node
							portX += tmpDe.getX();
							portY += tmpDe.getY();

							// System.err.println("PARENT: " + tmpDe);
							if (tmpDe.getDockArea() != DockArea.GROUP) {
								break;
							}
						}

						port.setLocation(portX, portY);
						// System.err.println("SET: " + portX + " : " + portY);

						// TODO: Need to set a side?
					});

					test.edgeInfoMap.values().forEach(ti -> {
						((ElkNode) ti.layoutParent).getContainedEdges().add((ElkEdge) ti.layoutElement);
						// System.err.println("ADDED EDGE");
					});
					// System.err.println("TEST: " + test);
					// TODO. Need to configure nodes so ports aren't moved

					// test.layoutMapping.getLayoutGraph().setProperty(CoreOptions.PORT_CONSTRAINTS,
					// PortConstraints.FIXED_POS);

					lockPortPositions(test.layoutMapping.getLayoutGraph());
				}

			}

			// TODO: Rename
			private void lockPortPositions(final ElkNode n) {
				n.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);

				n.getChildren().forEach(c -> lockPortPositions(c));

			}

		})./* setClearLayout(true). */configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM,
				"org.eclipse.elk.layered");

		// TODO: Wrap in single modification.
		// TODO: Use algorithms directly instead of connector, etc?

		firstPass = true;
		DiagramLayoutEngine.invokeLayout(editorPart, null, params);
		firstPass = false;
		DiagramLayoutEngine.invokeLayout(editorPart, null, params);

		return null;
	}

}
