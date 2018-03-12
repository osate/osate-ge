package org.osate.ge.tests;

import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.ModeTransition;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.osate.ge.query.StandaloneQuery;
import org.osate.ge.services.QueryService;

public class ModesTransitionTest {
	private final AgeGefBot bot = new AgeGefBot();
	final StandaloneQuery modeTransitionQuery = StandaloneQuery.create(
			(rootQuery) -> rootQuery.descendants().filter((d) -> d.getBusinessObject() instanceof ModeTransition));
	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage();
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject();
	}

	@Test
	public void createModes() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.maximize();
		bot.resize(editor, ElementNames.packageName, new Point(600, 600));
		editor.setFocus();

		bot.createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(40, 40));
		bot.renameElement(editor, ElementNames.abstractTypeName);
		bot.resize(editor, ElementNames.abstractTypeName, new Point(300, 300));

		bot.createToolItem(editor, ElementNames.abstractTypeName, ToolTypes.abstractFeature, new Point(15, 15));
		bot.renameElement(editor, ElementNames.abstractFeatureNewName);

		bot.createToolItem(editor, ElementNames.abstractTypeName, ToolTypes.mode, new Point(80, 90));
		bot.renameElement(editor, ElementNames.mode);

		bot.createToolItem(editor, ElementNames.abstractTypeName, ToolTypes.mode, new Point(80, 200));
		bot.renameElement(editor, ElementNames.mode2);

		final SWTBotGefEditPart abstractType = editor.getEditPart(ElementNames.abstractTypeName);
		// Find in feature
		final List<SWTBotGefEditPart> mode = bot.findChild(editor, abstractType,
				ElementNames.mode);

		// Find out feature
		final List<SWTBotGefEditPart> mode2 = bot.findChild(editor, abstractType, ElementNames.mode2);

		// Create connection
		editor.activateTool(ToolTypes.modeTransition);
		editor.click(mode2.get(0));
		editor.click(mode.get(0));
		bot.clickButton("OK");

		final AgeDiagramEditor ageDiagramEditor = (AgeDiagramEditor) AgeGefBot.getAgeFeatureProvider(editor)
				.getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer();
		final QueryService queryService = (QueryService) ageDiagramEditor.getAdapter(QueryService.class);

		Assert.assertTrue(queryService.getFirstResult(modeTransitionQuery, ageDiagramEditor.getAgeDiagram()) != null);
	}
}
