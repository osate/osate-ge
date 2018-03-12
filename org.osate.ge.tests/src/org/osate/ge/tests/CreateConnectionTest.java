package org.osate.ge.tests;

import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.FeatureConnection;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.osate.ge.internal.ui.properties.AppearancePropertySection;
import org.osate.ge.query.StandaloneQuery;
import org.osate.ge.services.QueryService;

public class CreateConnectionTest {
	private final AgeGefBot bot = new AgeGefBot();
	final StandaloneQuery connectionQuery = StandaloneQuery.create(
			(rootQuery) -> rootQuery.descendants().filter((fc) -> fc.getBusinessObject() instanceof FeatureConnection));
	final StandaloneQuery newConnectionQuery = StandaloneQuery.create(
			(rootQuery) -> rootQuery.descendants().filter((fc) -> fc.getBusinessObject() instanceof FeatureConnection
					&& ((FeatureConnection) fc.getBusinessObject()).getName().contains("new_")));

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage();
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
		bot.createAbstractTypeAndImplementation(ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject();
	}

	@Test
	public void createConnection() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resize(editor, ElementNames.abstractTypeName, new Point(100, 100));
		bot.createToolItem(editor, ElementNames.abstractTypeName, ToolTypes.abstractFeature, new Point(15, 15));
		bot.renameElement(editor, ElementNames.abstractFeatureNewName);

		bot.createToolItem(editor, ElementNames.abstractTypeName, ToolTypes.abstractFeature, new Point(15, 70));
		bot.renameElement(editor, ElementNames.abstractFeatureNewName2);

		final String abstractImplName = ElementNames.abstractTypeName + ".impl";
		bot.resize(editor, abstractImplName, new Point(500, 500));

		bot.selectElement(editor, ElementNames.abstractFeatureNewName2);
		bot.setElementOptionRadioInPropertiesView(editor, ElementNames.abstractFeatureNewName2, "Properties", "AADL",
				"Output");
		bot.executeContextMenuCommand(editor, abstractImplName,
				AgeGefBot.allFilters);

		createSubcomponents(editor, abstractImplName,
				ToolTypes.abstractSubcomponent);

		bot.executeContextMenuCommand(editor, ElementNames.abstractSubcomponentName, AgeGefBot.allFilters);
		bot.executeContextMenuCommand(editor, ElementNames.abstractSubcomponentName2, AgeGefBot.allFilters);

		final SWTBotGefEditPart subcomponent = editor.getEditPart(ElementNames.abstractSubcomponentName);
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

		final AgeDiagramEditor ageDiagramEditor = (AgeDiagramEditor) AgeGefBot.getAgeFeatureProvider(editor)
				.getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer();
		final QueryService queryService = (QueryService) ageDiagramEditor.getAdapter(QueryService.class);
		final DiagramNode dn = (DiagramNode) queryService.getFirstResult(newConnectionQuery,
				ageDiagramEditor.getAgeDiagram());
		final PictogramElement pe = ageDiagramEditor.getGraphitiAgeDiagram().getPictogramElement(dn);

		Display.getDefault().syncExec(() -> {
			ageDiagramEditor.selectPictogramElements(new PictogramElement[] { pe });
			bot.clickConnection(editor,
					(Connection) ageDiagramEditor.getDiagramBehavior().getFigureForPictogramElement(pe));
		});

		// TODO print edit parts
		bot.selectTabbedPropertySection("Appearance");
		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Show");
		// TODO print edit parts

		// HIDE LABEL and print edit parts
		bot.renameConnection(editor, ageDiagramEditor.getGraphitiAgeDiagram().getPictogramElement(dn),
				(Connection) ageDiagramEditor.getDiagramBehavior().getFigureForPictogramElement(pe),
				ElementNames.featureConnection);

		Assert.assertTrue(editor.getEditPart(ElementNames.featureConnection) != null);
	}

	private void createSubcomponents(final SWTBotGefEditor editor, final String parent, final String toolType) {
		bot.createToolItem(editor, parent, toolType, new Point(120, 110));
		bot.renameElement(editor, ElementNames.abstractSubcomponentName);

		bot.setElementOptionButtonInPropertiesView(editor, ElementNames.abstractSubcomponentName, "Properties", "AADL", "Choose...");
		bot.clickTableOption(ElementNames.packageName + "::" + ElementNames.abstractTypeName);
		bot.clickButton("OK");

		bot.createToolItem(editor, parent, toolType, new Point(120, 200));
		bot.renameElement(editor, ElementNames.abstractSubcomponentName2);

		bot.setElementOptionButtonInPropertiesView(editor, ElementNames.abstractSubcomponentName2, "Properties", "AADL", "Choose...");
		bot.clickTableOption(ElementNames.packageName + "::" + ElementNames.abstractTypeName);
		bot.clickButton("OK");
	}
}
