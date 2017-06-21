package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractImplementationImpl;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class DeletingClassifierTest {
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
	public void deleteClassifer() {
		final SWTBotGefEditor editor = bot.gefEditor(ElementNames.packageName);
		helper.createToolItem(editor, ToolTypes.abstractType, new Point(0, 0));
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15), AbstractTypeImpl.class);

		helper.createToolItem(editor, ToolTypes.abstractImplementation, new Point(100, 100));
		bot.waitUntil(Conditions.shellIsActive("Select a Classifier"));
		final SWTBotShell shell = bot.activeShell();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));

		bot.waitUntil(new ICondition() {
			@Override
			public boolean test() throws Exception {
				final List<SWTBotGefEditPart> list = editor.getSWTBotGefViewer().editParts(new Helper.NewElementMatcher<EditPart>(editor, ElementNames.abstractTypeName + ".impl", AbstractImplementationImpl.class));
				return !list.isEmpty();
			}

			@Override
			public void init(SWTBot bot) {
			}

			@Override
			public String getFailureMessage() {
				return "element not created";
			}
		});

		final GraphitiShapeEditPart graphitiEditPart = (GraphitiShapeEditPart)editor.mainEditPart().part();
		final int childrenSizeBefore = graphitiEditPart.getChildren().size();
		bot.sleep(2000);
		editor.select(ElementNames.abstractTypeName + ".impl").clickContextMenu("Delete");
		bot.button("Yes").click();
		editor.save();
		assertTrue(childrenSizeBefore-1 == graphitiEditPart.getChildren().size());
		
		bot.sleep(5000);
	}
}
