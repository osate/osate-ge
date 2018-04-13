package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractType;

public class OpenAssociatedDiagramTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(ElementNames.projectName, ElementNames.packageName);
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);

		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resizeEditPart(editor, new Point(600, 600), ElementNames.packageName);
		bot.createToolItemAndRename(editor, AbstractType.class, new Point(45, 45), ElementNames.abstractTypeName,
				ElementNames.packageName);
	}


	@After
	public void tearDown() {
		bot.deleteProject(ElementNames.projectName);
	}

	@Test
	public void openAssociatedDiagram() {
		bot.getEditor(ElementNames.packageName).select(ElementNames.abstractTypeName)
				.clickContextMenu(AgeGefBot.associatedDiagram);
		bot.clickButton("Yes");
		bot.clickButton("OK");

		Assert.assertTrue(bot.getEditor(ElementNames.packageName + "_" + ElementNames.abstractTypeName) != null);
	}
}
