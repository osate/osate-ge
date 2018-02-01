package org.osate.ge.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.filtering.ContentFilter;
import org.osate.ge.internal.services.ExtensionRegistryService;
import org.osate.ge.internal.ui.handlers.SetAutoContentsFilterHandler;
import org.osate.ge.internal.ui.util.SelectionUtil;
import org.osate.ge.internal.ui.util.UiUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ShowContributionItem extends CompoundContributionItem {
	private final IContributionItem[] EMPTY = new IContributionItem[0];

	@Override
	protected IContributionItem[] getContributionItems() {
		final List<IContributionItem> contributions = new ArrayList<>();

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return EMPTY;
		}

		final Bundle bundle = FrameworkUtil.getBundle(getClass());
		final ExtensionRegistryService extService = EclipseContextFactory.getServiceContext(bundle.getBundleContext())
				.get(ExtensionRegistryService.class);
		if(extService == null) {
			throw new RuntimeException("Unable to retrieve extension registry");
		}

		// Don't allow execution if editor is not editable
		// TODO
//		final AgeDiagramEditor editor = graphitiService.getEditor();
//		if (editor != null && !editor.isEditable()) {
//			return false;
//		}

		final List<DiagramElement> diagramElements = SelectionUtil
				.getSelectedDiagramElements(window.getActivePage().getSelection());
		final AgeDiagram diagram = UiUtil.getDiagram(diagramElements);
		if (diagram == null) {
			return EMPTY;
		}

		final List<ContentFilter> applicableFilters = new ArrayList<>();
		for (final ContentFilter contentFilter : extService.getContentFilters()) {
			for (final DiagramElement diagramElement : diagramElements) {
				if (contentFilter.isApplicable(diagramElement)) {
					applicableFilters.add(contentFilter);
					break;
				}
			}
		}
//
//		// TODO: What about the "Hide Contents" menu option.... WOuld not be available with this config..
//
//		// TODO: What about hide contents... Special behavior... Need special lable.
//
//		// TODO: Select radio contribution
//
////		org.eclipse.ui.commands.radioStateParameter
////		https://wiki.eclipse.org/Menu_Contributions/Radio_Button_Command
//// https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fui%2Fcommands%2FIElementUpdater.html
//
//		// Create command contributions

		// TODO: Sort
		for (final ContentFilter filter : applicableFilters) {
			// TODO: Rename command ID
			final CommandContributionItem commandItem = new CommandContributionItem(
					new CommandContributionItemParameter(window, null, "org.osate.ge.setAutoContentFilter",
							Collections.singletonMap(SetAutoContentsFilterHandler.PARAM_CONTENTS_FILTER_ID,
									filter.getId()),
							null, null, null,
							"Show " + filter.getName(), null, null, CommandContributionItem.STYLE_CHECK, null, true));
			contributions.add(commandItem);
		}

		return contributions.toArray(new IContributionItem[contributions.size()]);
	}
}
