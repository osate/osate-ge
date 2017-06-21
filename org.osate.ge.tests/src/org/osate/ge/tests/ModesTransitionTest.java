package org.osate.ge.tests;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractFeatureImpl;
import org.osate.aadl2.impl.AbstractTypeImpl;
import org.osate.aadl2.impl.ModeImpl;
import org.osate.aadl2.impl.ModeTransitionTriggerImpl;
import org.osate.aadl2.impl.NamedElementImpl;
import org.osate.ge.internal.services.BusinessObjectResolutionService;
import org.osate.ge.internal.services.impl.DefaultBusinessObjectResolutionService;

public class ModesTransitionTest {
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
	public void createModes() {
		final SWTBotGefEditor editor = bot.gefEditor(ElementNames.packageName);
		helper.maximize(editor);
		helper.createToolItem(editor, ToolTypes.abstractType, new Point(0, 0));
		bot.sleep(1000);
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15), AbstractTypeImpl.class);

		editor.select(ElementNames.abstractTypeName).clickContextMenu("Open Associated Diagram");

		final SWTBotGefEditor editor2 = bot.gefEditor(ElementNames.packageName + "::" + ElementNames.abstractTypeName);
		editor2.getEditPart(ElementNames.abstractTypeName).select().resize(PositionConstants.SOUTH_EAST, 400, 400);
		helper.createToolItem(editor2, ToolTypes.mode, new Point(15, 70));
		bot.sleep(1000);
		RenameHelper.renameElement(editor2, ElementNames.mode, new Point(20, 20), ModeImpl.class);

		helper.createToolItem(editor2, ToolTypes.mode, new Point(15, 150));
		bot.sleep(1000);
		RenameHelper.renameElement(editor2, ElementNames.mode2, new Point(20, 20), ModeImpl.class);

		helper.createToolItem(editor2, ToolTypes.abstractFeature, new Point(40, 40));
		bot.sleep(1000);
		RenameHelper.renameElement(editor2, ElementNames.abstractFeatureNewName, new Point(25, 25), AbstractFeatureImpl.class);

		editor2.activateTool(ToolTypes.modeTransition);
		editor2.click(ElementNames.mode);
		bot.sleep(500);

		editor2.click(ElementNames.mode2);
		bot.button("OK").click();
		bot.sleep(1000);

		editor2.activateTool("Select");
		editor2.click(ElementNames.mode2);

		final BusinessObjectResolutionService bor = new DefaultBusinessObjectResolutionService(((GraphitiShapeEditPart)editor2.mainEditPart().part()).getFeatureProvider());
		org.eclipse.graphiti.mm.algorithms.styles.Point loc = null;
		for(final SWTBotGefEditPart c : editor2.mainEditPart().children()) {
			for(final Object ch : c.part().getChildren()) {
				final Object model = ((AbstractGraphicalEditPart)ch).getModel();
					if(model instanceof FixPointAnchor) {
					final FixPointAnchor fpa = (FixPointAnchor)model;
					
					if(!fpa.getOutgoingConnections().isEmpty()) {
						for(Connection oc : fpa.getOutgoingConnections()) {
							final Object bo = bor.getBusinessObjectForPictogramElement(oc.getGraphicsAlgorithm().getPictogramElement());
							if(bo instanceof ModeTransitionTriggerImpl) {
								final ModeTransitionTriggerImpl mtti = (ModeTransitionTriggerImpl)bo;
								if(mtti.getOwner() instanceof NamedElementImpl) {
									loc = fpa.getLocation();
								}
							}
						}
					}
				}
			}
		}
		
		RenameHelper.renameConnection(editor2, ElementNames.modeTransition, new Point(15, 15), ModeTransitionTriggerImpl.class, loc);
		editor2.select(ElementNames.modeTransition);
	}

	public void selectActiveComponents() {

	}
}
