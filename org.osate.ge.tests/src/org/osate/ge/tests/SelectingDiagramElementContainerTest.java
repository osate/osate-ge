package org.osate.ge.tests;

import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AadlPackage;
import org.osate.ge.internal.graphiti.AgeFeatureProvider;

public class SelectingDiagramElementContainerTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage();
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(20, 20));
		bot.renameElement(editor, ElementNames.abstractTypeName);
	}

	@After
	public void tearDown() {
		bot.deleteProject();
	}

	@Test
	public void selectDiagramElementContainer() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		editor.select(ElementNames.abstractTypeName).clickContextMenu("Select Container");
		final GraphitiShapeEditPart diagram = (GraphitiShapeEditPart)editor.mainEditPart().part();
		final AgeFeatureProvider featureProvider = (AgeFeatureProvider) diagram.getFeatureProvider();
		final PictogramElement selectedPictogramElement = diagram.getFeatureProvider().getDiagramTypeProvider()
				.getDiagramBehavior().getDiagramContainer().getSelectedPictogramElements()[0];
		Assert.assertTrue(
				featureProvider
				.getBusinessObjectForPictogramElement(selectedPictogramElement) instanceof AadlPackage);
	}
}
