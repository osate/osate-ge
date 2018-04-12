package org.osate.ge.tests;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.graphiti.ui.platform.GraphitiConnectionEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.AbstractSubcomponent;
import org.osate.aadl2.FeatureConnection;
import org.osate.ge.internal.graphiti.AgeFeatureProvider;
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
		bot.createAbstractTypeAndImplementation(ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject(ElementNames.projectName);
	}

	@Test
	public void createConnection() {
		final AgeSWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resizeEditPart(editor, new Point(150, 150), ElementNames.abstractTypeName);
		bot.createToolItem(editor, ToolTypes.getToolItem(AbstractFeature.class), new Point(15, 15),
				ElementNames.abstractTypeName);
		bot.renameElement(editor, ElementNames.abstractFeatureNewName);
		bot.openPropertiesView(editor, ElementNames.abstractTypeName);

		bot.createToolItem(editor, ToolTypes.getToolItem(AbstractFeature.class), new Point(100, 100),
				ElementNames.abstractTypeName);
		bot.renameElement(editor, ElementNames.abstractFeatureNewName2);

		final String abstractImplName = ElementNames.abstractTypeName + ".impl";
		bot.resizeEditPart(editor, new Point(400, 400), abstractImplName);

		bot.setElementOptionRadioInPropertiesView(editor, "AADL", "Output", ElementNames.abstractTypeName,
				ElementNames.abstractFeatureNewName2);
		bot.executeContextMenuCommand(editor, abstractImplName, AgeGefBot.allFilters);

		createSubcomponents(editor, AbstractSubcomponent.class, abstractImplName);
		editor.setFocus();
		// Show children of subcomponents
		bot.selectElements(editor, new String[] { abstractImplName, ElementNames.abstractSubcomponentName },
				new String[] { abstractImplName, ElementNames.abstractSubcomponentName2 });
		editor.clickContextMenu(AgeGefBot.allFilters);

		final SWTBotGefEditPart subcomponent = editor.getEditPart(ElementNames.abstractSubcomponentName);
		System.err.println(subcomponent + " subcomponent");
		bot.sleep(5);
		// Find in feature
		final List<SWTBotGefEditPart> featureIn = bot.findChild(editor, subcomponent,
				ElementNames.abstractFeatureNewName);

		final SWTBotGefEditPart subcomponent2 = editor.getEditPart(ElementNames.abstractSubcomponentName2);
		// Find out feature
		final List<SWTBotGefEditPart> featureOut = bot.findChild(editor, subcomponent2,
				ElementNames.abstractFeatureNewName2);

		// Create connection
		editor.activateTool("Feature Connection");
		editor.click(featureOut.get(0));
		editor.click(featureIn.get(0));
		editor.activateDefaultTool();

		final AgeFeatureProvider ageFeatureProvider = AgeGefBot.getAgeFeatureProvider(editor);
		final SWTBotGefConnectionEditPart connectionEditPart = editor.allConnections().stream().filter(editPart -> {
			final GraphitiConnectionEditPart gcep = (GraphitiConnectionEditPart) editPart.part();
			final Object bo = ageFeatureProvider.getBusinessObjectForPictogramElement(gcep.getPictogramElement());
			if (bo instanceof FeatureConnection) {
				return ((FeatureConnection) bo).getName().contains("new_");
			}

			return false;
		}).collect(Collectors.toList()).get(0);

		// Show connection label for renaming
		editor.select(connectionEditPart);
		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");

		// Rename
		bot.renameConnection(editor, connectionEditPart, ConnectionPoint.MIDDLE, ElementNames.featureConnection);

		// Hide label
		editor.select(ElementNames.featureConnection);
		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Hide");

		// Assert
		final FeatureConnection fc = (FeatureConnection) ageFeatureProvider.getBusinessObjectForPictogramElement(
				((GraphitiConnectionEditPart) connectionEditPart.part()).getPictogramElement());
		Assert.assertTrue(fc.getName().equalsIgnoreCase(ElementNames.featureConnection));
	}

	private void createSubcomponents(final SWTBotGefEditor editor, final Class<?> clazz, final String parent) {
		bot.createToolItemAndRename(editor, clazz, new Point(200, 100), ElementNames.abstractSubcomponentName, parent);
		bot.createToolItemAndRename(editor, clazz, new Point(120, 250), ElementNames.abstractSubcomponentName2, parent);

		bot.openPropertiesView(editor, ElementNames.abstractSubcomponentName);
		bot.selectTabbedPropertySection("AADL");
		bot.selectElements(editor, new String[] { ElementNames.abstractSubcomponentName },
				new String[] { ElementNames.abstractSubcomponentName2 });
		bot.clickButton("Choose...");
		bot.clickTableOption(AgeGefBot.qualifiedName(ElementNames.packageName, ElementNames.abstractTypeName));
		bot.clickButton("OK");
	}
}