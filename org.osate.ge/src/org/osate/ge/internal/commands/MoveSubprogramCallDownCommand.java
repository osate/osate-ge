package org.osate.ge.internal.commands;

import javax.inject.Named;

import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramCallSequence;
import org.osate.ge.di.GetLabel;
import org.osate.ge.di.Names;
import org.osate.ge.internal.di.GetBusinessObjectToModify;

public class MoveSubprogramCallDownCommand extends ReorderSubprogramCallCommand {
	public MoveSubprogramCallDownCommand() {
		super();
	}

	@GetLabel
	public String getLabel() {
		return "Move Down";
	}

	@GetBusinessObjectToModify
	public Object getBusinessObjectToModify(@Named(Names.BUSINESS_OBJECT) final SubprogramCall call) {
		System.err.println(call.eContainer() + " modifiedObject");
		return call.eContainer();
	}

	@Override
	protected int getNewIndex(final SubprogramCall call) {
		final SubprogramCallSequence cs = (SubprogramCallSequence)call.eContainer();
		final int currentIndex = cs.getOwnedSubprogramCalls().indexOf(call);
		return currentIndex == -1 ? -1 : currentIndex + 1;
	}
}
