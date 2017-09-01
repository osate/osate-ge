package org.osate.ge.internal.ui.handlers;

import java.util.Objects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.service.DiagramLayoutEngine;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osate.ge.internal.elk.TestPropertyValue;

public class LayoutDiagramHandler extends AbstractHandler {
	public final static IProperty<TestPropertyValue> TEST_PROPERTY = new Property<>(
			"org.osate.ge.elk.nodesToProcess");

	// TODO: REname
	public static boolean firstPass = true;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editorPart = Objects.requireNonNull(HandlerUtil.getActiveEditor(event),
				"unable to retrieve active editor");
		// TODO: Support progress bar for layout. There is a parameter ins the layout engine. Test.
		final DiagramLayoutEngine.Parameters params = new DiagramLayoutEngine.Parameters();
		//params.addLayoutRun().configure(ElkNode.class)
		params.addLayoutRun().configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM,
				"org.eclipse.elk.layered");

		// TODO: Execute twice. Need to have different parameters
		// DiagramLayoutEngine.invokeLayout(editorPart, null, params);

		// TODO: Other users of IGraphElementVisitor?

		// LayeredOptions.DIRECTION
		// LayeredOptions.NO_LAYOUT.getClass();
		// CoreOptions.PORT_CONSTRAINTS.getClass();

		// TODO: Better way.. Is there a way to completely ignore some features?

		// params.addLayoutRun().configure(ElkNode.class).setProperty(CoreOptions.ALGORITHM,
		// "org.eclipse.elk.algorithm.layered");
		// .setProperty(LayeredOptions.SPACING_LABEL_NODE, 0.0);

//		params.addLayoutRun(new LayoutConfigurator() {
//			@Override
//			public void visit(final ElkGraphElement element) {
//				super.visit(element); // TODO: Call this aftewards?
//				final TestPropertyValue test = element.getProperty(TEST_PROPERTY);
//
//				if (test != null) {
//					// Set property to null. This prevents the root node from being processed multiple times.
//					element.setProperty(TEST_PROPERTY, null);
//
//					System.err.println("B");
//					test.portInfoMap.values().forEach(ti -> {
//						// TODO: Cleanup to avoid casting
//						final ElkPort port = (ElkPort) ti.layoutElement;
//						final DiagramElement portDiagramElement = (DiagramElement) test.layoutMapping.getGraphMap()
//								.get(ti.layoutElement); // TODO: Check for null
//
//						((ElkNode) ti.layoutParent).getPorts().add(port);
//
//						System.err.println("ADDED PORT");
//
//						// TODO: Set position relative to parent
//
//						// TODO: If multiple layer of nesting.. May need to sort the ordering of the ports..
//						// TODO: Need to have the appropriate offset for positioning ports...
//
//						// TODO: Does fixed position handle docking to the sides or do both x and y need to be set?
//						double portX = 0;
//						double portY = 0;
//
//						// TODO: Assign position to nested ports based on containers
//						// TODO: Need to lay children out instead of putting them on top of each other..
//						// TODO: Should use X and Y already used or should it auto assign new X and Y values?
//						for (DiagramNode tmp = portDiagramElement.getParent(); tmp instanceof DiagramElement; tmp = tmp
//								.getParent()) {
//							final DiagramElement tmpDe = (DiagramElement) tmp;
//
//							// TODO: check for type/node
//							portX += tmpDe.getX();
//							portY += tmpDe.getY();
//
//							System.err.println("PARENT: " + tmpDe);
//							if (tmpDe.getDockArea() != DockArea.GROUP) {
//								break;
//							}
//						}
//
//						port.setLocation(portX, portY);
//						System.err.println("SET: " + portX + " : " + portY);
//
//						// TODO: Need to set a side?
//					});
//
//					test.edgeInfoMap.values().forEach(ti -> {
//						((ElkNode) ti.layoutParent).getContainedEdges().add((ElkEdge) ti.layoutElement);
//						System.err.println("ADDED EDGE");
//					});
//					// System.err.println("TEST: " + test);
//					// TODO. Need to configure nodes so ports aren't moved
//
//					// test.layoutMapping.getLayoutGraph().setProperty(CoreOptions.PORT_CONSTRAINTS,
//					// PortConstraints.FIXED_POS);
//
//					lockPortPositions(test.layoutMapping.getLayoutGraph());
//				}
//
//			}
//
//			// TODO: Rename
//			private void lockPortPositions(final ElkNode n) {
//				n.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
//
//				n.getChildren().forEach(c -> lockPortPositions(c));
//
//			}
//
//		}).configure(ElkGraphElement.class).setProperty(CoreOptions.ALGORITHM,
//				"org.eclipse.elk.layered");

		// TODO: Wrap in single modification.
		// TODO: Use algorithms directly instead of connector, etc?

		firstPass = true;
		DiagramLayoutEngine.invokeLayout(editorPart, null, params);
		firstPass = false;
		DiagramLayoutEngine.invokeLayout(editorPart, null, params);

		return null;
	}

}
