package org.osate.ge.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.filtering.ContentFilter;
import org.osate.ge.internal.services.ExtensionRegistryService;
import org.osate.ge.internal.ui.handlers.ToggleContentFilterHandler;
import org.osate.ge.internal.ui.util.SelectionUtil;
import org.osate.ge.internal.ui.util.UiUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ToggleFiltersContributionItem extends CompoundContributionItem {
	private final IContributionItem[] EMPTY = new IContributionItem[0];

	@Override
	protected IContributionItem[] getContributionItems() {
		final List<IContributionItem> contributions = new ArrayList<>();

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return EMPTY;
		}

		if (window.getActivePage() == null) {
			return EMPTY;
		}

		final IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		if(!(activeEditor instanceof AgeDiagramEditor)) {
			return EMPTY;
		}

		// Don't contribute commands if editor is not editable
		final AgeDiagramEditor editor = (AgeDiagramEditor)activeEditor;
		if(activeEditor == null || !editor.isEditable()) {
			return EMPTY;
		}

		final Bundle bundle = FrameworkUtil.getBundle(getClass());
		final ExtensionRegistryService extRegistry = Objects.requireNonNull(
				EclipseContextFactory.getServiceContext(bundle.getBundleContext()).get(ExtensionRegistryService.class),
				"Unable to retrieve extension registry");

		final List<DiagramElement> diagramElements = SelectionUtil
				.getSelectedDiagramElements(window.getActivePage().getSelection());
		final AgeDiagram diagram = UiUtil.getDiagram(diagramElements);
		if (diagram == null) {
			return EMPTY;
		}

		final List<ContentFilter> applicableFilters = new ArrayList<>();
		for (final ContentFilter contentFilter : extRegistry.getContentFilters()) {
			for (final DiagramElement diagramElement : diagramElements) {
				if (contentFilter.isApplicable(diagramElement.getBusinessObject())) {
					applicableFilters.add(contentFilter);
					break;
				}
			}
		}

		// Create command contributions
		applicableFilters.stream().sorted((cf1, cf2) -> cf1.getName().compareToIgnoreCase(cf2.getName()))
		.forEachOrdered(filter -> {
			final CommandContributionItem commandItem = new CommandContributionItem(
					new CommandContributionItemParameter(window, null, "org.osate.ge.toggleContentFilter",
							Collections.singletonMap(ToggleContentFilterHandler.PARAM_CONTENTS_FILTER_ID,
									filter.getId()),
							null, null, null,
							"Show " + filter.getName(), null, null, CommandContributionItem.STYLE_CHECK, null,
							true));
			contributions.add(commandItem);
		});

		return contributions.toArray(new IContributionItem[contributions.size()]);
	}
}
