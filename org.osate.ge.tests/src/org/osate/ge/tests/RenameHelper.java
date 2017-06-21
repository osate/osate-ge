package org.osate.ge.tests;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.ui.PlatformUI;

public class RenameHelper {
	static void renameElement(final SWTBotGefEditor editor, final String newName, final Point offsetPoint, final Class<?> clazz) {
		final List<SWTBotGefEditPart> list = editor.getSWTBotGefViewer().editParts(new Helper.NewElementMatcher<EditPart>(editor, "new_", clazz));
		editor.getSWTBotGefViewer().select(list);

		final List<SWTBotGefEditPart> selectedEditParts = editor.getSWTBotGefViewer().selectedEditParts();
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)selectedEditParts.get(0).part();
		
		try {
			final Robot robot = new Robot();
			robot.setAutoDelay(25);

			editor.getWidget().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					final FigureCanvas canvas = (FigureCanvas)editor.getWidget().getDisplay().getFocusControl();
					final GraphicsAlgorithm ga = gsep.getPictogramElement().getGraphicsAlgorithm();
					final Point point = PlatformUI.getWorkbench().getDisplay().map(editor.getWidget().getDisplay().getFocusControl(), null,
							ga.getX(), ga.getY());
					
					robot.mouseMove(point.x-canvas.getHorizontalBar().getSelection()+5+offsetPoint.x, point.y-canvas.getVerticalBar().getSelection()+5+offsetPoint.y);
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				}
			});
			
			editor.bot().sleep(1000);
			typeName(robot, newName);

			robot.mouseMove(300, 300);
			editor.bot().sleep(3000);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	
	}
	
	static void renameConnection(final SWTBotGefEditor editor, final String newName, final Point offsetPoint, final Class<?> clazz, org.eclipse.graphiti.mm.algorithms.styles.Point loc) {
		try {
			final Robot robot = new Robot();
			editor.getWidget().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					final FigureCanvas canvas = (FigureCanvas)editor.getWidget().getDisplay().getFocusControl();
					final Point point = PlatformUI.getWorkbench().getDisplay().map(editor.getWidget().getDisplay().getFocusControl(), null,
							loc.getX(), loc.getY());

					robot.mouseMove(point.x-canvas.getHorizontalBar().getSelection()+5+offsetPoint.x, point.y-canvas.getVerticalBar().getSelection()+5+offsetPoint.y);
					editor.bot().sleep(1000);
					
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					
					editor.bot().sleep(1000);
					
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				}
			});
			
			editor.bot().sleep(3000);
			typeName(robot, newName);
			robot.mouseMove(300, 300);
			editor.bot().sleep(3000);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	
	static void typeName(final Robot robot, final String newName) {
		final OfInt  it = newName.chars().iterator();
		while(it.hasNext()) {
			final int c = it.nextInt();
			if(KeyEvent.VK_UNDERSCORE == KeyEvent.getExtendedKeyCodeForChar(c)) {
				robot.keyPress(KeyEvent.VK_SHIFT);
				robot.keyPress(KeyEvent.VK_MINUS);
				robot.keyRelease(KeyEvent.VK_MINUS);
				robot.keyRelease(KeyEvent.VK_SHIFT);
			} else {
				robot.keyPress(KeyEvent.getExtendedKeyCodeForChar(c));
				robot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(c));
			}
		}

		final int keyCode = KeyEvent.VK_ENTER;
		robot.keyPress(keyCode);
		robot.keyRelease(keyCode);
	}
}

