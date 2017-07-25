package org.osate.ge.internal.graphiti.elk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.elk.alg.layered.p3order.CrossingMinimizationStrategy;
import org.eclipse.elk.alg.layered.properties.GreedySwitchType;
import org.eclipse.elk.alg.layered.properties.LayeredOptions;
// TODO: Cleanup
//import org.eclipse.elk.core.klayoutdata.KLayoutData;
//import org.eclipse.elk.core.klayoutdata.KShapeLayout;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.Alignment;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.HierarchyHandling;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortAlignment;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.options.SizeConstraint;
import org.eclipse.elk.core.options.SizeOptions;
import org.eclipse.elk.core.service.DiagramLayoutEngine.Parameters;
import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.ui.IWorkbenchPart;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.NamedElement;
import org.osate.ge.internal.AgeDiagramProvider;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramModification;
import org.osate.ge.internal.diagram.runtime.DiagramModifier;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.diagram.runtime.Point;
import org.osate.ge.internal.graphics.AgeConnection;
import org.osate.ge.internal.graphics.AgeShape;
import org.osate.ge.internal.graphics.Label;
import org.osate.ge.internal.graphiti.services.GraphitiService;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.eclipse.elk.graph.ElkBendPoint;
import org.eclipse.elk.graph.ElkConnectableShape;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart;

// TODO: Labels
// TODO: Insets
// TODO: User configuration using hte layout view
public class AgeLayoutConnector implements IDiagramLayoutConnector {
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
		
		// Prevents exception in some cases but breaks connection layout. Something to do with empty elements? Similiar exception experienced with empty nodes.. 
		// TODO: Experiment with dummy nodes?
		//rootNode.setProperty(LayeredOptions.CROSSING_MINIMIZATION_GREEDY_SWITCH_TYPE, GreedySwitchType.OFF); 
		rootNode.setProperty(CoreOptions.HIERARCHY_HANDLING, HierarchyHandling.INCLUDE_CHILDREN);
		mapping.getGraphMap().put(rootNode, diagram);

	//	How to set. Algorithm... on the config store? (CoreOptions.ALGORITHM, "org.eclipse.elk.layered"); // TODO: Is this the proper way to do it?

		createElkGraphElementsForShapes(diagram, rootNode, mapping);

		// TODO: Creature features? Create feature groups as unmoveable ports.
		
		createElkGraphElementsForConnections(diagram, mapping);
		
		// TODO: Connections. Could be a separate pass. Or could eagerly create things. eager may be better because connections could connect to connections?
		// Does ELK allow edges to connect? Doesn't look like it.
		
		mapping.setLayoutGraph(rootNode);
		
