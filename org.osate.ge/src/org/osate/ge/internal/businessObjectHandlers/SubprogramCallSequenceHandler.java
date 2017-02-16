package org.osate.ge.internal.businessObjectHandlers;

import java.util.stream.Stream;
import javax.inject.Named;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.BehavioredImplementation;
import org.osate.aadl2.CallContext;
import org.osate.aadl2.CalledSubprogram;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramCallSequence;
import org.osate.ge.Categories;
import org.osate.ge.PaletteEntry;
import org.osate.ge.PaletteEntryBuilder;
import org.osate.ge.di.CanCreate;
import org.osate.ge.di.CanDelete;
import org.osate.ge.di.Create;
import org.osate.ge.di.GetChildren;
import org.osate.ge.di.GetGraphic;
import org.osate.ge.di.GetName;
import org.osate.ge.di.GetPaletteEntries;
import org.osate.ge.di.IsApplicable;
import org.osate.ge.di.Names;
import org.osate.ge.di.SetName;
import org.osate.ge.di.ValidateName;
import org.osate.ge.graphics.Graphic;
import org.osate.ge.graphics.RectangleBuilder;
import org.osate.ge.internal.DiagramElementProxy;
import org.osate.ge.internal.di.CanRename;
import org.osate.ge.internal.di.GetDefaultLabelConfiguration;
import org.osate.ge.internal.di.InternalNames;
import org.osate.ge.internal.labels.LabelConfiguration;
import org.osate.ge.internal.labels.LabelConfigurationBuilder;
import org.osate.ge.internal.model.SubprogramCallOrder;
import org.osate.ge.internal.query.StandaloneDiagramElementQuery;
import org.osate.ge.internal.services.AadlFeatureService;
import org.osate.ge.internal.services.NamingService;
import org.osate.ge.internal.services.QueryService;
import org.osate.ge.internal.services.RefactoringService;
import org.osate.ge.internal.ui.dialogs.DefaultSelectSubprogramDialogModel;
import org.osate.ge.internal.ui.dialogs.SelectSubprogramDialog;
import org.osate.ge.internal.util.AadlHelper;
import org.osate.ge.internal.util.ImageHelper;

public class SubprogramCallSequenceHandler {
	private static final StandaloneDiagramElementQuery componentImplementationQuery = StandaloneDiagramElementQuery.create((root) -> root.ancestors().filter((fa) -> fa.getBusinessObject() instanceof ComponentImplementation).first());
	private Graphic graphic = RectangleBuilder.create().dashed().build();
	private LabelConfiguration nameLabelConfiguration = LabelConfigurationBuilder.create().top().horizontalCenter().build();
		
	@IsApplicable
	public boolean isApplicable(final @Named(Names.BUSINESS_OBJECT) SubprogramCallSequence cs) {
		return true;
	}
	
	@GetPaletteEntries
	public PaletteEntry[] getPaletteEntries(final @Named(Names.DIAGRAM_BO) ComponentClassifier classifier) {				
		return new PaletteEntry[] {
			PaletteEntryBuilder.create().label("Subprogram Call Sequence").icon(ImageHelper.getImage(Aadl2Factory.eINSTANCE.getAadl2Package().getSubprogramCallSequence())).category(Categories.SUBPROGRAM_CALLS).build()
		};
	}
	
	@GetGraphic
	public Graphic getGraphicalRepresentation(final @Named(Names.BUSINESS_OBJECT) SubprogramCallSequence cs) {
		return graphic;
	}
	
	@GetName
	public String getName(final @Named(Names.BUSINESS_OBJECT) SubprogramCallSequence cs) {
		return cs.getName();
	}
	
	@GetDefaultLabelConfiguration
	public LabelConfiguration getNameLabelConfiguration() {
		return nameLabelConfiguration;
	}
	
	@CanRename
	@CanDelete
    public boolean canEdit(final @Named(Names.BUSINESS_OBJECT) SubprogramCallSequence cs, final @Named(InternalNames.DIAGRAM_ELEMENT_PROXY) DiagramElementProxy diagramElement, final QueryService queryService) {
		return cs.getContainingClassifier() == getComponentImplementation(diagramElement, queryService);
    }
    
	private ComponentImplementation getComponentImplementation(final DiagramElementProxy csDiagramElement, final QueryService queryService) {
		return (ComponentImplementation)queryService.getFirstBusinessObject(componentImplementationQuery, csDiagramElement);
	}
	
	@ValidateName
    public String validateName(final @Named(Names.BUSINESS_OBJECT) SubprogramCallSequence cs, final @Named(Names.NAME) String value, final NamingService namingService) {
    	return namingService.checkNameValidity(cs, value);
    }	
	
	@SetName
	public void setName(final @Named(Names.BUSINESS_OBJECT) SubprogramCallSequence cs, final @Named(Names.NAME) String value, final RefactoringService refactoringService) {
		refactoringService.renameElement(cs, value);
	}
	
	@CanCreate
	public boolean canCreate(final @Named(Names.TARGET_BO) BehavioredImplementation bi) {		
		return true;
	}
	
	@Create
	public SubprogramCallSequence createBusinessObject(@Named(Names.OWNER_BO) BehavioredImplementation bi, final AadlFeatureService featureService, final NamingService namingService) {
		final DefaultSelectSubprogramDialogModel subprogramSelectionModel = new DefaultSelectSubprogramDialogModel(featureService, bi);
		final SelectSubprogramDialog dlg = new SelectSubprogramDialog(Display.getCurrent().getActiveShell(), subprogramSelectionModel);
		if(dlg.open() == Dialog.CANCEL) {
			return null;
		}
		
		// Get the CallContext and Called Subprogram
		final CallContext callContext = subprogramSelectionModel.getCallContext(dlg.getSelectedContext());		
		final CalledSubprogram calledSubprogram = subprogramSelectionModel.getCalledSubprogram(dlg.getSelectedSubprogram());
	
		// Get the classifier
		final String newScsName = namingService.buildUniqueIdentifier(bi, "new_call_sequence");
		final String initialSubprogramCallName = namingService.buildUniqueIdentifier(bi, "new_call");
		
		final SubprogramCallSequence newScs = bi.createOwnedSubprogramCallSequence();
		newScs.setName(newScsName);

		// Create an initial call. Needed because call sequences must have at least one call
		final SubprogramCall initialSubprogramCall = newScs.createOwnedSubprogramCall();
		initialSubprogramCall.setName(initialSubprogramCallName);	 			
		initialSubprogramCall.setContext(callContext);
		initialSubprogramCall.setCalledSubprogram(calledSubprogram);

		AadlHelper.ensurePackageIsImported(bi, callContext);
		AadlHelper.ensurePackageIsImported(bi, calledSubprogram);
			
		return newScs;
	}
	
	@GetChildren
	public Stream<?> getChildren(final @Named(Names.BUSINESS_OBJECT) SubprogramCallSequence cs) {
		return Stream.concat(cs.getOwnedSubprogramCalls().stream(), SubprogramCallOrder.getSubprogramCallOrders(cs.getOwnedSubprogramCalls()).stream());
	}	
}