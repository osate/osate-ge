package org.osate.ge.internal.ui.handlers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.osate.ge.internal.diagram.runtime.DiagramModification;
import org.osate.ge.internal.diagram.runtime.types.ContentsFilter;
import org.osate.ge.internal.graphiti.AgeFeatureProvider;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.osate.ge.internal.ui.util.SelectionUtil;
import org.osate.ge.internal.ui.util.UiUtil;

public class SetAutoContentsFilterHandler extends AbstractHandler implements IElementUpdater {
	public final String PARAM_CONTENTS_FILTER_ID = "contentsFilterId";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (!(activeEditor instanceof AgeDiagramEditor)) {
			throw new RuntimeException("Unexpected editor: " + activeEditor);
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

		final ContentsFilter contentsFilter = diagram.getConfiguration().getDiagramType()
				.getContentsFilter((String) event.getParameters().get(PARAM_CONTENTS_FILTER_ID));
		if (contentsFilter == null) {
			throw new RuntimeException("Unable to get contents filter");
		}

		diagram.modify("Show " + contentsFilter.description(), m -> {
			// Hide children that do not pass the contents filter even if they have been set to manual.
			for (final DiagramElement e : selectedDiagramElements) {
				for (final DiagramElement child : e.getDiagramElements()) {
					if (!contentsFilter.test(child.getBusinessObject())) {
						m.setManual(child, false);
						setDescendantsAsAutomatic(m, child);
					}
				}
			}

			for (DiagramElement e : selectedDiagramElements) {
				m.setManual(e, true);
				m.setAutoContentsFilter(e, contentsFilter);
			}
		});

		// Update the diagram
		final IUpdateContext updateCtx = new UpdateContext(diagramEditor.getGraphitiAgeDiagram().getGraphitiDiagram());
		diagramEditor.getDiagramBehavior().executeFeature(featureProvider.getUpdateFeature(updateCtx), updateCtx);

		return null;
	}

	private void setDescendantsAsAutomatic(final DiagramModification m, final DiagramElement e) {
		// Set all descendants of the specified element as automatic/not manual
		for (final DiagramElement child : e.getDiagramElements()) {
			if (child.isManual()) {
				m.setManual(child, false);
			}
			setDescendantsAsAutomatic(m, child);
		}
	}

	// TODO: Remove if not using radio.
	@Override
	public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
		element.setChecked(getActiveContentsFilters().contains(parameters.get(PARAM_CONTENTS_FILTER_ID)));
	}

	// Returns a set containing the ids all the contents filters of the selected diagram elements
	private final Set<String> getActiveContentsFilters() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return Collections.emptySet();
		}

		final List<DiagramElement> diagramElements = SelectionUtil
				.getSelectedDiagramElements(window.getActivePage().getSelection());

		// TODO: Only add content filter if element is manual? Otherwise add the default?
		final Set<String> ids = new HashSet<>();

		for (final DiagramElement de : diagramElements) {
			if (de.isManual() && de.getAutoContentsFilter() != null) {
				ids.add(de.getAutoContentsFilter().id());
			}
		}

		return ids;
	}
}
