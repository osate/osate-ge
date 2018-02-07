package org.osate.ge.internal.ui.handlers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.filtering.ContentFilter;
import org.osate.ge.internal.graphiti.AgeFeatureProvider;
import org.osate.ge.internal.services.ExtensionRegistryService;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.osate.ge.internal.ui.util.SelectionUtil;
import org.osate.ge.internal.ui.util.UiUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.ImmutableSet;

public class ToggleContentFilterHandler extends AbstractHandler implements IElementUpdater {
	public static final String PARAM_CONTENTS_FILTER_ID = "contentsFilterId";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (!(activeEditor instanceof AgeDiagramEditor)) {
			throw new RuntimeException("Unexpected editor: " + activeEditor);
		}

		// Get the extension registry
		final Bundle bundle = FrameworkUtil.getBundle(getClass());
		final ExtensionRegistryService extService = EclipseContextFactory.getServiceContext(bundle.getBundleContext())
				.get(ExtensionRegistryService.class);
		if(extService == null) {
			throw new RuntimeException("Unable to retrieve extension registry");
		}

		// Get editor and various services
		final AgeDiagramEditor diagramEditor = (AgeDiagramEditor) activeEditor;
		final AgeFeatureProvider featureProvider = (AgeFeatureProvider) diagramEditor.getDiagramTypeProvider()
				.getFeatureProvider();
		final List<DiagramElement> selectedDiagramElements = AgeHandlerUtil.getSelectedDiagramElements(event);
		final AgeDiagram diagram = UiUtil.getDiagram(selectedDiagramElements);
		if (diagram == null) {
			throw new RuntimeException("Unable to get diagram");
		}

		final String contentFilterId = (String) event.getParameters().get(PARAM_CONTENTS_FILTER_ID);
		if (contentFilterId == null) {
			throw new RuntimeException("Unable to get content filter");
		}

		final boolean addFilter = !isFilterEnabled(contentFilterId);

		final ContentFilter filter = extService.getContentFilterById(contentFilterId)
				.orElseThrow(() -> new RuntimeException("Unable to get content filter"));

		final String modLabel = (addFilter ? "Enable " : "Disable ") + "Show " + filter.getName();
		diagram.modify(modLabel, m -> {
			// Update the content filters of each element for which the content filter is applicable
			for (final DiagramElement e : selectedDiagramElements) {
				if (filter.isApplicable(e.getBusinessObject())) {
					final Set<ContentFilter> newContentFilters = new HashSet<>(e.getContentFilters());
					if (addFilter) {
						newContentFilters.add(filter);
					} else {
						newContentFilters.remove(filter);
					}
					m.setContentFilters(e, ImmutableSet.copyOf(newContentFilters));
				}
			}
		});

		// Update the diagram
		final IUpdateContext updateCtx = new UpdateContext(diagramEditor.getGraphitiAgeDiagram().getGraphitiDiagram());
		diagramEditor.getDiagramBehavior().executeFeature(featureProvider.getUpdateFeature(updateCtx), updateCtx);

		return null;
	}

	@Override
	public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
		final String contentFilterIdArg = (String) parameters.get(PARAM_CONTENTS_FILTER_ID);
		element.setChecked(isFilterEnabled(contentFilterIdArg));
	}

	private static final boolean isFilterEnabled(final String contentFilterId) {
		return getActiveContentsFilters().stream().anyMatch(cf -> cf.getId().equals(contentFilterId));
	}

	// Returns a set containing all the contents filters for the selected diagram elements
	private static final Set<ContentFilter> getActiveContentsFilters() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return Collections.emptySet();
		}

		final List<DiagramElement> diagramElements = SelectionUtil
				.getSelectedDiagramElements(window.getActivePage().getSelection());

		final Set<ContentFilter> allContentFilters = new HashSet<>();
		for (final DiagramElement de : diagramElements) {
			allContentFilters.addAll(de.getContentFilters());
		}

		return allContentFilters;
	}
}
