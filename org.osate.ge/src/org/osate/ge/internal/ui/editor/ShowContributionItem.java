package org.osate.ge.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.types.ContentsFilter;
import org.osate.ge.internal.diagram.runtime.types.DiagramType;
import org.osate.ge.internal.ui.util.SelectionUtil;
import org.osate.ge.internal.ui.util.UiUtil;

public class ShowContributionItem extends CompoundContributionItem {
	private final IContributionItem[] EMPTY = new IContributionItem[0];

	@Override
	protected IContributionItem[] getContributionItems() {
		final List<IContributionItem> contributions = new ArrayList<>();

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return EMPTY;
		}

		// TODO
//		// Don't allow execution if editor is not editable
//		final AgeDiagramEditor editor = graphitiService.getEditor();
//		if (editor != null && !editor.isEditable()) {
//			return false;
//		}
//
		final List<DiagramElement> diagramElements = SelectionUtil
				.getSelectedDiagramElements(window.getActivePage().getSelection());
		final AgeDiagram diagram = UiUtil.getDiagram(diagramElements);
		if(diagram == null) {
			return EMPTY;
		}

		final DiagramType dt = diagram.getConfiguration().getDiagramType();
		final List<ContentsFilter> applicableFilters = new ArrayList<>(
				dt.getApplicableAutoContentsFilters(diagramElements.get(0).getBusinessObject()));
		for(int i = 1; i < diagramElements.size(); i++) {
			applicableFilters
			.retainAll(dt.getApplicableAutoContentsFilters(diagramElements.get(i).getBusinessObject()));
		}

		// TODO: What about the "Hide Contents" menu option.... WOuld not be available with this config..

		// TODO: Should be inhandler?
		/*
		// Make the command executable if any of the elements have a different filter value
		for(final DiagramElement e : elements) {
			if(e.getAutoContentsFilter() != newFilterValue) {
				return true;
			}
		}
		 */
		// TODO: What about hide contents... Special behavior... Need special lable.

		// TODO: Select radio contribution

//		org.eclipse.ui.commands.radioStateParameter
//		https://wiki.eclipse.org/Menu_Contributions/Radio_Button_Command
// https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fui%2Fcommands%2FIElementUpdater.html

		// Create command contributions
		for (final ContentsFilter filter : applicableFilters) {
			contributions.add(new CommandContributionItem(new CommandContributionItemParameter(window, null,
					"org.osate.ge.setAutoContentFilter", Collections.singletonMap("contentsFilterId", filter.id()),
					null,
					null, null, filter.description(), null, null,
					CommandContributionItem.STYLE_PUSH, null, true)));
		}

		// TODO: Need a custom option... Should be "selected" if there are any custom settings

		return contributions.toArray(new IContributionItem[contributions.size()]);
	}
}
