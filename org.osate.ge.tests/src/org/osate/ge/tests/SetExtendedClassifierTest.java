package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractImplementationImpl;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class SetExtendedClassifierTest {
	private final SWTGefBot bot = new SWTGefBot();
	private final Helper helper = new Helper(bot);

	@Before
	public void setUp() {
		helper.createNewProjectAndPackage();
		helper.openDiagram(new String[] { ElementNames.projectName, "packages" }, ElementNames.packageName + ".aadl");
	}

	@After
	public void tearDown() {
		helper.deleteProject();
	}
	
	@Test
	public void setExtendedClassifier() {
		// context menu on abstract impl "Set Extended Classifier..."
		final SWTBotGefEditor editor = bot.gefEditor(ElementNames.packageName);
		helper.maximize(editor);
		helper.createToolItem(editor, ToolTypes.abstractType, new Point(0, 0));
		bot.sleep(1000);
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15), AbstractTypeImpl.class);

		helper.createToolItem(editor, ToolTypes.abstractImplementation, new Point(10, 350));
		bot.button("OK").click();
		bot.sleep(1000);
		
		helper.createToolItem(editor, ToolTypes.abstractImplementation, new Point(150, 70));
		bot.button("OK").click();
		bot.sleep(1000);

		editor.select(ElementNames.abstractTypeName + ".impl2").clickContextMenu("Set Extended Classifier...");
		bot.button("OK").click();
	}
}
