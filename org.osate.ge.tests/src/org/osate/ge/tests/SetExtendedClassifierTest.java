package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SetExtendedClassifierTest {
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
	public void setExtendedClassifier() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.maximize();
		bot.resize(editor, ElementNames.packageName, new Point(600, 600));

		bot.createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(40, 40));
		bot.renameElement(editor, ElementNames.abstractTypeName);

		bot.createImplementation(editor, ElementNames.packageName, ToolTypes.abstractImplementation,
				ElementNames.abstractTypeName, ElementNames.abstractTypeName, new Point(350, 350));
		bot.createImplementation(editor, ElementNames.packageName, ToolTypes.abstractImplementation,
				ElementNames.abstractTypeName, ElementNames.abstractTypeName + 2, new Point(150, 150));

		final String implName = ElementNames.abstractTypeName + "." + ElementNames.abstractTypeName + 2;
		bot.setElementOptionButtonInPropertiesView(editor, implName,
				"Properties", "AADL", "Choose...");
		bot.clickButton("OK");
	}
}
