package org.osate.ge.tests;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractType;
import org.osate.aadl2.impl.AbstractImplementationImpl;

public class OpenAADLInstanceModelTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.createNewProjectAndPackage(ElementNames.projectName, ElementNames.packageName);
		ageBot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
	}

	@After
	public void tearDown() {
		helper.deleteProject();
	}

	@Test
	public void openAADLInstanceModel() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);

		bot.createToolItem(editor, new Point(15, 15), ToolTypes.getToolItem(AbstractType.class));
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15));

		helper.createToolItem(editor, new Point(100, 100), ToolTypes.getToolItem(clazz));
		bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive("Select a Classifier"));
		final SWTBotShell shell = bot.activeShell();
		bot.button("OK").click();
		bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses(shell));
		bot.waitUntil(new ICondition() {
			@Override
			public boolean test() throws Exception {
				final List<SWTBotGefEditPart> list = editor.getSWTBotGefViewer().editParts(new Helper.NewElementMatcher<EditPart>(editor, ElementNames.abstractTypeName + ".impl", AbstractImplementationImpl.class));
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

		editor.select(ElementNames.abstractTypeName + ".impl").clickContextMenu("Instantiate");

		final String[] nodePath = new String[] { ElementNames.projectName, "instances" };
		// Expand tree path and select the aadl file to open
		final SWTBotTreeItem aadlPackage = bot.tree().expandNode(nodePath).getNode(0).click();
		// Open Diagram
		aadlPackage.contextMenu("Open Diagram").click();
	}
}
