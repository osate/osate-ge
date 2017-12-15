package org.osate.ge.internal.graphiti.features;

import java.lang.annotation.Annotation;
import java.util.Objects;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.di.Activate;
import org.osate.ge.di.CanActivate;
import org.osate.ge.di.GetLabel;
import org.osate.ge.di.HasDoneChanges;
import org.osate.ge.di.IsAvailable;
import org.osate.ge.di.Names;
import org.osate.ge.internal.di.GetBusinessObjectToModify;
import org.osate.ge.internal.graphiti.GraphitiAgeDiagramProvider;
import org.osate.ge.internal.graphiti.diagram.GraphitiAgeDiagram;
import org.osate.ge.internal.services.AadlModificationService;
import org.osate.ge.internal.services.ExtensionService;
import org.osate.ge.internal.util.AnnotationUtil;

/**
 * Custom feature for wrapping AADL Graphical Editor commands registered with the command service as a graphiti custom feature.
 *
 */
public class CommandCustomFeature extends AbstractCustomFeature {
	private final Object cmd;
	private final ExtensionService extService;
	private final AadlModificationService aadlModificationService;
	private final GraphitiAgeDiagramProvider graphitiAgeDiagramProvider;
	private boolean hasMadeChanges = false;

	public CommandCustomFeature(final Object cmd,
			final ExtensionService extService,
			final AadlModificationService aadlModificationService,
			final GraphitiAgeDiagramProvider graphitiAgeDiagramProvider,
			final IFeatureProvider fp) {
		super(fp);
		this.extService = Objects.requireNonNull(extService, "extService must not be null");
		this.cmd = Objects.requireNonNull(cmd, "cmd must not be null");
		this.aadlModificationService = Objects.requireNonNull(aadlModificationService, "aadlModificationService must not be null");
		this.graphitiAgeDiagramProvider = Objects.requireNonNull(graphitiAgeDiagramProvider, "graphitiAgeDiagramProvider must not be null");
	}

	public Object getCommand() {
		return cmd;
	}

	@Override
	public String getName() {
		final IEclipseContext eclipseContext = extService.createChildContext();
		try {
			return (String)ContextInjectionFactory.invoke(cmd, GetLabel.class, eclipseContext);
		} finally {
			eclipseContext.dispose();
		}
	}

	@Override
	public boolean isAvailable(final IContext context) {
		// Return true if the command doesn't have a method with the annotation
		if(!hasMethodWithAnnotation(IsAvailable.class)) {
			return true;
		}

		final ICustomContext customCtx = (ICustomContext)context;
		final IEclipseContext eclipseContext = extService.createChildContext();
		try {
			final BusinessObjectContext bocs[] = getBusinessObjectContexts(customCtx.getPictogramElements());
			if(bocs == null) {
				return false;
			}

			final Object[] businessObjects = getBusinessObjects(bocs);
			if(businessObjects == null){
				return false;
			}

			populateEclipseContext(eclipseContext, bocs, businessObjects, null);
			return (Boolean)ContextInjectionFactory.invoke(cmd, IsAvailable.class, eclipseContext, Boolean.FALSE);
		} finally {
			eclipseContext.dispose();
		}
	}

	private BusinessObjectContext[] getBusinessObjectContexts(final PictogramElement[] pes) {
		return getBusinessObjectContexts(graphitiAgeDiagramProvider.getGraphitiAgeDiagram(), pes);
	}

	/**
	 * Returns null if it is unable get the business object context for any pictogram element
	 * @param pes
	 * @return
	 */
	private static BusinessObjectContext[] getBusinessObjectContexts(final GraphitiAgeDiagram graphitiAgeDiagram, final PictogramElement[] pes) {
		final BusinessObjectContext[] bocs = new BusinessObjectContext[pes.length];
		for(int i = 0; i < pes.length; i++) {
			final BusinessObjectContext boc = graphitiAgeDiagram.getClosestDiagramNode(pes[i]);

			// Return null if we are unable to get the BOC for any passed in pictogram element
			if(boc == null) {
				return null;
			}

			bocs[i] = boc;
		}

		return bocs;
	}

	@Override
	public boolean canExecute(final ICustomContext context) {
		// Return true if the command doesn't have a method with the annotation
		if(!hasMethodWithAnnotation(CanActivate.class)) {
			return true;
		}

		final BusinessObjectContext bocs[] = getBusinessObjectContexts(context.getPictogramElements());
		final Object[] businessObjects = getBusinessObjects(bocs);

		final IEclipseContext eclipseContext = extService.createChildContext();
		try {
			populateEclipseContext(eclipseContext, bocs, businessObjects, null);
			return (boolean)ContextInjectionFactory.invoke(cmd, CanActivate.class, eclipseContext, Boolean.FALSE);
		} finally {
			eclipseContext.dispose();
		}
	}

