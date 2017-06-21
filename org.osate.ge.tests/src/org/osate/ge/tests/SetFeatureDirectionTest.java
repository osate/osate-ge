package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.FeatureGroup;
import org.osate.ge.internal.services.BusinessObjectResolutionService;
import org.osate.ge.internal.services.impl.DefaultBusinessObjectResolutionService;

public class SetFeatureDirectionTest {
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
	public void setFeatureDirection() {
		final SWTBotGefEditor editor = bot.gefEditor(ElementNames.packageName);
		final BusinessObjectResolutionService bor = new DefaultBusinessObjectResolutionService(((GraphitiShapeEditPart)editor.mainEditPart().part()).getFeatureProvider());

		List<SWTBotGefEditPart> featureGroupDescendants = editor.getEditPart(ElementNames.abstractTypeName).descendants(new FeatureGroupDescendants(bor));
		final GraphitiShapeEditPart featureGroup = (GraphitiShapeEditPart)featureGroupDescendants.get(0).part();
		final Object fg = bor.getBusinessObjectForPictogramElement(featureGroup.getPictogramElement());
		//TODO: clean up selections like this instead of hard coding names
		editor.select(featureGroupDescendants.get(0)).clickContextMenu("Set Direction to In and Out");
		
		assertTrue(fg instanceof FeatureGroup);
		assertTrue(((FeatureGroup)fg).getDirection() == DirectionType.IN_OUT);
/*		final SWTBotGefEditor editor = bot.gefEditor(CreateNewAADLPackageTest.packageName);

		//Open Associated Diagram "new classifier"
		editor.select("new_classifier").clickContextMenu("Open Associated Diagram");

		final SWTBotGefEditor editor2 = bot.gefEditor(CreateNewAADLPackageTest.packageName + "::new_classifier");
		//editor2 = AADLPackage::new_classifier
		//activateTool("Feature Group")
		editor2.getEditPart("new_classifier").select().resize(PositionConstants.SOUTH_EAST, 400, 400);

		editor2.activateTool("Feature Group");
		editor2.click(30,30);
		bot.saveAllEditors();

		editor.show();
		editor.activateTool("Feature Group Type");
		editor.click(150, 150);
		bot.sleep(2000);
		editor.select("new_feature2").clickContextMenu("Set Classifier...");

		bot.table().getTableItem("AADLPackage::new_classifier4").select();
		bot.button("OK").click();
		bot.sleep(2000000);*/
	}
	
	private class FeatureGroupDescendants implements Matcher<EditPart> {
		final private BusinessObjectResolutionService bor;

		private FeatureGroupDescendants(final BusinessObjectResolutionService bor) {
			this.bor = bor;
		}

		@Override
		public void describeTo(Description description) {}

		@Override
		public boolean matches(Object item) {
			if(item instanceof GraphitiShapeEditPart) {
				final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)item;
				if(bor.getBusinessObjectForPictogramElement(gsep.getPictogramElement()) instanceof FeatureGroup) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		public void describeMismatch(Object item, Description mismatchDescription) {}

		@Override
		public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}
	}
}
