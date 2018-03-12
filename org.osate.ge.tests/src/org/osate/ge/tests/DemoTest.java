package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.BusTypeImpl;
import org.osate.aadl2.impl.DeviceTypeImpl;
import org.osate.aadl2.impl.FeatureGroupTypeImpl;
import org.osate.aadl2.impl.ProcessorTypeImpl;
import org.osate.ge.internal.ui.dialogs.ClassifierOperationDialog;

public class DemoTest {
	private final String packageName = "hardware";
	private final String implName = "impl";
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(ElementNames.projectName, packageName);
		bot.openDiagram(new String[] { ElementNames.projectName }, packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject();
	}

	@Test
	public void runDemoTest() {
		final SWTBotGefEditor editor = bot.getEditor(packageName);
		bot.resize(editor, packageName, new Point(600, 600));

		bot.createToolItem(editor, packageName, "System Implementation", new Point(50, 50));
		bot.waitUntilShellIsActive("Create Component Implementation");
		bot.setTextWithId(ClassifierOperationDialog.primaryPartIdentifier, implName);
		bot.clickRadio("New Component Type");
		bot.setTextWithId(ClassifierOperationDialog.baseValueIdentifier, "hardware");
		bot.clickButton("OK");
		bot.waitUntilElementExists(editor, "hardware" + "." + implName);

		bot.createToolItemAndRename(editor, packageName, ToolTypes.processorType, new Point(20, 150),
				ProcessorTypeImpl.class, "cpu");
		bot.createToolItemAndRename(editor, packageName, ToolTypes.deviceType, new Point(300, 150),
				DeviceTypeImpl.class, "sensor");
		bot.createToolItemAndRename(editor, packageName, ToolTypes.deviceType, new Point(300, 300),
				DeviceTypeImpl.class, "actuator");
		bot.createToolItemAndRename(editor, packageName, ToolTypes.busType, new Point(20, 400), BusTypeImpl.class,
				"ethernet_switch");
		bot.createToolItemAndRename(editor, packageName, ToolTypes.featureGroupType, new Point(300, 500),
				FeatureGroupTypeImpl.class, "sensor_data");
		bot.executeContextMenuCommand(editor, packageName, "Layout Diagram");
	}
}
