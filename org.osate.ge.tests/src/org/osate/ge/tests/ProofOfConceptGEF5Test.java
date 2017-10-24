package org.osate.ge.tests;

import static org.eclipse.swtbot.eclipse.finder.waits.Conditions.waitForEditor;
import static org.junit.Assert.assertNotNull;

import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.WaitForEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osate.ge.gef.Palette;
import org.osate.ge.gef.nodes.AgeShapeNode;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.tests.gef5.SWTBotAgeEditor;
import org.testfx.api.FxRobot;
import org.testfx.matcher.base.GeneralMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.service.query.NodeQuery;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;

// This is a rough proof of concept of a user interface test which mixed SWT and JavaFX.
// It demonstrates the ability to open a GEF5 based editor, activate the editor's SWT context menu, and click JavaFX nodes.
// Requirements and Assumptions:
// VM Argument: -Dosgi.framework.extensions=org.eclipse.fx.osgi
// Run in UI thread must be unchecked.
// Workspace is not cleared on each run.
// Workspace includes an AADL project named "test" which contains a "diagrams" folder which contains a "test.aadl_diagram" file.
public class ProofOfConceptGEF5Test {
	private static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		//bot.viewByTitle("Welcome").close();
	}

	@Test
	public void testGef5() {
		// Open the editor
		bot.viewByTitle("AADL Navigator").show();
		bot.tree().getTreeItem("test").expand().getNode("diagrams").expand().getNode("test.aadl_diagram").select()
		.contextMenu("Open With").menu("AADL Diagram Editor (GEF)").click();

		// Wait for the editor
		// TOOD: Look for existing matcher from swtbot
		bot.sleep(4000);
		final WaitForEditor waitForEditor = waitForEditor(new CustomMatcher<IEditorReference>("Find Editor") {
			@Override
			public boolean matches(Object item) {
				final IEditorReference ref = (IEditorReference)item;
				return ref.getName().contains("test.aadl_diagram");
			}
		});
		bot.waitUntilWidgetAppears(waitForEditor);

		final IEditorReference ref = waitForEditor.get(0);
		final SWTBotAgeEditor editorBot = new SWTBotAgeEditor(ref, bot);
		editorBot.contextMenu().menu("Test Menu Item").click();
		bot.sleep(4000);
		bot.shell("Test").activate();
		bot.button("OK").click();
		bot.sleep(4000);

		// TODO: should have an API similar to the other SWT bot classes which return proxy objects for controlling things.
		final IContentPart<? extends Node> dePart = findFirstDiagramElementContentPart(editorBot);
		assertNotNull(dePart);

		final AgeShapeNode shapeNode = (AgeShapeNode)dePart.getVisual();

		final FxRobot fxRobot = new FxRobot();

		//fxRobot.clickOn(query, buttons);
		//fxRobot.point(/query)
		final Scene scene = UIThreadRunnable.syncExec((Result<Scene>) () -> {
			return editorBot.getEditor().getCanvas().getScene();
		});

		final Matcher<Node> paletteMatcher = GeneralMatchers.typeSafeMatcher(Palette.class, "Palette", n -> true);
		final NodeQuery rootNodeQuery = fxRobot.from(scene.getRoot());
		final Node paletteCompartmentBtn = rootNodeQuery.lookup(paletteMatcher)
				.lookup(LabeledMatchers.hasText("Group 1")).query();
		fxRobot.clickOn(paletteCompartmentBtn, MouseButton.PRIMARY);

		// Select a node
		fxRobot.clickOn(shapeNode, MouseButton.PRIMARY);

		// Show context menu again.
		// Displayed text should indicate the selection.
		// TODO: For a real test, the results would be asserted
		editorBot.contextMenu().menu("Test Menu Item").click();
		bot.sleep(4000);
		bot.shell("Test").activate();
		bot.button("OK").click();
		bot.sleep(4000);
	}

	private static IContentPart<? extends Node> findFirstDiagramElementContentPart(final SWTBotAgeEditor editorBot) {
		for (final IVisualPart<? extends Node> vp : editorBot.getEditor().getContentViewer().getVisualPartMap()
				.values()) {
			if (vp instanceof IContentPart) {
				final Object content = ((IContentPart<? extends Node>) vp).getContent();
				if (content instanceof DiagramElement) {
					;
					return (IContentPart<? extends Node>) vp;
				}
			}
		}

		return null;
	}
}
