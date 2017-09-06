package org.osate.ge.tests.gef5;

import java.util.Objects;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.waits.WaitForObjectCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.osate.ge.gef.ui.editor.AgeEditor;

public class SWTBotAgeEditor {
	private final AgeEditor editor;

	public SWTBotAgeEditor(final IEditorReference editorReference, final SWTWorkbenchBot bot)
			throws WidgetNotFoundException {

		editor = UIThreadRunnable.syncExec((Result<AgeEditor>) () -> {
			final IEditorPart editorPart = editorReference.getEditor(true);
			return editorPart.getAdapter(AgeEditor.class);
		});

		Objects.requireNonNull(editor, "Unable to retrieve editor form: " + editorReference);
		Objects.requireNonNull(editor.getCanvas(), "Canvas is null");
	}

	public SWTBotRootMenu contextMenu() {
		WaitForObjectCondition<Menu> waitForMenu = org.eclipse.swtbot.swt.finder.waits.Conditions
				.waitForPopupMenu(editor.getCanvas());
		new SWTBot().waitUntilWidgetAppears(waitForMenu);
		return new SWTBotRootMenu(waitForMenu.get(0));
	}

	// TODO: Consider not exposing this.
	public AgeEditor getEditor() {
		return editor;
	}
}