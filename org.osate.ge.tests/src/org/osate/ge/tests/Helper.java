package org.osate.ge.tests;


import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.osate.aadl2.impl.ModeTransitionTriggerImpl;
import org.osate.aadl2.impl.NamedElementImpl;
import org.osate.ge.internal.services.BusinessObjectResolutionService;
import org.osate.ge.internal.services.impl.DefaultBusinessObjectResolutionService;

public class Helper {
	final SWTGefBot bot;

	public Helper(final SWTGefBot bot) {
		this.bot = bot;
	}

	public void createNewProjectAndPackage() {
		closeWelcomePage();
		final SWTBotMenu newMenu = bot.menu("Other...", true).click();
		bot.tree().getTreeItem("AADL").expand().getNode("AADL Project").click();
		bot.button("Next >").click();
		bot.text().setText(ElementNames.projectName);
		bot.button("Finish").click();

		if(!bot.activePerspective().getLabel().equals("AADL")) {
			// Open AADL Perspective Dialog
			bot.button("Yes").click();
		}

		// Create AADL Package
		newMenu.click();
		bot.tree().getTreeItem("AADL").expand().getNode("AADL Package (Graphical)").click();
		bot.button("Next >").click();
		bot.text().setText(ElementNames.packageName);
		bot.button("Finish").click();

		// Close editor for open test
		bot.gefEditor(ElementNames.packageName).close();

		bot.tree().expandNode(new String[] { ElementNames.projectName, "packages" }).getNode(ElementNames.packageName + ".aadl").click();
		bot.tree().contextMenu("Open").click();
	}

	public void deleteProject() {
		bot.tree().select(0).contextMenu("Delete").click();
		bot.checkBox("Delete project contents on disk (cannot be undone)").click();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(bot.activeShell()));
		bot.saveAllEditors();
		bot.closeAllEditors();
		bot.perspectiveByLabel("Resource").activate();
	}

	public void createToolItem(final SWTBotGefEditor editor, final String toolItem, final Point p) {
		editor.activateTool(toolItem);
		editor.click(p.x, p.y);
		bot.sleep(2000);
	}

	public void openDiagram(final String[] nodePath, final String fileName) {
		bot.tree().expandNode(nodePath).getNode(fileName).click();
		bot.tree().contextMenu("Open Diagram").click();
	}

	public void closeWelcomePage() {
		for(final SWTBotView view : bot.views()) {
			if (view.getTitle().equals("Welcome")) {
				view.close();
			}
		}
	}

	public static class NewElementMatcher<T> extends BaseMatcher<T> {
		final private BusinessObjectResolutionService bor;
		final private CharSequence charSeq;
		final private Class<?> clazz;

		public NewElementMatcher(final SWTBotGefEditor editor, final CharSequence charSeq, final Class<?> clazz) {
			this.bor = new DefaultBusinessObjectResolutionService(((GraphitiShapeEditPart)editor.mainEditPart().part()).getFeatureProvider());
			this.charSeq = charSeq;
			this.clazz = clazz;
		}

		@Override
		public boolean matches(final Object item) {
			if(item instanceof GraphitiShapeEditPart) {
				final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)item;
				
				final Object element = bor.getBusinessObjectForPictogramElement(gsep.getPictogramElement());
				if(element.getClass() == clazz && element instanceof NamedElementImpl && gsep.isActive()) {
					final NamedElementImpl namedElement = (NamedElementImpl)element;
					return namedElement.getName().contains(charSeq);
				}
			}
			
			return false;
		}

		@Override
		public void describeTo(Description description) {}
	}

	public void maximize(final SWTBotGefEditor editor) {
		editor.getWidget().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				editor.getWidget().getDisplay().getActiveShell().setMaximized(true);
			}
		});
	}

	public void waitForNew(final SWTBotGefEditor editor, final CharSequence charSeq, final Class<?> clazz) {
		editor.bot().waitUntil(new ICondition() {
			@Override
			public boolean test() throws Exception {
				final List<SWTBotGefEditPart> list = editor.getSWTBotGefViewer().editParts(new Helper.NewElementMatcher<EditPart>(editor, charSeq, clazz));
				return !list.isEmpty();
			}

			@Override
			public void init(SWTBot bot) {
			}

			@Override

			public String getFailureMessage() {
				return "element not created";
			}
		});
	}

	/*	final static ICondition condition = new ICondition() {
		@Override
		public boolean test() throws Exception {
			final List<SWTBotGefEditPart> list = editor4.getSWTBotGefViewer().editParts(new Helper.NewElementMatcher<EditPart>(editor, "new_", AbstractSubcomponentImpl.class));
			return !list.isEmpty();
		}

		@Override
		public void init(SWTBot bot) {
		}

		@Override

		public String getFailureMessage() {
			return "element not created";
		}
	};*/
}
