package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.impl.AbstractTypeImpl;
import org.osate.aadl2.impl.FeatureGroupImpl;
import org.osate.aadl2.impl.FeatureGroupTypeImpl;
import org.osate.ge.internal.services.impl.DefaultBusinessObjectResolutionService;

public class SetFeatureClassifierTest {
	private final SWTGefBot bot = new SWTGefBot();
	private final Helper helper = new Helper(bot);

	@Before
	public void setUp() {
		helper.createNewProjectAndPackage();
		helper.openDiagram(new String[] { ElementNames.projectName, "packages" }, ElementNames.packageName + ".aadl");
	}

	@After
	public void tearDown() {
		helper.deleteProject();
	}
	
	@Test
	public void setFeatureClassifier() {
		final SWTBotGefEditor editor = bot.gefEditor(ElementNames.packageName);
		helper.maximize(editor);
		helper.createToolItem(editor, ToolTypes.abstractType, new Point(0, 0));
		bot.sleep(1000);
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15), AbstractTypeImpl.class);

		editor.select(ElementNames.abstractTypeName).clickContextMenu("Open Associated Diagram");

		final SWTBotGefEditor editor2 = bot.gefEditor(ElementNames.packageName + "::" + ElementNames.abstractTypeName);
		editor2.getEditPart(ElementNames.abstractTypeName).select().resize(PositionConstants.SOUTH_EAST, 400, 400);
		
		helper.createToolItem(editor2, ToolTypes.featureGroup, new Point(30, 30));
		bot.sleep(1000);
		RenameHelper.renameElement(editor2, ElementNames.featureGroupName, new Point(25, 25), FeatureGroupImpl.class);
		bot.saveAllEditors();
		
		editor.show();
		helper.createToolItem(editor, ToolTypes.featureGroupType, new Point(150, 150));
		bot.sleep(1000);
		RenameHelper.renameElement(editor, ElementNames.featureGroupTypeName, new Point(15, 15), FeatureGroupTypeImpl.class);
		
		editor.select(ElementNames.featureGroupName).clickContextMenu("Set Classifier...");
		bot.table().getTableItem(ElementNames.packageName + "::" + ElementNames.featureGroupTypeName).select();
		bot.button("OK").click();
		
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)editor.getSWTBotGefViewer().getEditPart(ElementNames.featureGroupName).part();
		final FeatureGroup fg = (FeatureGroup)new DefaultBusinessObjectResolutionService(((GraphitiShapeEditPart)editor.mainEditPart().part()).getFeatureProvider()).getBusinessObjectForPictogramElement(gsep.getPictogramElement());

		assertTrue(fg.getClassifier().getName().equals(ElementNames.featureGroupTypeName));
		assertTrue(setFeatureDirection(editor));
		assertTrue(setFeatureInverse(editor));
	}
	
	public boolean setFeatureDirection(final SWTBotGefEditor editor) {
		editor.select(ElementNames.featureGroupName).clickContextMenu("Set Direction to In and Out");

		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)editor.getSWTBotGefViewer().getEditPart(ElementNames.featureGroupName).part();
		final FeatureGroup fg = (FeatureGroup)new DefaultBusinessObjectResolutionService(((GraphitiShapeEditPart)editor.mainEditPart().part()).getFeatureProvider()).getBusinessObjectForPictogramElement(gsep.getPictogramElement());

		return ((FeatureGroup)fg).getDirection() == DirectionType.IN_OUT;
	}
	
	public boolean setFeatureInverse(final SWTBotGefEditor editor) {
		editor.select(ElementNames.featureGroupName).clickContextMenu("Set to Inverse");

		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)editor.getSWTBotGefViewer().getEditPart(ElementNames.featureGroupName).part();
		final FeatureGroup fg = (FeatureGroup)new DefaultBusinessObjectResolutionService(((GraphitiShapeEditPart)editor.mainEditPart().part()).getFeatureProvider()).getBusinessObjectForPictogramElement(gsep.getPictogramElement());
		
		return fg.isInverse();
	}
}
