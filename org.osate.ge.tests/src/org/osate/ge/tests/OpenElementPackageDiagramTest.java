package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class OpenElementPackageDiagramTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage();
	}

	@After
	public void tearDown() {
		bot.deleteProject();
	}

	@Test
	public void openElementPackageDiagram() {
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
		final SWTBotGefEditor pkgDiagramEditor = bot.getEditor(ElementNames.packageName);
		bot.createToolItem(pkgDiagramEditor, ElementNames.packageName, ToolTypes.abstractType, new Point(20, 20));
		bot.waitUntilNewElementIsCreated(pkgDiagramEditor, AbstractTypeImpl.class);
		RenameHelper.renameElement(pkgDiagramEditor, ElementNames.abstractTypeName, new Point(45, 15));
		bot.openDiagramFromContextMenu(pkgDiagramEditor, ElementNames.abstractTypeName, AgeGefBot.associatedDiagram);
		pkgDiagramEditor.saveAndClose();

		final SWTBotGefEditor associatedDiagramEditor = bot.getEditor(ElementNames.packageName + "_" + ElementNames.abstractTypeName);
		associatedDiagramEditor.select(ElementNames.abstractTypeName).clickContextMenu("Package Diagram");
		assertTrue(bot.isActiveEditor(ElementNames.packageName));
	}
}
