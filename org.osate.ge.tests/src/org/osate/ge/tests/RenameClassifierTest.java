package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractType;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class RenameClassifierTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(ElementNames.projectName, ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject(ElementNames.projectName);
	}

	@Test
	public void renameClassifer() {
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.createToolItem(editor, ToolTypes.getToolItem(AbstractType.class), new Point(20, 20),
				ElementNames.packageName);
		bot.waitUntilNewElementIsCreated(editor, AbstractTypeImpl.class);

		final String newName = ElementNames.abstractTypeName;
		bot.renameElement(editor, newName);

		Assert.assertTrue(editor.getEditPart(newName) != null);
	}
}
