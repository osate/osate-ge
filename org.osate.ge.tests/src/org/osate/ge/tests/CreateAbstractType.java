package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class CreateAbstractType {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.createNewProjectAndPackage();
	}

	@After
	public void tearDown() {
		bot.deleteProject();
	}

	@Test
	public void renameClassifer() {
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(20, 20));
		bot.waitUntilNewElementIsCreated(editor, AbstractTypeImpl.class);
	}
}
