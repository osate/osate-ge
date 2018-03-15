package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.BusAccess;
import org.osate.aadl2.BusType;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.DeviceSubcomponent;
import org.osate.aadl2.DeviceType;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.ProcessorType;
import org.osate.aadl2.SystemImplementation;
import org.osate.aadl2.SystemSubcomponent;

public class DemoSystemTest {
	private final String hw = "hardware";
	private final String sw = "software";
	private final String cpu = "cpu";
	private final String application = "application";
	private final String projectName = "demo_test";
	private final String demo_system = "demo_system";
	private final String implName = "impl";
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(projectName, hw);
		bot.openDiagram(new String[] { projectName }, hw);
	}

	@After
	public void tearDown() {
		bot.deleteProject(projectName);
	}

	@Test
	public void runDemoTest() {
		final SWTBotGefEditor editor = bot.getEditor(hw);
		bot.resize(editor, hw, new Point(600, 600));
		bot.createTypeAndImplementation(editor, new Point(50, 50), hw, implName, hw,
				ToolTypes.getToolItem(SystemImplementation.class));

		bot.createToolItemAndRename(editor, hw, ProcessorType.class, new Point(20, 150), cpu);
		bot.createToolItemAndRename(editor, hw, DeviceType.class, new Point(300, 150), "sensor");
		bot.createToolItemAndRename(editor, hw, DeviceType.class, new Point(300, 300), "actuator");
		bot.createToolItemAndRename(editor, hw, BusType.class, new Point(20, 400), "ethernet_switch");
		bot.createToolItemAndRename(editor, hw, FeatureGroupType.class, new Point(300, 500), "sensor_data");
		bot.executeContextMenuCommand(editor, hw, "Layout Diagram");

		bot.createAADLPackage(projectName, sw);
		final SWTBotGefEditor swEditor = bot.getEditor(sw);
		bot.resize(swEditor, sw, new Point(600, 600));
		bot.createToolItemAndRename(swEditor, sw, ProcessorType.class, new Point(20, 150), "sensor_fuser");
		bot.createToolItemAndRename(swEditor, sw, ProcessorType.class, new Point(300, 150), "actuator_controller");
		bot.createTypeAndImplementation(swEditor, new Point(50, 50), sw, implName, application,
				ToolTypes.getToolItem(SystemImplementation.class));

		bot.createAADLPackage(projectName, demo_system);
		final SWTBotGefEditor demoTestEditor = bot.getEditor(demo_system);
		bot.resize(demoTestEditor, demo_system, new Point(600, 600));
		bot.createTypeAndImplementation(demoTestEditor, new Point(50, 50), demo_system, implName, demo_system,
				ToolTypes.getToolItem(SystemImplementation.class));

		final String demoSysImpl = demo_system + "." + implName;
		bot.openAssociatedDiagramFromContextMenu(demoTestEditor, demoSysImpl);

		final SWTBotGefEditor demoSysImplEditor = bot.getEditor(demo_system + "_" + demo_system + "_" + implName);
		bot.clickElement(demoSysImplEditor, demo_system + "." + implName);
		bot.executeContextMenuCommand(demoSysImplEditor, demoSysImpl, "All Filters");
		bot.resize(demoSysImplEditor, demoSysImpl, new Point(600, 600));

		final String swSc = "sw";
		bot.createToolItemAndRename(demoSysImplEditor, demoSysImpl, SystemSubcomponent.class,
				new Point(50, 50), swSc);
		bot.setElementOptionButtonInPropertiesView(demoSysImplEditor, "Properties", "AADL", "Choose...", swSc);
		bot.clickTableOption(AgeGefBot.qualifiedName(sw, application + "." + implName));
		bot.clickButton("OK");

		final String hwSc = "hw";
		bot.createToolItemAndRename(demoSysImplEditor, demoSysImpl, SystemSubcomponent.class,
				new Point(50, 150), hwSc);
		bot.setElementOptionButtonInPropertiesView(demoSysImplEditor, "Properties", "AADL", "Choose...", hwSc);
		bot.clickTableOption(AgeGefBot.qualifiedName(hw, hw + "." + implName));
		bot.clickButton("OK");

		demoTestEditor.click(demo_system);
		bot.clickElement(demoSysImplEditor, hwSc);
		bot.executeContextMenuCommand(demoSysImplEditor, hwSc, "All Filters");
		bot.resize(demoSysImplEditor, hwSc, new Point(350, 350));

		// Create devices
		bot.createToolItemAndRename(demoSysImplEditor, hwSc, DeviceSubcomponent.class,
				new Point(20, 20), "sensor1");
		bot.executeContextMenuCommand(demoSysImplEditor, "sensor1", "All Filters");
		bot.setElementOptionButtonInPropertiesView(demoSysImplEditor, "Properties", "AADL", "Choose...", "sensor1");
		bot.clickTableOption(AgeGefBot.qualifiedName(hw, "sensor"));
		bot.clickButton("OK");

		bot.createToolItemAndRename(demoSysImplEditor, "sensor1", BusAccess.class, new Point(80, 20), "ba_req");

		bot.sleep(5);

		bot.createToolItemAndRename(demoSysImplEditor, "sensor1", DataPort.class, new Point(20,20), "dp_out");
		bot.setElementOptionRadioInPropertiesView(demoSysImplEditor, "Properties", "AADL", "Output", "dp_out");
		bot.createToolItemAndRename(demoSysImplEditor, hwSc, DeviceSubcomponent.class,
				new Point(20, 200), "sensor2");
		bot.executeContextMenuCommand(demoSysImplEditor, "sensor2", "All Filters");
		bot.setElementOptionButtonInPropertiesView(demoSysImplEditor, "Properties", "AADL", "Choose...", "sensor2");
		bot.clickTableOption(AgeGefBot.qualifiedName(hw, "actuator"));
		bot.clickButton("OK");


		bot.createToolItemAndRename(demoSysImplEditor, "sensor2", DataPort.class,
				new Point(20, 20), "dp_in");
		bot.setElementOptionRadioInPropertiesView(demoSysImplEditor, "Properties", "AADL", "Input", "dp_in");

		demoTestEditor.show();
		bot.clickElement(demoTestEditor, demo_system);
		bot.createToolItemAndRename(demoTestEditor, demo_system, ProcessorType.class,
				new Point(25, 200), "cpu1");
		bot.createToolItemAndRename(demoTestEditor, demo_system, ProcessorType.class,
				new Point(300, 200), "cpu2");
		bot.clickElement(demoTestEditor, demo_system);

		bot.sleep(5);
		final String[] elementNames = { "cpu1", "cpu2" };
		bot.setElementOptionButtonInPropertiesView(demoTestEditor, "Properties", "AADL", "Choose...", elementNames);
		bot.clickTableOption(AgeGefBot.qualifiedName(hw, cpu));
		bot.clickButton("OK");


		bot.sleep(100);
	}
}
