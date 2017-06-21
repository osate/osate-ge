package org.osate.ge.tests;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.ViewReference;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractImplementationImpl;
import org.osate.aadl2.impl.AbstractTypeImpl;
import org.osate.aadl2.impl.NamedElementImpl;
import org.osate.ge.internal.services.BusinessObjectResolutionService;
import org.osate.ge.internal.services.impl.DefaultBusinessObjectResolutionService;

public class InstantiateAndOpenImplTest {
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
	public void instantiateImplTest() {
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

		editor.select(ElementNames.abstractTypeName + ".impl").clickContextMenu("Instantiate");
	}

	@Test
	public void openImplTest() {
		instantiateImplTest();

		final String[] nodePath = new String[] { ElementNames.projectName, "instances" };
		// Expand tree path and select the aadl file to open
		final SWTBotTreeItem aadlPackage = bot.tree().expandNode(nodePath).getNode(0).click();

		// Open Diagram
		aadlPackage.contextMenu("Open Diagram").click();
		
		bot.waitUntil(new ICondition() {			
			@Override
			public boolean test() throws Exception {
				return bot.activeEditor().getTitle().contains("Instance");
			}
			
			@Override
			public void init(SWTBot bot) {
			}
			
			@Override
			public String getFailureMessage() {
				return null;
			}
		});
	}
}
