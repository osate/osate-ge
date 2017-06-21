package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.impl.AbstractTypeImpl;

public class MoveShapeUsingKeysTest {
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
		helper.maximize(editor);
		helper.createToolItem(editor, ToolTypes.abstractType, new Point(0, 0));
		RenameHelper.renameElement(editor, ElementNames.abstractTypeName, new Point(15, 15), AbstractTypeImpl.class);
		
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)editor.getSWTBotGefViewer().getEditPart(ElementNames.abstractTypeName).part();
		final GraphicsAlgorithm ga = gsep.getPictogramElement().getGraphicsAlgorithm();
		
		final int beforeX = ga.getX();
		final int beforeY = ga.getY();
		
		editor.getSWTBotGefViewer().select(ElementNames.abstractTypeName);

		try {
			final Robot robot = new Robot();
			robot.setAutoDelay(25);
			robot.keyPress(KeyEvent.VK_PERIOD);

			for(int i = 0; i < 10; i++) {
				robot.keyPress(KeyEvent.VK_DOWN);
				robot.keyRelease(KeyEvent.VK_DOWN);
			}

			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);		
		} catch (AWTException e) {
			e.printStackTrace();
		}

		assertTrue(beforeX != ga.getX() || beforeY != ga.getY());
	}
}
