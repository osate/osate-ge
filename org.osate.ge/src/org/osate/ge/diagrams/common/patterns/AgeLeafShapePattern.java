/*******************************************************************************
 * Copyright (C) 2013 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The US Government has unlimited rights in this work in accordance with W31P4Q-10-D-0092 DO 0073.
 *******************************************************************************/
package org.osate.ge.diagrams.common.patterns;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IPeCreateService;
import org.osate.aadl2.Element;
import org.osate.ge.diagrams.common.AadlElementWrapper;
import org.osate.ge.services.AnchorService;
import org.osate.ge.services.VisibilityService;

/**
 * Class for shapes that have their inside recreated on updating. Even though they may have child shapes they are created as an automatic unit because their children may 
 * be recreated during updates.
 * @author philip.alldredge
 *
 */
public abstract class AgeLeafShapePattern extends AgePattern {
	private final AnchorService anchorUtil;
	private VisibilityService visibilityHelper;
	
	public AgeLeafShapePattern(final AnchorService anchorUtil, final VisibilityService visibilityHelper) {
		this.anchorUtil = anchorUtil;
		this.visibilityHelper = visibilityHelper;
	}
	
	@Override
	public final PictogramElement add(final IAddContext context) {
		final Element element = (Element)AadlElementWrapper.unwrap(context.getNewObject());
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();

        // Create the container shape
        final ContainerShape container = peCreateService.createContainerShape(context.getTargetContainer(), true);
        link(container, new AadlElementWrapper(element));

		// Create the GA and inner Shape
        createGaAndInnerShapes(container, element, context.getX(), context.getY());
        
        updateAnchors(container);
        setShapeProperties(container, element);

        layoutPictogramElement(container);
        
        return container;
	}
	
	/**
	 * Creates anchors. If they already exist, then they are updated.
	 * @param shape
	 */
	protected void updateAnchors(final ContainerShape shape) {
		anchorUtil.createOrUpdateChopboxAnchor(shape, chopboxAnchorName);
	}
	
	@Override
	public final boolean update(final IUpdateContext context) {
		final PictogramElement pe = context.getPictogramElement();
		final Object bo = AadlElementWrapper.unwrap(getBusinessObjectForPictogramElement(pe));
		
		if(pe instanceof ContainerShape) {
			final ContainerShape shape = (ContainerShape)pe;		

			visibilityHelper.setIsGhost(shape, false);
			
			// Update/Recreate the child shapes and the graphics algorithm for the shape
			createGaAndInnerShapes(shape, bo, pe.getGraphicsAlgorithm().getX(), pe.getGraphicsAlgorithm().getY());
			
			updateAnchors(shape);
			
			setShapeProperties(shape, bo);
			
			// Layout the shape
			layoutPictogramElement(shape);
		}

		return true;
	}

	/**
	 * Called to set properties after the shape has been created/updated
	 * @param container
	 * @param bo
	 */
	protected void setShapeProperties(final ContainerShape shape, final Object bo) {}
	
	/**
	 * 
	 * @param container
	 * @param bo the unwrapped business object
	 * @param x
	 * @param y
	 */
	protected abstract void createGaAndInnerShapes(final ContainerShape shape, final Object bo, int x, int y);
}