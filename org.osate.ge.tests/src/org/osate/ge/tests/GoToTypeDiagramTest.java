package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class GoToTypeDiagramTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage();
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject();
	}

	@Test
	public void goToTypeDiagram() throws WidgetNotFoundException, ClassNotFoundException {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resize(editor, ElementNames.packageName, new Point(600, 600));

		bot.createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(25, 25));
		bot.waitUntilNewElementIsCreated(editor, AbstractTypeImpl.class);
		bot.renameElement(editor, ElementNames.abstractTypeName);
		bot.waitUntilElementExists(editor, ElementNames.abstractTypeName);

		bot.createImplementation(editor, ElementNames.packageName, ToolTypes.abstractImplementation,
				ElementNames.abstractTypeName, "impl", new Point(100, 100));

		bot.openDiagramFromContextMenu(editor,
				ElementNames.abstractTypeName + "." + "impl"/* ElementNames.abstractTypeName */,
				"Type Diagram");

		assertTrue(bot.isActiveEditor(ElementNames.packageName + "_" + ElementNames.abstractTypeName));
	}
}
