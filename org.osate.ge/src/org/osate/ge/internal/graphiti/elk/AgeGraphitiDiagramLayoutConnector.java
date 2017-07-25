package org.osate.ge.internal.graphiti.elk;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
// TODO: Cleanup
//import org.eclipse.elk.core.klayoutdata.KLayoutData;
//import org.eclipse.elk.core.klayoutdata.KShapeLayout;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.Alignment;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.PortAlignment;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.options.SizeConstraint;
import org.eclipse.elk.core.options.SizeOptions;
import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.ui.IWorkbenchPart;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.eclipse.elk.graph.properties.IPropertyHolder;
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

// TODO: Are GraphFeatures set automatically or is that something that should be specified? Same with Hierarchy Handling
// TODO: Labels
// TODO: Insets
// TODO: Is there a way to specify a subset of sides that can be used instead of just free?
// When to use interactive flag?

// FixedLayout vs No Layout
// Port Anchors

public class AgeGraphitiDiagramLayoutConnector implements IDiagramLayoutConnector {
	@Override
	public LayoutMapping buildLayoutGraph(final IWorkbenchPart workbenchPart, final Object diagramPart) {
		if(diagramPart != null) {
			// TODO
			throw new RuntimeException("Unhandled case. Only laying out the entire editor is supported.");
		}
		
		/*
		// Get the root shape
		final AgeDiagramEditor editor = ((AgeDiagramEditor)workbenchPart);
		final EditPart editorContent = editor.getGraphicalViewer().getContents();
        final PictogramElement pe = ((IPictogramElementEditPart)editorContent).getPictogramElement(); // TODO
        final ContainerShape rootShape;
        if (pe instanceof Diagram) {
            rootShape = (ContainerShape)pe;
        } else {
        	throw new UnsupportedOperationException("Unsupported pictogram element: " + pe);
        }
        
		final LayoutMapping mapping = new LayoutMapping(workbenchPart);
		final KNode topNode;
		if (rootShape instanceof Diagram) {
            topNode = ElkUtil.createInitializedNode();
            final KShapeLayout shapeLayout = topNode.getData(KShapeLayout.class);
            final GraphicsAlgorithm ga = rootShape.getGraphicsAlgorithm();
            shapeLayout.setPos(ga.getX(), ga.getY());
            shapeLayout.setSize(ga.getWidth(), ga.getHeight());
            mapping.getGraphMap().put(topNode, rootShape);
        } else {
        	// TODO
            //topNode = createNode(mapping, null, rootShape);
        	throw new RuntimeException("TODO");
        }
		
		
		mapping.setParentElement(rootShape);
		mapping.setLayoutGraph(topNode);
		
		
		// TODO: Handle selection, etc.
        
		// TODO: Need property service
		final PropertyService propertyService = (PropertyService)editor.getAdapter(PropertyService.class);
		
		buildLayoutGraphRecursively(mapping, propertyService, rootShape, topNode);
		
		
		//System.err.println("DIAGRAM PART: " + diagramPart + " : " + propertyService);
		
		// TODO: Connections
		
		return mapping;
		*/
		// TODO
		return null;
	}
	/*
	// TODO: Render parameters for consistency?
	protected void buildLayoutGraphRecursively(final LayoutMapping mapping, final PropertyService propertyService, final ContainerShape parentElement, final KNode parentNode) {
        for (final Shape shape : parentElement.getChildren()) {        	
        	// TODO: Use different property?
        	// TODO: Handle ghosted and invisible elements properly
        	if(propertyService.isLogicalTreeNode(shape)) {
            	System.err.println("SHAPE: " + shape);
        		KGraphElement newNode = createChild(mapping, propertyService, parentNode, shape);
                if (newNode instanceof KNode && shape instanceof ContainerShape) {
                    // process the children of the container shape
                    buildLayoutGraphRecursively(mapping, propertyService, (ContainerShape)shape, (KNode)newNode);
                }
        	}
        }
    }
	*/
	/*
	protected KGraphElement createChild(final LayoutMapping mapping, final PropertyService propertyService, final KNode parentNode, final Shape shape) {
		final String dockArea = propertyService.getDockArea(shape);

        // TODO: Sort through
        //CoreOptions.NO_LAYOUT
        //CoreOptions.NODE_SIZE_OPTIONS
        //CoreOptions.PORT_ANCHOR
        //CoreOptions.PORT_CONSTRAINTS
        //CoreOptions.PORT_SIDE
		
		if(dockArea == null) {
			final KNode childNode = ElkUtil.createInitializedNode();
	        mapping.getGraphMap().put(childNode, shape);	        
			childNode.setParent(parentNode);
	       
	        final KShapeLayout nodeLayout = childNode.getData(KShapeLayout.class);
			nodeLayout.setProperty(CoreOptions.PORT_ALIGNMENT_BASIC, PortAlignment.BEGIN);
	        nodeLayout.setXpos(shape.getGraphicsAlgorithm().getX());
	        nodeLayout.setYpos(shape.getGraphicsAlgorithm().getY());
	        nodeLayout.setWidth(shape.getGraphicsAlgorithm().getWidth());
	        nodeLayout.setHeight(shape.getGraphicsAlgorithm().getHeight());
	        nodeLayout.resetModificationFlag();

	        // TODO: Remove minimum size? WIll need support for labels, etc. Otherwise the size will be set to a small size.
	        nodeLayout.setProperty(CoreOptions.NODE_SIZE_MINIMUM, new KVector(100, 100));
	        nodeLayout.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, EnumSet.of(SizeConstraint.MINIMUM_SIZE));

	        // TODO: Need to create label(s)
	        
	        return childNode;
		} else {
			// TODO: Major
			// TODO: What about feature groups?		
			final KPort port = ElkUtil.createInitializedPort();
			mapping.getGraphMap().put(port, shape);
			port.setNode(parentNode);	

	        final KShapeLayout portLayout = port.getData(KShapeLayout.class);
	        
	        // TODO: How to fix a port to a specific side while allowing others to float between sides?
	        //portLayout.setProperty(CoreOptions.PORT_SIDE, PortSide.EAST); // Seems to work in conjunction with fixed side on the node layout. FIXED_SIZE
	        
	        // TODO: This exposes the issue with the positioning... The port is always positioned so that it is outside the node...
	        
	        
	       // portLayout.setProperty(CoreOptions.PORT_ANCHOR, new KVector(50.0, 50.0));
	        portLayout.setXpos(shape.getGraphicsAlgorithm().getX());
	        portLayout.setYpos(shape.getGraphicsAlgorithm().getY());
	        
	        // ports will overlap.
	        portLayout.setWidth(shape.getGraphicsAlgorithm().getWidth());
	        portLayout.setHeight(shape.getGraphicsAlgorithm().getHeight());
	        portLayout.resetModificationFlag();
	        portLayout.setProperty(CoreOptions.PORT_BORDER_OFFSET, (float)-shape.getGraphicsAlgorithm().getWidth()); // TODO: How does this work with top and bottom ports?
	        
	        // TODO: Share code between port and shapes	        
	        // TODO: Restrict sides, etc
	        
	        return port;
		}
        
	}
*/
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
				/*
				// TODO: Graphiti ELK plugin has a coordinate offset that it adds to all the direct children of the layout graph. Prevent everything from being offset?
				
				final IFeatureProvider fp = editor.getDiagramTypeProvider().getFeatureProvider();
				
				for (Entry<KGraphElement, Object> entry : mapping.getGraphMap().entrySet()) {
					final KGraphElement ge = entry.getKey();
					final PictogramElement pe = (PictogramElement)entry.getValue();
					
					if(pe instanceof Diagram) {
						// TODO: Ignore?
					} else if(pe instanceof Shape) {
						final KShapeLayout shapeLayout = ge.getData(KShapeLayout.class);

						// TODO: Need to update layout for ports and other components.
						// TODO: Decide how to handle the updating of port sides.
						
						// TODO: Check for null
						System.err.println(ge + " : " + shapeLayout.getXpos() + "," + shapeLayout.getYpos() + " : " + shapeLayout.getWidth() + " : " + shapeLayout.getHeight());
						//System.err.println(shapeLayout.getProperties()); // TODO: Get port side, etc...
						if(shapeLayout.isModified()) {
							// TODO: Need to understand port positioning
							pe.getGraphicsAlgorithm().setX(Math.round(shapeLayout.getXpos()));
							pe.getGraphicsAlgorithm().setY(Math.round(shapeLayout.getYpos()));
							pe.getGraphicsAlgorithm().setWidth(Math.round(shapeLayout.getWidth()));
							pe.getGraphicsAlgorithm().setHeight(Math.round(shapeLayout.getHeight()));
						}
						
					}
					
					//System.err.println(ge + " : " + pe);
		         //   command.add(entry.getKey(), (PictogramElement) entry.getValue());
		        }
		        		          
		          */
			}
		});
       // editingDomain.getCommandStack().execute(mapping.getProperty(LAYOUT_COMMAND));
	}
}
