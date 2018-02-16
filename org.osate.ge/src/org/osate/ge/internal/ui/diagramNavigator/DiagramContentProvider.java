package org.osate.ge.internal.ui.diagramNavigator;

import java.util.Set;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

public class DiagramContentProvider implements IPipelinedTreeContentProvider2 {
	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		System.err.println("INIT");
	}

	@Override
	public void restoreState(IMemento aMemento) {
	}

	@Override
	public void saveState(IMemento aMemento) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return null;
	}

	// TODO: Needed for pipeline?
	@Override
	public Object[] getChildren(final Object parentElement) {
		return new Object[0];
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		System.err.println("HC : " + element);
		return false;
	}

	@Override
	public boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren) {
		return true;// false;
	}

	@Override
	public void getPipelinedChildren(Object aParent, @SuppressWarnings("rawtypes") Set theCurrentChildren) {
		// Clear children.
		// TODO: Would a filter be better?
		theCurrentChildren.clear();
	}

	@Override
	public void getPipelinedElements(Object anInput, @SuppressWarnings("rawtypes") Set theCurrentElements) {
	}

	@Override
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
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
