package org.osate.ge.ui.tools;

import org.eclipse.emf.common.command.AbstractCommand;

// An abstract command class which is always prepared for execution and cannot be undone.
public abstract class NonUndoableToolCommand extends AbstractCommand {
	public NonUndoableToolCommand() {
		super("Tool");
	}
	
	@Override
	public boolean prepare() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return false;
	}
	
	@Override
	public void redo() {}
}