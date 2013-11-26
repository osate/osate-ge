package edu.uah.rsesc.aadl.age.diagrams.componentImplementation;

import java.util.List;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.pattern.IConnectionPattern;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.Aadl2Package;

import edu.uah.rsesc.aadl.age.diagrams.common.AgeFeatureProvider;
import edu.uah.rsesc.aadl.age.diagrams.common.patterns.FlowSpecificationPattern;
import edu.uah.rsesc.aadl.age.diagrams.common.patterns.ModePattern;
import edu.uah.rsesc.aadl.age.diagrams.common.patterns.ModeTransitionPattern;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.features.ChangeSubcomponentTypeFeature;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.features.ComponentImplementationUpdateDiagramFeature;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.features.RefineSubcomponentFeature;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.features.RenameConnectionFeature;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.features.SetSubcomponentClassifierFeature;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.patterns.ComponentImplementationPattern;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.patterns.ConnectionPattern;
import edu.uah.rsesc.aadl.age.diagrams.componentImplementation.patterns.SubcomponentPattern;
import edu.uah.rsesc.aadl.age.services.BusinessObjectResolutionService;

public class ComponentImplementationFeatureProvider extends AgeFeatureProvider {
	public ComponentImplementationFeatureProvider(final IDiagramTypeProvider dtp) {
		super(dtp);
		this.addPattern(make(ComponentImplementationPattern.class));
		this.addPattern(make(SubcomponentPattern.class));
		this.addConnectionPattern(make(FlowSpecificationPattern.class));
		this.addPattern(make(ModePattern.class));
		this.addConnectionPattern(make(ModeTransitionPattern.class));
		this.addAadlFeaturePatterns();
		addAadlConnectionPatterns();
	}

	@Override
	public IUpdateFeature getUpdateFeature(IUpdateContext context) {
	   PictogramElement pictogramElement = context.getPictogramElement();
	   if(pictogramElement instanceof Diagram) {
		   return make(ComponentImplementationUpdateDiagramFeature.class);
	   }
	   return super.getUpdateFeature(context);
	}
	
	/**
	 * Method used to additively build a list of custom features. Subclasses can override to add additional custom features while including those supported by parent classes.
	 * @param features
	 */
	protected void addCustomFeatures(final List<ICustomFeature> features) {
		features.add(make(SetSubcomponentClassifierFeature.class));
		features.add(make(RefineSubcomponentFeature.class));
		
		for(final EClass subcomponentType : ChangeSubcomponentTypeFeature.getSubcomponentTypes()) {
			final IEclipseContext childCtx = getContext().createChild();
			childCtx.set("Subcomponent Type", subcomponentType);
			features.add(ContextInjectionFactory.make(ChangeSubcomponentTypeFeature.class, childCtx));	
		}
		super.addCustomFeatures(features);
	}
	
	private IConnectionPattern createConnectionPattern(final EClass connectionType) {
		final IEclipseContext childCtx = getContext().createChild();
		childCtx.set("Connection Type", connectionType);
		return ContextInjectionFactory.make(ConnectionPattern.class, childCtx);
	}
	
	/**
	 * Creates and adds patterns related to AADL Connections
	 */
	private void addAadlConnectionPatterns() {
		// Create the connection patterns
		final Aadl2Package p = Aadl2Factory.eINSTANCE.getAadl2Package();
		for(final EClass connectionType : ConnectionPattern.getConnectionTypes()) {
			addConnectionPattern(createConnectionPattern(connectionType));
		}
	}
	
	@Override
	protected IDirectEditingFeature getDirectEditingFeatureAdditional(final IDirectEditingContext context) {
		final BusinessObjectResolutionService bor = getContext().get(BusinessObjectResolutionService.class);
		if(bor != null) {
			if(bor.getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof org.osate.aadl2.Connection) {
				return make(RenameConnectionFeature.class);
			}
		}

		return super.getDirectEditingFeatureAdditional(context);
	}
}
