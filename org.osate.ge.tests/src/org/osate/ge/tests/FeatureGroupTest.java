package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.ge.internal.ui.properties.SetFeatureGroupInversePropertySection;

public class FeatureGroupTest {
	private final AgeGefBot bot = new AgeGefBot();

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
	public void setFeatureClassifier() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resize(editor, ElementNames.packageName, new Point(600, 600));

		bot.createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(40, 40));
		bot.renameElement(editor, ElementNames.abstractTypeName);

		bot.openDiagramFromContextMenu(editor, ElementNames.abstractTypeName, "Associated Diagram");
		bot.resize(editor, ElementNames.abstractTypeName, new Point(200, 200));

		bot.createToolItem(editor, ElementNames.abstractTypeName, ToolTypes.featureGroup, new Point(30, 30));
		bot.renameElement(editor, ElementNames.featureGroupName);

		bot.createToolItem(editor, ElementNames.packageName, ToolTypes.featureGroupType, new Point(25, 250));
		bot.renameElement(editor, ElementNames.featureGroupTypeName);

		bot.setElementOptionButtonInPropertiesView(editor, ElementNames.featureGroupName, "Properties", "AADL",
				"Choose...");
		final GraphitiShapeEditPart fgtGsep = (GraphitiShapeEditPart) bot
				.findChild(editor, editor.getEditPart(ElementNames.packageName), ElementNames.featureGroupTypeName)
				.get(0).part();
		final FeatureGroupType fgt = (FeatureGroupType) AgeGefBot.getBusinessObject(editor,
				fgtGsep.getPictogramElement());
		bot.clickTableOption(fgt.getQualifiedName());
		bot.clickButton("OK");

		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) bot
				.findChild(editor, editor.getEditPart(ElementNames.abstractTypeName), ElementNames.featureGroupName)
				.get(0).part();
		final PictogramElement pe = gsep.getPictogramElement();
		assertTrue(isClassifierSet(editor, pe));
		assertTrue(setFeatureDirection(editor, pe));
		assertTrue(setFeatureInverse(editor,
				pe));
	}

	private boolean isClassifierSet(final SWTBotGefEditor editor, final PictogramElement pe) {
		final FeatureGroup fg = (FeatureGroup) AgeGefBot.getBusinessObject(editor, pe);
		return ElementNames.featureGroupTypeName.equalsIgnoreCase(fg.getClassifier().getName());
	}

	public boolean setFeatureDirection(final SWTBotGefEditor editor, final PictogramElement pe) {
		bot.setElementOptionRadioInPropertiesView(editor, ElementNames.featureGroupName, "Properties", "AADL",
				"Input");
		final FeatureGroup fg = (FeatureGroup) AgeGefBot.getBusinessObject(editor, pe);
		return fg.getDirection() == DirectionType.IN;
	}

	public boolean setFeatureInverse(final SWTBotGefEditor editor, final PictogramElement pe) {
		bot.clickCheckBoxWithId(SetFeatureGroupInversePropertySection.setFeatureGroupInverseUniqueId);
		final FeatureGroup fg = (FeatureGroup) AgeGefBot.getBusinessObject(editor, pe);
		return fg.isInverse();
	}
}
