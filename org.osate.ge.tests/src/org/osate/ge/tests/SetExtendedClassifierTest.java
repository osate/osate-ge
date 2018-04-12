package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractImplementation;
import org.osate.aadl2.AbstractType;

public class SetExtendedClassifierTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(ElementNames.projectName, ElementNames.packageName);
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject(ElementNames.projectName);
	}

	@Test
	public void setExtendedClassifier() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resize(editor, new Point(600, 600), ElementNames.packageName);

		bot.createToolItemAndRename(editor, AbstractType.class, new Point(40, 40), ElementNames.abstractTypeName,
				ElementNames.packageName);

		bot.createImplementation(editor, ToolTypes.getToolItem(AbstractImplementation.class),
				ElementNames.abstractTypeName, ElementNames.abstractTypeName, new Point(350, 350),
				ElementNames.packageName);
		bot.createImplementation(editor, ToolTypes.getToolItem(AbstractImplementation.class),
				ElementNames.abstractTypeName, ElementNames.abstractTypeName + 2, new Point(150, 150),
				ElementNames.packageName);

		final String implName = ElementNames.abstractTypeName + "." + ElementNames.abstractTypeName + 2;
		bot.openPropertiesView(editor, ElementNames.packageName);
		bot.clickElementsMouse(editor, new String[] { implName });
		bot.selectTabbedPropertySection("AADL");
		bot.clickButton("Choose...");
		bot.clickButton("OK");
	}
}
