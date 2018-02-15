package org.osate.ge.internal.ui.diagramNavigator;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.osate.ge.internal.services.DiagramService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class DiagramContentProvider implements IPipelinedTreeContentProvider2 {
	private DiagramService diagramService;

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		final Bundle bundle = FrameworkUtil.getBundle(getClass());
		final IEclipseContext context = EclipseContextFactory.getServiceContext(bundle.getBundleContext())
				.createChild();
		diagramService = Objects.requireNonNull(context.get(DiagramService.class), "Unable to get diagram service");
	}

	@Override
	public void restoreState(IMemento aMemento) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveState(IMemento aMemento) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(final Object inputElement) {
		System.err.println("GET ELEMENT: " + inputElement);
		return null;
	}

	// TODO: Needed for pipeline
	@Override
	public Object[] getChildren(final Object parentElement) {
		System.err.println("GET CHILDREN: " + parentElement);
		// return null;
		return new Object[0];
	}

	@Override
	public Object getParent(final Object element) {
		System.err.println("GET PARENT: " + element);
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (!(element instanceof IFile)) {
			return false;
		}
		System.err.println("HAS CHILDREN: " + element);
		return true;
	}

	@Override
	public boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren) {
		if (anInput instanceof IFile) {
			System.err.println("A: " + anInput);
			return false;
		}
		System.err.println("B: " + anInput);
		return currentHasChildren;
	}

	@Override
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		// TODO Auto-generated method stub
		theCurrentChildren.clear();

		if (aParent instanceof IProject) {
			// TODO: Adjust behavior based on user settings
			// TODO: Need a quick way to convert from file to the diagram reference?
			diagramService.findDiagrams(Collections.singleton(((IProject) aParent))).stream().map(dr -> dr.getFile())
			.forEach(theCurrentChildren::add);
		}
	}

	@Override
	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
	}

	@Override
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		// TODO
		return aSuggestedParent;
	}

	@Override
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
		return anAddModification;
	}

	@Override
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
		return aRemoveModification;
	}

	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		// TODO Auto-generated method stub
		return false;
	}
}
