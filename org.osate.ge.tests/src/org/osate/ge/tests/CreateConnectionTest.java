package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.FeatureConnection;
import org.osate.aadl2.impl.AbstractFeatureImpl;
import org.osate.aadl2.impl.AbstractSubcomponentImpl;
import org.osate.aadl2.impl.AbstractTypeImpl;
import org.osate.ge.internal.services.BusinessObjectResolutionService;
import org.osate.ge.internal.services.impl.DefaultBusinessObjectResolutionService;

public class CreateConnectionTest {
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
	public void createConnection() {
		final SWTBotGefEditor editor = bot.gefEditor(ElementNames.packageName);
		helper.maximize(editor);
		helper.createToolItem(editor, ToolTypes.abstractType, new Point(0, 0));
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15), AbstractTypeImpl.class);

		helper.createToolItem(editor, ToolTypes.abstractImplementation, new Point(100, 100));
		bot.sleep(1000);
		bot.button("OK").click();

		editor.select(ElementNames.abstractTypeName).clickContextMenu("Open Associated Diagram");
		final SWTBotGefEditor editor2 = bot.gefEditor(ElementNames.packageName + "::" + ElementNames.abstractTypeName);
		editor2.show();

		editor2.getEditPart(ElementNames.abstractTypeName).select().resize(PositionConstants.SOUTH_EAST, 400, 400);
		editor2.select(ElementNames.abstractTypeName);
		bot.sleep(1000);
		editor2.click(ElementNames.abstractTypeName);
		bot.sleep(1000);

		helper.createToolItem(editor2, ToolTypes.abstractFeature, new Point(40, 40));
		bot.sleep(1000);

		RenameHelper.renameElement(editor2, ElementNames.abstractFeatureNewName, new Point(25, 25), AbstractFeatureImpl.class);
		editor2.select(ElementNames.abstractFeatureNewName).clickContextMenu("Set Direction to Out");
		bot.sleep(1000);

		editor.show();
		editor.select(ElementNames.abstractTypeName).clickContextMenu("Open Associated Diagram");
		final SWTBotGefEditor editor3 = bot.gefEditor(ElementNames.packageName + "::" + ElementNames.abstractTypeName);
		bot.sleep(10000);
		helper.createToolItem(editor3, ToolTypes.abstractFeature, new Point(40, 100));

		RenameHelper.renameElement(editor3, ElementNames.abstractFeatureNewName2, new Point(25, 25), AbstractFeatureImpl.class);

		editor3.select(ElementNames.abstractFeatureNewName2).clickContextMenu("Set Direction to In and Out");

		editor.show();
		editor.select(ElementNames.abstractTypeName + ".impl").clickContextMenu("Open Associated Diagram");
		bot.sleep(1000);
		final SWTBotGefEditor editor4 = bot.gefEditor(ElementNames.packageName + "::" + ElementNames.abstractTypeName + ".impl");
		editor4.getEditPart(ElementNames.abstractTypeName + ".impl").select().resize(PositionConstants.SOUTH_EAST, 400, 400);
		
		helper.createToolItem(editor4, ToolTypes.abstractSubcomponent, new Point(30, 99));
		
		RenameHelper.renameElement(editor4, ElementNames.abstractSubcomponentName, new Point(15, 25), AbstractSubcomponentImpl.class);

		editor4.select(ElementNames.abstractSubcomponentName).clickContextMenu("Set Classifier...");
		bot.table().getTableItem(ElementNames.packageName + "::" + ElementNames.abstractTypeName).click();
		bot.button("OK").click();

		helper.createToolItem(editor4, ToolTypes.abstractSubcomponent, new Point(30, 60));

		RenameHelper.renameElement(editor4, ElementNames.abstractSubcomponentName2, new Point(15, 25), AbstractSubcomponentImpl.class);

		editor4.select(ElementNames.abstractSubcomponentName2).clickContextMenu("Set Classifier...");
		bot.table().getTableItem(ElementNames.packageName + "::" + ElementNames.abstractTypeName).click();
		bot.button("OK").click();

		final GraphitiShapeEditPart diagramEditPart = (GraphitiShapeEditPart)editor4.mainEditPart().part();
		final BusinessObjectResolutionService bor = new DefaultBusinessObjectResolutionService(diagramEditPart.getFeatureProvider());
		final List<SWTBotGefEditPart> feature = editor4.getEditPart(ElementNames.abstractSubcomponentName).descendants(new Anchors(bor));
		final List<SWTBotGefEditPart> feature2 = editor4.getEditPart(ElementNames.abstractSubcomponentName2).descendants(new Anchors(bor));

		editor4.activateTool("Feature Connection");
		editor4.click(feature.get(0));
		editor4.click(feature2.get(0));

		assertTrue(!editor4.mainEditPart().descendants(new Connections(bor)).isEmpty());
	}

	private class Connections implements Matcher<EditPart> {
		final private BusinessObjectResolutionService bor;

		private Connections(final BusinessObjectResolutionService bor) {
			this.bor = bor;
		}

		@Override
		public void describeTo(Description description) {}

		@Override
		public boolean matches(Object item) {
			if(item instanceof GraphitiShapeEditPart) {
				final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)item;
				if(bor.getBusinessObjectForPictogramElement(gsep.getPictogramElement()) instanceof FeatureConnection) {
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

	private class Anchors implements Matcher<EditPart> {
		final private BusinessObjectResolutionService bor;

		private Anchors(final BusinessObjectResolutionService bor) {
			this.bor = bor;
		}

		@Override
		public void describeTo(Description description) {}

		@Override
		public boolean matches(Object item) {
			if(item instanceof GraphitiShapeEditPart) {
				final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)item;
				if(bor.getBusinessObjectForPictogramElement(gsep.getPictogramElement()) instanceof AbstractFeature) {
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
