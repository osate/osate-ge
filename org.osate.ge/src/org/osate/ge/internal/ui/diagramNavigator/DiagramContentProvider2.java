package org.osate.ge.internal.ui.diagramNavigator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.osate.ge.internal.services.DiagramService;
import org.osate.ge.internal.services.DiagramService.DiagramReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

// TODO: Rename
public class DiagramContentProvider2 implements ICommonContentProvider {
	private DiagramService diagramService;

	public DiagramContentProvider2() {
	}

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
		System.err.println("B : " + inputElement);
		return null;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		System.err.println("GET CHILDREN: " + parentElement);

		if (parentElement instanceof IProject) {
			final IProject project = (IProject) parentElement;
			final List<? extends DiagramReference> projectDiagrams = diagramService
					.findDiagrams(Collections.singleton(project));

			// TODO: Should builder() take arguments or should it be methods on the builder... Would be required methods though..
			final DiagramGroup projectGroup = DiagramGroup.builder(projectDiagrams, project).build();
			return getChildren(projectGroup);
		} else if (parentElement instanceof DiagramGroup) {
			final DiagramGroup parentGroup = (DiagramGroup) parentElement;

			if (isGroupByDiagramTypesEnabled() && parentGroup.getDiagramTypeId() == null) {
				return parentGroup.findMatchingDiagramReferences().map(d -> d.getDiagramTypeId()).distinct()
						.map(diagramTypeId -> DiagramGroup.builder(parentGroup).diagramType(diagramTypeId).build())
						.toArray();
			} else if (isGroupByContextEnabled() && !parentGroup.isContextReferenceValid()) {
				return parentGroup.findMatchingDiagramReferences().map(d -> d.getContextReference()).distinct()
						.map(contextRef -> DiagramGroup.builder(parentGroup).contextReference(contextRef).build())
						.toArray();
			} else {
				return parentGroup.findMatchingDiagramReferences().map(dr -> dr.getFile()).toArray();
			}
		}

		// TODO: Adjust behavior based on user settings
		// TODO: Need a quick way to convert from file to the diagram reference?

		// TODO: Can return null?
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
		System.err.println("HC2 : " + element);
		return true;
	}

	// TODO: Rename
	private final boolean isGroupByDiagramTypesEnabled() {
		return true;
	}

	private final boolean isGroupByContextEnabled() {
		return true;
	}
}
