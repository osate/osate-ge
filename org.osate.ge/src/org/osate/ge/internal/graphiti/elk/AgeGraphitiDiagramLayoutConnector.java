package org.osate.ge.internal.graphiti.elk;

import org.eclipse.elk.core.klayoutdata.KShapeLayout;
import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.ui.IWorkbenchPart;
import org.osate.ge.internal.services.PropertyService;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.eclipse.elk.graph.KNode;
import org.eclipse.elk.graph.KPort;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart;

public class AgeGraphitiDiagramLayoutConnector implements IDiagramLayoutConnector {
	@Override
	public LayoutMapping buildLayoutGraph(final IWorkbenchPart workbenchPart, final Object diagramPart) {
		System.err.println("TODO: Build layout graph");
		
		if(diagramPart != null) {
			// TODO
			throw new RuntimeException("Unhandled case. Only laying out the entire editor is supported.");
		}
		
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
		
		
		System.err.println("DIAGRAM PART: " + diagramPart + " : " + propertyService);
		
		// TODO: Connections
		
		return mapping;
	}
	
	protected void buildLayoutGraphRecursively(final LayoutMapping mapping, final PropertyService propertyService, final ContainerShape parentElement, final KNode parentNode) {
        for (final Shape shape : parentElement.getChildren()) {        	
        	// TODO: Use different property?
        	// TODO: Handle ghosted and invisible elements properly
        	if(propertyService.isLogicalTreeNode(shape)) {
            	System.err.println("SHAPE: " + shape);
        		KNode node = createNode(mapping, parentNode, shape);
                if (shape instanceof ContainerShape) {
                    // process the children of the container shape
                    buildLayoutGraphRecursively(mapping, propertyService, (ContainerShape)shape, node);
                }
        	}
        }
    }
	
	protected KNode createNode(final LayoutMapping mapping, final KNode parentNode, final Shape shape) {
		final KNode childNode = ElkUtil.createInitializedNode();
        childNode.setParent(parentNode);
        
        // TODO: Set shape layout
        
        // TODO: Not all of these shapes will be nodes.. Some will be ports
        final KPort port = ElkUtil.createInitializedPort();
        //port.setNode(parentNode);
        
        // TODO: Can ports have children.(Feature Groups)
        // Looks like they must be children of nodes...

		// TODO. Set settings.
        // TODO: Handle docked shapes
        
        // TODO: Set sides for ports, TODO: set constraints, etc
        // TODO: Look into hierarchy handling
        
        
		return childNode;
	}
	
	@Override
	public void applyLayout(final LayoutMapping mapping, final IPropertyHolder settings) {
		// TODO Auto-generated method stub
		System.err.println("TODO: Apply layout");
		
		//final TransactionalEditingDomain editingDomain = ((DiagramEditor)mapping.getWorkbenchPart()).getEditingDomain();
       // editingDomain.getCommandStack().execute(mapping.getProperty(LAYOUT_COMMAND));
	}
}
