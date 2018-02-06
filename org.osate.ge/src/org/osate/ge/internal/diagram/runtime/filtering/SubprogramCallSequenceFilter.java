package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.SubprogramCallSequence;
import org.osate.ge.BusinessObjectContext;

public class SubprogramCallSequenceFilter implements ContentFilter {
	public static final String ID = "subprogramCallSequences";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Subprogram Call Sequences";
	}

	@Override
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof ComponentImplementation
				|| boc.getBusinessObject() instanceof Subcomponent;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof SubprogramCallSequence;
	}
}
