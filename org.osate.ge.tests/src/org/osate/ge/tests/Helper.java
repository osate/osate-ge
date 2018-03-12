package org.osate.ge.tests;


import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.osate.aadl2.impl.NamedElementImpl;
import org.osate.ge.internal.graphiti.AgeFeatureProvider;

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
			bot.button("Open Perspective").click();
		}

		// Create AADL Package
		newMenu.click();
		bot.tree().getTreeItem("AADL").expand().getNode("AADL Package").click();
		bot.button("Next >").click();
		bot.text().setText(ElementNames.packageName);
		bot.radio("Graphical Editor").click();
		bot.button("Finish").click();
		bot.button("OK").click();

		// Close editor for open test
		bot.gefEditor(ElementNames.packageName + ".aadl_diagram").close();

		bot.tree().expandNode(new String[] { ElementNames.projectName }).getNode(ElementNames.packageName + ".aadl")
				.click();
		bot.tree().contextMenu("Open").click();
	}

	public void deleteProject() {
		bot.tree().select(ElementNames.projectName).contextMenu("Delete").click();
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
		bot.tree().expandNode(nodePath).getNode(fileName + ".aadl").click();
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
		final private AgeFeatureProvider ageFeatureProvider;
		final private CharSequence charSeq;
		final private Class<?> clazz;

		public NewElementMatcher(final SWTBotGefEditor editor, final CharSequence charSeq, final Class<?> clazz) {
			this.ageFeatureProvider = (AgeFeatureProvider) ((GraphitiShapeEditPart) editor.mainEditPart().part())
					.getFeatureProvider();
			this.charSeq = charSeq;
			this.clazz = clazz;
		}

		@Override
		public boolean matches(final Object item) {
			if (item instanceof GraphitiShapeEditPart) {
				final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) item;
				final Object element = ageFeatureProvider
						.getBusinessObjectForPictogramElement(gsep.getPictogramElement());
				if (element instanceof NamedElementImpl && element.getClass() == clazz && gsep.isActive()) {
					final NamedElementImpl namedElement = (NamedElementImpl) element;
					return namedElement.getName().contains(charSeq);
				}
			}

			return false;
		}

		@Override
		public void describeTo(Description description) {
		}
	}

	public void maximize(final SWTBotGefEditor editor) {
		editor.getWidget().getDisplay().asyncExec(() -> editor.getWidget().getDisplay().getActiveShell().setMaximized(true));
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
}
