package org.osate.ge.tests;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.BusTypeImpl;
import org.osate.aadl2.impl.DeviceSubcomponentImpl;
import org.osate.aadl2.impl.DeviceTypeImpl;
import org.osate.aadl2.impl.FeatureGroupTypeImpl;
import org.osate.aadl2.impl.ProcessTypeImpl;
import org.osate.aadl2.impl.ProcessorTypeImpl;
import org.osate.aadl2.impl.SystemSubcomponentImpl;

public class DemoSystemTest {
	private final String hw = "hardware";
	private final String sw = "software";
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
		bot.createTypeAndImplementation(editor, new Point(50, 50), hw, implName, hw, ToolTypes.systemImplementation);

		bot.createToolItemAndRename(editor, hw, ToolTypes.processorType, new Point(20, 150),
				ProcessorTypeImpl.class, "cpu");
		bot.createToolItemAndRename(editor, hw, ToolTypes.deviceType, new Point(300, 150),
				DeviceTypeImpl.class, "sensor");
		bot.createToolItemAndRename(editor, hw, ToolTypes.deviceType, new Point(300, 300),
				DeviceTypeImpl.class, "actuator");
		bot.createToolItemAndRename(editor, hw, ToolTypes.busType, new Point(20, 400), BusTypeImpl.class,
				"ethernet_switch");
		bot.createToolItemAndRename(editor, hw, ToolTypes.featureGroupType, new Point(300, 500),
				FeatureGroupTypeImpl.class, "sensor_data");
		bot.executeContextMenuCommand(editor, hw, "Layout Diagram");

		bot.createAADLPackage(projectName, sw);
		final SWTBotGefEditor swEditor = bot.getEditor(sw);
		bot.resize(swEditor, sw, new Point(600, 600));
		bot.createToolItemAndRename(swEditor, sw, ToolTypes.processorType, new Point(20, 150),
				ProcessTypeImpl.class, "sensor_fuser");
		bot.createToolItemAndRename(swEditor, sw, ToolTypes.processorType, new Point(300, 150),
				ProcessTypeImpl.class, "actuator_controller");
		bot.createTypeAndImplementation(swEditor, new Point(50, 50), sw, implName, application,
				ToolTypes.systemImplementation);

		bot.createAADLPackage(projectName, demo_system);
		final SWTBotGefEditor demoTestEditor = bot.getEditor(demo_system);
		bot.resize(demoTestEditor, demo_system, new Point(600, 600));
		bot.createTypeAndImplementation(demoTestEditor, new Point(50, 50), demo_system, implName, demo_system,
				ToolTypes.systemImplementation);

		final String demoSysImpl = demo_system + "." + implName;
		bot.openAssociatedDiagramFromContextMenu(demoTestEditor, demoSysImpl);

		final SWTBotGefEditor demoSysImplEditor = bot.getEditor(demo_system + "_" + demo_system + "_" + implName);
		bot.clickElement(demoSysImplEditor, demo_system + "." + implName);
		bot.executeContextMenuCommand(demoSysImplEditor, demoSysImpl, "All Filters");
		bot.resize(demoSysImplEditor, demoSysImpl, new Point(600, 600));

		final String swSc = "sw";
		bot.createToolItemAndRename(demoSysImplEditor, demoSysImpl, ToolTypes.systemSubcomponent, new Point(50, 50),
				SystemSubcomponentImpl.class, swSc);
		bot.setElementOptionButtonInPropertiesView(demoSysImplEditor, swSc, "Properties", "AADL", "Choose...");
		bot.clickTableOption(AgeGefBot.qualifiedName(sw, application + "." + implName));
		bot.clickButton("OK");

		final String hwSc = "hw";
		bot.createToolItemAndRename(demoSysImplEditor, demoSysImpl, ToolTypes.systemSubcomponent, new Point(50, 150),
				SystemSubcomponentImpl.class, hwSc);
		bot.setElementOptionButtonInPropertiesView(demoSysImplEditor, hwSc, "Properties", "AADL", "Choose...");
		bot.clickTableOption(AgeGefBot.qualifiedName(hw, hw + "." + implName));
		bot.clickButton("OK");

		demoTestEditor.click(demo_system);
		bot.clickElement(demoSysImplEditor, hwSc);
		bot.executeContextMenuCommand(demoSysImplEditor, hwSc, "All Filters");
		bot.resize(demoSysImplEditor, hwSc, new Point(350, 350));

		// Create devices
		bot.createToolItemAndRename(demoSysImplEditor, hwSc, ToolTypes.deviceSubcomponent, new Point(20, 20),
				DeviceSubcomponentImpl.class, "sensor1");
		bot.createToolItemAndRename(demoSysImplEditor, hwSc, ToolTypes.deviceSubcomponent, new Point(20, 200),
				DeviceSubcomponentImpl.class, "sensor2");


		bot.sleep(100);
	}
}
