package org.osate.ge.internal.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class OpenAssociatedDiagramHandler extends AbstractHandler {
//	@IsAvailable
//	public boolean isAvailable(@Named(Names.BUSINESS_OBJECT) final Object bo,
//			@Named(Names.BUSINESS_OBJECT_CONTEXT) final BusinessObjectContext boc) {
//		return bo instanceof Classifier ||
//				(bo instanceof Subcomponent && AadlSubcomponentUtil.getComponentClassifier(boc, (Subcomponent)bo) != null);
//	}
//
//	@Activate
//	public void activate(@Named(Names.BUSINESS_OBJECT) final Object bo,
//			@Named(Names.BUSINESS_OBJECT_CONTEXT) final BusinessObjectContext boc,
//			final DiagramService diagramService) {
//		if(bo instanceof Classifier) {
//			diagramService.openOrCreateDiagramForBusinessObject(bo);
//		} else if(bo instanceof Subcomponent) {
//			final ComponentClassifier cc = AadlSubcomponentUtil.getComponentClassifier(boc, (Subcomponent) bo);
//			if (cc != null) {
//				diagramService.openOrCreateDiagramForBusinessObject(cc);
//			}
//		}
//	}

	@Override
	public void setEnabled(final Object evaluationContext) {
//		boolean enabled = false;
//		final List<DiagramElement> selectedDiagramElements = AgeHandlerUtil
//				.getSelectedDiagramElementsFromContext(evaluationContext);
//		if (selectedDiagramElements.size() == 1) {
//			final Object selectedBo = selectedDiagramElements.get(0).getBusinessObject();
//			final EObject boEObj = getEObject(selectedBo);
//			if (boEObj != null) {
//				final URI uri = EcoreUtil.getURI(boEObj);
//				if (uri != null && boEObj.eResource() instanceof XtextResource) {
//					final XtextResource res = (XtextResource) boEObj.eResource();
//					if (res.getResourceServiceProvider().get(GlobalURIEditorOpener.class) != null) {
//						enabled = true;
//					}
//				}
//			}
//		}
//
//		setBaseEnabled(enabled);
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
//		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
//		if (!(activeEditor instanceof AgeDiagramEditor)) {
//			throw new RuntimeException("Unexpected editor: " + activeEditor);
//		}
//
//		// Get diagram and selected elements
//		final List<DiagramElement> selectedDiagramElements = AgeHandlerUtil.getSelectedDiagramElements(event);
//		if(selectedDiagramElements.size() == 0) {
//			throw new RuntimeException("No element selected");
//		}
//
//		final Object selectedBo = selectedDiagramElements.get(0).getBusinessObject();
//		final EObject boEObj = getEObject(selectedBo);
//		if (boEObj == null) {
//			throw new RuntimeException("Unsupported type: " + selectedBo);
//		}
//
//		final URI uri = Objects.requireNonNull(EcoreUtil.getURI(boEObj), "Unable to get URI for business object");
//		if (!(boEObj.eResource() instanceof XtextResource)) {
//			throw new RuntimeException("The resource of the loaded business object resource is not an XtextResource");
//		}
//
//		final XtextResource res = (XtextResource) boEObj.eResource();
//		final GlobalURIEditorOpener opener = Objects.requireNonNull(
//				(GlobalURIEditorOpener) res.getResourceServiceProvider().get(GlobalURIEditorOpener.class),
//				"unable to get global URI Editor opener");
//		opener.open(uri, true);

		return null;
	}
}
