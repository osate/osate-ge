package org.osate.ge.tests;

import org.eclipse.graphiti.ui.platform.GraphitiConnectionEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.FeatureConnection;
import org.osate.aadl2.SystemImplementation;
import org.osate.aadl2.SystemSubcomponent;
import org.osate.aadl2.impl.FeatureConnectionImpl;
import org.osate.aadl2.impl.FlowSpecificationImpl;
import org.osate.ge.internal.ui.editor.FlowContributionItem;
import org.osate.ge.internal.ui.properties.AppearancePropertySection;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.tests.AgeGefBot.AgeSWTBotGefEditor;

public class CreateFlowImplTest {
	final AgeGefBot bot = new AgeGefBot();

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
	public void createFlowImpl() {
		final AgeSWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resize(editor, new Point(600, 600), ElementNames.packageName);
		bot.createTypeAndImplementation(editor, new Point(50, 50), "impl2", "sys",
				StringUtil.camelCaseToUser(SystemImplementation.class.getSimpleName()), ElementNames.packageName);
		bot.resize(editor, new Point(250, 200), "sys");

		bot.createToolItemAndRename(editor, DataPort.class, new Point(10, 30), "dp_in", "sys");
		bot.setElementOptionRadioInPropertiesView(editor, "AADL", "Input", "dp_in");

		bot.createToolItemAndRename(editor, DataPort.class, new Point(190, 100), "dp_out", "sys");
		bot.setElementOptionRadioInPropertiesView(editor, "AADL", "Output", "dp_out");

		final String flowSinkToolItem = "Flow Sink Specification";
		bot.createToolItem(editor, flowSinkToolItem, new Point(5, 5), "sys", "dp_in");


		final SWTBotGefConnectionEditPart flowSink = bot.getNewConnectionEditPart(editor,
				FlowSpecificationImpl.class).get(0);
		editor.select(flowSink);
		final GraphitiConnectionEditPart gcepSink = (GraphitiConnectionEditPart) flowSink.part();
		bot.clickConnection(editor, gcepSink.getConnectionFigure());

		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");

		bot.renameConnection(editor, flowSink, "fsink");

		final String flowSourceToolItem = "Flow Source Specification";
		bot.createToolItem(editor, flowSourceToolItem, new Point(5, 5), "sys", "dp_out");

		final SWTBotGefConnectionEditPart flowSrc = bot.getNewConnectionEditPart(editor,
				FlowSpecificationImpl.class).get(0);
		editor.select(flowSrc);
		final GraphitiConnectionEditPart gcepSrc = (GraphitiConnectionEditPart) flowSrc.part();
		bot.clickConnection(editor, gcepSrc.getConnectionFigure());

		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");

		bot.renameConnection(editor, flowSrc, "fsrc");

		bot.createImplementation(editor, StringUtil.camelCaseToUser(SystemImplementation.class.getSimpleName()), "sys",
				"impl",
				new Point(500, 500), ElementNames.packageName);

		bot.openAssociatedDiagramFromContextMenu(editor, "sys.impl2");
		final AgeSWTBotGefEditor implEditor = bot.getEditor(ElementNames.packageName + "_sys_impl2");
		bot.resize(implEditor, new Point(600, 600), "sys.impl2");

		bot.createToolItemAndRename(implEditor, SystemSubcomponent.class, new Point(250, 150), "ss1", "sys.impl2");
		bot.createToolItemAndRename(implEditor, SystemSubcomponent.class, new Point(450, 450), "ss2", "sys.impl2");

		bot.openPropertiesView(implEditor, "sys.impl2");
		bot.selectTabbedPropertySection("AADL");
		bot.clickElements(implEditor, new String[] { "ss1" }, new String[] { "ss2" });
		bot.clickButton("Choose...");
		bot.clickTableOption(AgeGefBot.qualifiedName(ElementNames.packageName, "sys"));
		bot.clickButton("OK");

		implEditor.activateTool(ToolTypes.getToolItem(FeatureConnection.class));
		// Create Connection 1
		bot.clickElements(implEditor, new String[] { "sys.impl2", "dp_in" }, new String[] { "ss1", "dp_in" });
		implEditor.activateDefaultTool();

		bot.selectTabbedPropertySection("Appearance");
		final SWTBotGefConnectionEditPart connectionEditPart = bot.getNewConnectionEditPart(implEditor,
				FeatureConnectionImpl.class).get(0);

		implEditor.select(connectionEditPart);
		final GraphitiConnectionEditPart gcep = (GraphitiConnectionEditPart) connectionEditPart.part();
		bot.clickConnection(implEditor, gcep.getConnectionFigure());
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");

		bot.renameConnection(implEditor, connectionEditPart, ElementNames.featureConnection);

		// Create Connection 2
		implEditor.activateTool(ToolTypes.getToolItem(FeatureConnection.class));
		bot.clickElements(implEditor, new String[] { "ss1", "dp_out" }, new String[] { "ss2", "dp_in" });
		implEditor.activateDefaultTool();

		final SWTBotGefConnectionEditPart connectionEditPart2 = bot.getNewConnectionEditPart(implEditor,
				FeatureConnectionImpl.class).get(0);
		implEditor.select(connectionEditPart2);
		final GraphitiConnectionEditPart gcep2 = (GraphitiConnectionEditPart) connectionEditPart2.part();
		bot.clickConnection(implEditor, gcep2.getConnectionFigure());
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");

		bot.renameConnection(implEditor, connectionEditPart2, ElementNames.featureConnection + "2");

		// Create Connection 3
		implEditor.activateTool(ToolTypes.getToolItem(FeatureConnection.class));
		bot.clickElements(implEditor, new String[] { "ss2", "dp_out" }, new String[] { "sys.impl2", "dp_out" });
		implEditor.activateDefaultTool();

		final SWTBotGefConnectionEditPart connectionEditPart3 = bot.getNewConnectionEditPart(implEditor,
				FeatureConnectionImpl.class).get(0);
		implEditor.select(connectionEditPart3);
		final GraphitiConnectionEditPart gcep3 = (GraphitiConnectionEditPart) connectionEditPart3.part();
		bot.clickConnection(implEditor, gcep3.getConnectionFigure());

		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");

		bot.renameConnection(implEditor, connectionEditPart3, ElementNames.featureConnection + "3");

		bot.clickElements(implEditor, new String[] { "sys.impl2" });

		bot.clickToolbarButtonWithTooltip("Create Flow Implementation");

		// Drag dialog down and to the left so it is not covering diagram for element selection
		bot.dragShellAwayFromEditor(implEditor, "Create Flow Implementation");

		final GraphitiConnectionEditPart con = (GraphitiConnectionEditPart) bot
				.getConnectionEditParts(implEditor, "fsrc", "sys.impl2", "dp_out").get(0).part();
		bot.clickConnection(implEditor, con.getConnectionFigure());
		bot.clickElements(implEditor, new String[] { "ss1" });

		bot.clickConnection(implEditor, gcep2.getConnectionFigure());
		bot.clickElements(implEditor, new String[] { "ss2" });

		bot.clickConnection(implEditor, gcep3.getConnectionFigure());
		bot.clickElements(implEditor, new String[] { "dp_out" });

		bot.setFocusShell("Create Flow Implementation");
		bot.clickButton("OK");

		bot.clickCombo(FlowContributionItem.highlightFlow, "sys.impl2::fsrc");
	}
}