		return mapping;
	}
	
	private void createElkGraphElementsForShapes(final DiagramNode dn, final ElkNode parent, final LayoutMapping mapping) {
		// TODO: This function doesn't handle connection labels.. need to do that in this function or in teh connection function
		for(final DiagramElement de : dn.getDiagramElements()) {
			if(de.getGraphic() instanceof AgeShape) {
				if(de.getGraphic() instanceof Label) {
					// TODO: Handle labels
					final ElkLabel newLabel = ElkGraphUtil.createLabel(parent);
					newLabel.setX(de.getX());
					newLabel.setY(de.getY());
					newLabel.setWidth(de.getWidth());
					newLabel.setHeight(de.getHeight());
					mapping.getGraphMap().put(newLabel, de); // TODO: Do this for all graph elements
					newLabel.setProperty(CoreOptions.NODE_LABELS_PLACEMENT, NodeLabelPlacement.insideTopCenter()); // TODO:
					
					// There is a fixed option but that doens't appear to work properly.
					// TODO: Labels are not moveable but if this is configured properly, it will likely be good enough
					
				} else {
					final ElkNode newNode = ElkGraphUtil.createNode(parent);
					// TODO: Check if shape has position or size
					newNode.setX(de.getX());
					newNode.setY(de.getY());
					newNode.setWidth(de.getWidth());
					newNode.setHeight(de.getHeight());
					mapping.getGraphMap().put(newNode, de); // TODO: Do this for all graph elements
					
					//newNode.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.mrtree");
					
					// If parent is fixed then children are not layed out..
					if(de.getBusinessObject() instanceof AadlPackage) {
						newNode.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.layered");
						//newNode.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.fixed");
					} else {
						final String name = de.getBusinessObject() instanceof NamedElement ? ((NamedElement)de.getBusinessObject()).getQualifiedName() : "";
						if("binding_test::top.impl".equalsIgnoreCase(name)) {
							newNode.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.layered");
							System.err.println("FOUND IT");
						} else {
							//newNode.setProperty(LayeredOptions.NO_LAYOUT, true);
							//newNode.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.fixed");
							//newNode.setProperty(LayeredOptions.POSITION, new KVector(de.getX(), de.getY()));
						}
					}
					
					// TODO: Configure label spacing.
					
					// TODO: Features and feature groups
					// TODO: Labels. Shouldn't duplicate code. Share with code that is above
					// TODO: Only do this is the node has a label
					final ElkLabel newLabel = ElkGraphUtil.createLabel(newNode);
					newLabel.setX(0);
					newLabel.setY(0);
					newLabel.setWidth(de.getWidth()); // TODO: Need to be smaller
					newLabel.setHeight(14); // TODO: Based on lable size
					newLabel.setProperty(CoreOptions.NODE_LABELS_PLACEMENT, NodeLabelPlacement.insideTopCenter()); // TODO:
					
					// TODO: Unpositionable child shapes(extra labels, etc)
					//newLabel.setProperty(LayeredOptions.CROSSING_MINIMIZATION_STRATEGY, CrossingMinimizationStrategy.LAYER_SWEEP
					//newLabel.setProperty(LayeredOptions.CROSSING_MINIMIZATION_GREEDY_SWITCH_TYPE, GreedySwitchType.OFF);

					//LayeredOptions.POSITION
					//LayeredOptions.
					
					createElkGraphElementsForShapes(de, newNode, mapping);
				}
			}
		}		
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
					final ElkEdge newEdge = ElkGraphUtil.createSimpleEdge((ElkConnectableShape)edgeStart, (ElkConnectableShape)edgeEnd);//ElkGraphUtil.createEdge(elkParentNode); // TODO: Coordinate system. Read documentation			
					mapping.getGraphMap().put(newEdge, de); // TODO: Do this for all graph elements
					
					// TODO: Features and feature groups
					// TODO: Labels
					// TODO: Unpositionable child shapes(extra labels, etc)
				}
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
		// TODO Auto-generated method stub
		System.err.println("TODO: Apply layout");
				
		final AgeDiagramEditor editor = ((AgeDiagramEditor)mapping.getWorkbenchPart());
		final TransactionalEditingDomain editingDomain = editor.getEditingDomain();
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
			@Override
			protected void doExecute() {
				// TODO Auto-generated method stub
				System.err.println("TADA");
				
				final AgeDiagram diagram = getDiagram(mapping.getWorkbenchPart());
				diagram.modify(new DiagramModifier() {				
					@Override
					public void modify(final DiagramModification m) {						
						// Modify shapes
						for (Entry<ElkGraphElement, Object> entry : mapping.getGraphMap().entrySet()) {
							final ElkGraphElement elkElement = (ElkGraphElement)entry.getKey();
							final DiagramNode dn = (DiagramNode)entry.getValue();
							
							// TODO: Is there a modified flag for elk element. One appeared to be mentioned
							
							if(dn instanceof DiagramElement) {
								final DiagramElement de = (DiagramElement)dn;
								if(de.getGraphic() instanceof AgeShape) {
									// TODO: Handle labels. At least handle connection labels. Other labels aren't positionable.
									if(!(de.getGraphic() instanceof Label)) {
										if(elkElement instanceof ElkShape) {
											final ElkShape elkShape = (ElkShape)elkElement;
											// TODO: Only set appropriate fields
											// TODO: Should runtime diagram use doubles?
											//System.err.println("POSITION: (" + de.getX() + ", " + de.getY() + ") -> (" + elkShape.getX() + ", " + elkShape.getY() + ")");
											m.setPosition(de, new Point((int)elkShape.getX(), (int)elkShape.getY()));
											m.setSize(de, new Dimension((int)elkShape.getWidth(), (int)elkShape.getHeight()));
										}
									} else {
										// TODO: For testing
										if(elkElement instanceof ElkShape) {
											final ElkShape elkShape = (ElkShape)elkElement;
											System.err.println(elkShape.getX() + " : "  + elkShape.getWidth());
										}
									}
								}
							}
						}

						int cCount = 0; // TODO: Remove
						int cwsCount = 0; // TODO: Remove	
						
						// Modify Connections
						for (Entry<ElkGraphElement, Object> entry : mapping.getGraphMap().entrySet()) {
							final ElkGraphElement elkElement = (ElkGraphElement)entry.getKey();
							final DiagramNode dn = (DiagramNode)entry.getValue();
							
							// TODO: Is there a modified flag for elk element. One appeared to be mentioned
							
							if(dn instanceof DiagramElement) {
								final DiagramElement de = (DiagramElement)dn;
								if(de.getGraphic() instanceof AgeConnection) {
									// TODO: Hierarchical edges aren't being handled. Because of the layout algorithm?
									
								//	System.err.println("C");
									if(elkElement instanceof ElkEdge) {
										final ElkEdge edge = (ElkEdge)elkElement;
										cCount++;
										
										if(edge.getSections().size() > 0) {
											// TODO: Check that it is 1. Edges that represent multiple connections?
											final ElkEdgeSection es = edge.getSections().get(0);
											// TODO: Understand edge section fields. 
											// TODO: Coordinate transformations.
											
											
											
											final Point parentPosition = getAbsolutePosition(de.getContainer());
											final Point elkContainerPosition = getAbsolutePosition((DiagramNode)mapping.getGraphMap().get(edge.getContainingNode()));
											final int offsetX = elkContainerPosition.x;// - parentPosition.x; // TODO: Double
											final int offsetY = elkContainerPosition.y;// - parentPosition.y; // TODO: Double
											/*System.err.println("BENDPOINTS " + es.getBendPoints().size());
											System.err.println(es.getStartX() + " : "  + es.getStartY() + " : " + offsetX + ", " + offsetY + " : " + elkContainerPosition.y + " : " + parentPosition.y);
											for(final ElkBendPoint bp : es.getBendPoints()) {
												System.err.println("BP: " + bp.getX() + " : " + bp.getY());
											}
											*/
											final List<Point> newBendpoints;
											if(es.getBendPoints().size() == 0) {
												newBendpoints = null;
											} else {
												// TODO: Starting and ending points. Usage allows more accurately using ELK layout but causes problems with connection endings
												//newBendpoints = new ArrayList<>(es.getBendPoints().size()+2);
												//newBendpoints.add(new Point((int)es.getStartX() + offsetX, (int)es.getStartY() + offsetY)); // TODO: doubles
												
												newBendpoints = es.getBendPoints().stream().
														map(bp -> new Point((int)bp.getX() + offsetX, (int)bp.getY() + offsetY)). // TODO: Use doubles for points and dimensions
														collect(Collectors.toList());
												
												//newBendpoints.add(new Point((int)es.getEndX() + offsetX, (int)es.getEndY() + offsetY)); // TODO: doubles
											}
											
											m.setBendpoints(de, newBendpoints);
											cwsCount++;
										}
									}
								}
							}
						}
						
						System.err.println("CCOUNT: " + cCount + " : " + cwsCount);
					}
				});
			}
		});
       // editingDomain.getCommandStack().execute(mapping.getProperty(LAYOUT_COMMAND));
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
