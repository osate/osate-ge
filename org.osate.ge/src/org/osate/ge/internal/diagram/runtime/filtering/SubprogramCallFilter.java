package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.SubprogramCall;
import org.osate.aadl2.SubprogramCallSequence;
import org.osate.ge.BusinessObjectContext;

public class SubprogramCallFilter implements ContentFilter {
	public static final String ID = "subprogramCalls";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Subprogram Calls";
	}

	@Override
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof SubprogramCallSequence;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof SubprogramCall;
	}
}
