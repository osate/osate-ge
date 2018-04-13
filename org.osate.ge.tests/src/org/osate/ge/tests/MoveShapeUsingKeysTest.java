package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.aadl2.AbstractType;

public class MoveShapeUsingKeysTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(ElementNames.projectName, ElementNames.packageName);
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject(ElementNames.projectName);
	}

	@Test
	public void setFeatureDirection() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.resizeEditPart(editor, new Point(600, 600), ElementNames.packageName);

		// bot.createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(25, 25));
		// bot.renameElement(editor, ElementNames.abstractTypeName);
		bot.createToolItemAndRename(editor, AbstractType.class, new Point(25, 25), ElementNames.abstractTypeName,
				ElementNames.packageName);
		bot.resizeEditPart(editor, new Point(300, 300), ElementNames.abstractTypeName);

		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)editor.getSWTBotGefViewer().getEditPart(ElementNames.abstractTypeName).part();
		final PictogramElement pe = gsep.getPictogramElement();
		final GraphicsAlgorithm gaBefore = pe.getGraphicsAlgorithm();
		final int beforeX = gaBefore.getX();
		final int beforeY = gaBefore.getY();
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

		final GraphicsAlgorithm gaAfter = pe.getGraphicsAlgorithm();
		assertTrue(beforeX != gaAfter.getX() || beforeY != gaAfter.getY());
	}
}
