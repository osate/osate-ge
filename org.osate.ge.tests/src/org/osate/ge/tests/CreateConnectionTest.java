package org.osate.ge.tests;

import org.eclipse.draw2d.Connection;
import org.eclipse.graphiti.ui.platform.GraphitiConnectionEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.AbstractSubcomponent;
import org.osate.aadl2.impl.FeatureConnectionImpl;
import org.osate.ge.internal.ui.properties.AppearancePropertySection;
import org.osate.ge.tests.AgeGefBot.AgeSWTBotGefEditor;
import org.osate.ge.tests.AgeGefBot.ConnectionPoint;

public class CreateConnectionTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(ElementNames.projectName, ElementNames.packageName);
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
		bot.createAbstractTypeAndImplementation(ElementNames.packageName, new Point(30, 30));
	}

	@After
	public void tearDown() {
		bot.deleteProject(ElementNames.projectName);
	}

	@Test
	public void createConnection() {
		final AgeSWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resizeEditPart(editor, new Point(150, 150), ElementNames.abstractTypeName);
		bot.openPropertiesView();

		bot.createToolItemAndRename(editor, AbstractFeature.class, new Point(15, 15),
				ElementNames.abstractFeatureNewName, ElementNames.abstractTypeName);
		bot.createToolItemAndRename(editor, AbstractFeature.class, new Point(100, 100),
				ElementNames.abstractFeatureNewName2, ElementNames.abstractTypeName);

		bot.setElementOptionRadioInPropertiesView(editor, "AADL", "Output", ElementNames.abstractFeatureNewName2);
		System.err.println("After Set Element");
		final String abstractImplName = ElementNames.abstractTypeName + ".impl";
		bot.resizeEditPart(editor, new Point(400, 400), abstractImplName);
		bot.executeContextMenuCommand(editor, abstractImplName, AgeGefBot.allFilters);
		System.err.println("After before create subcomponents");

		createSubcomponents(editor, AbstractSubcomponent.class, abstractImplName);
		System.err.println("After Create subcomponents");

		// Show children of subcomponents
		bot.selectElements(editor, new String[] { abstractImplName, ElementNames.abstractSubcomponentName },
				new String[] { abstractImplName, ElementNames.abstractSubcomponentName2 });
		editor.clickContextMenu(AgeGefBot.allFilters);

		final SWTBotGefEditPart subcomponent = editor.getEditPart(ElementNames.abstractSubcomponentName);
		// Find in feature
		final SWTBotGefEditPart featureIn = bot.findChild(editor, subcomponent, ElementNames.abstractFeatureNewName)
				.get(0);

		final SWTBotGefEditPart subcomponent2 = editor.getEditPart(ElementNames.abstractSubcomponentName2);
		// Find out feature
		final SWTBotGefEditPart featureOut = bot.findChild(editor, subcomponent2, ElementNames.abstractFeatureNewName2)
				.get(0);
		System.err.println("create connection");
		// Create connection
		editor.activateTool("Feature Connection");
		editor.click(featureOut);
		editor.click(featureIn);
		editor.activateDefaultTool();

		final SWTBotGefConnectionEditPart connectionEditPart = bot
				.getNewConnectionEditPart(editor, FeatureConnectionImpl.class).get(0);
		editor.select(connectionEditPart);
		final Connection con = ((GraphitiConnectionEditPart) connectionEditPart.part()).getConnectionFigure();
		bot.clickConnection(editor, con);
		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");

		System.err.println("rename connection");
		// Rename
		bot.renameConnection(editor, connectionEditPart, ConnectionPoint.MIDDLE, ElementNames.featureConnection);

		// Hide label
		editor.select(ElementNames.featureConnection);
		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Hide");
		System.err.println("Hide");

	}

	private void createSubcomponents(final AgeSWTBotGefEditor editor, final Class<?> clazz, final String parent) {
		editor.setFocus();
		bot.createToolItemAndRename(editor, clazz, new Point(200, 100), ElementNames.abstractSubcomponentName, parent);
		bot.createToolItemAndRename(editor, clazz, new Point(120, 250), ElementNames.abstractSubcomponentName2, parent);

		bot.setElementOptionButtonInPropertiesView(editor, "AADL", "Choose...",
				new String[] { ElementNames.abstractSubcomponentName },
				new String[] { ElementNames.abstractSubcomponentName2 });
		bot.clickTableOption(AgeGefBot.qualifiedName(ElementNames.packageName, ElementNames.abstractTypeName));
		bot.clickButton("OK");
	}
}