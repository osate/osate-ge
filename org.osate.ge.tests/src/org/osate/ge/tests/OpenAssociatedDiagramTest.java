package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class OpenAssociatedDiagramTest {
	private final SWTGefBot bot = new SWTGefBot();
	private final Helper helper = new Helper(bot);

	@Before
	public void setUp() {
		helper.createNewProjectAndPackage();
	}

	@After
	public void tearDown() {
		helper.deleteProject();
	}

	@Test
	public void openAssociatedDiagram() {
		helper.openDiagram(new String[] { ElementNames.projectName, "packages" }, ElementNames.packageName + ".aadl");
		
		final SWTBotGefEditor editor = bot.gefEditor(ElementNames.packageName);
		helper.createToolItem(editor, ToolTypes.abstractType, new Point(0, 0));
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15), AbstractTypeImpl.class);
		editor.select(ElementNames.abstractTypeName).clickContextMenu("Open Associated Diagram");
		
		assertTrue(bot.gefEditor(ElementNames.packageName + "::" + ElementNames.abstractTypeName) != null);
	}
}