	@Override
	public void execute(final ICustomContext context) {
		final BusinessObjectContext bocs[] = getBusinessObjectContexts(context.getPictogramElements());
		final Object[] businessObjects = getBusinessObjects(bocs);
		final EObject objectToModify = getObjectToModify(bocs, businessObjects);
		if(objectToModify  != null) {
			final Object bo = businessObjects[0];
			if(!(bo instanceof EObject)) {
				throw new RuntimeException("Only EObject instances may be modified");
			}

			aadlModificationService.modify(objectToModify, (resource, liveObjectToModify) -> {
				final Object liveBo;
				if (bo instanceof EObject) {
					final EObject a = (EObject) bo;
					liveBo = resource.getResourceSet().getEObject(EcoreUtil.getURI(a), true);
				} else {
					liveBo = null;
				}

				final Object[] liveBos = liveBo == null ? null : new Object[] { liveBo };

				// Business Object Contexts are not supplied to commands which modify the model. Commands which modify the diagram must use the supplied BO's
				// which are guaranteed to be in the appropriate resource.
				hasMadeChanges = activate(null, liveBos, true, liveObjectToModify);
				return null;
			});
		} else {
			hasMadeChanges = activate(bocs, businessObjects, false, null);
		}
	}

	private EObject getObjectToModify(final BusinessObjectContext[] bocs, final Object[] businessObjects) {
		final IEclipseContext eclipseContext = extService.createChildContext();
		try {
			populateEclipseContext(eclipseContext, bocs, businessObjects, null);
			final Object obj = ContextInjectionFactory.invoke(cmd, GetBusinessObjectToModify.class, eclipseContext, null);
			if(obj != null && !(obj instanceof EObject)) {
				throw new RuntimeException("Only EObject instances may be modified by commands");
			}

			return (EObject)obj;
		} finally {
			eclipseContext.dispose();
		}
	}

	private boolean activate(final BusinessObjectContext[] bocs, final Object[] businessObjects,
			final boolean modifiesBusinessObjects, final EObject boToModify) {
		final IEclipseContext eclipseContext = extService.createChildContext();
		try {
			populateEclipseContext(eclipseContext, bocs, businessObjects, boToModify);
			Boolean result = (Boolean)ContextInjectionFactory.invoke(cmd, Activate.class, eclipseContext, modifiesBusinessObjects);
			if(result == null) {
				result = modifiesBusinessObjects;
			}
			return result;
		} finally {
			eclipseContext.dispose();
		}
	}

	@Override
	public boolean canUndo(final IContext context) {
		return false;
	}

	@Override
	public boolean hasDoneChanges() {
		final IEclipseContext eclipseContext = extService.createChildContext();
		try {
			return (Boolean)ContextInjectionFactory.invoke(cmd, HasDoneChanges.class, eclipseContext, hasMadeChanges);
		} finally {
			eclipseContext.dispose();
		}
	}

	private static void populateEclipseContext(final IEclipseContext context, final BusinessObjectContext[] bocs,
			final Object[] businessObjects, final EObject boToModify) {
		// Diagram Elements
		if(bocs != null && bocs.length > 0) {
			if(bocs.length == 1) {
				context.set(Names.BUSINESS_OBJECT_CONTEXT, bocs[0]);
			}

			context.set(Names.BUSINESS_OBJECT_CONTEXTS, bocs);
		}

		// Business Objects
		if(businessObjects != null && businessObjects.length > 0) {
			if(businessObjects.length == 1) {
				context.set(Names.BUSINESS_OBJECT, businessObjects[0]);
			}
			context.set(Names.BUSINESS_OBJECTS, businessObjects);
		}

		// Business Object to Modify
		if (boToModify != null) {
			context.set(Names.MODIFY_BO, boToModify);
		}
	}

	private Object[] getBusinessObjects(final BusinessObjectContext[] bocs) {
		if(bocs == null) {
			return null;
		}

		final Object[] bos = new Object[bocs.length];
		for(int i = 0; i < bocs.length; i++) {
			bos[i] = bocs[i].getBusinessObject();

			if(bos[i] == null) {
				return null;
			}

			if (bos[i] instanceof EObject && ((EObject) bos[i]).eIsProxy()) {
				return null;
			}
		}

		return bos;
	}

	private boolean hasMethodWithAnnotation(final Class<? extends Annotation> annotationClass) {
		return AnnotationUtil.hasMethodWithAnnotation(annotationClass, cmd);
	}
}
