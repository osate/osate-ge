package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.Mode;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectContext;

public class ModeFilter implements ContentFilter {
	public static final String ID = "modes";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Modes";
	}

	@Override
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof ComponentClassifier
				|| boc.getBusinessObject() instanceof Subcomponent;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof Mode;
	}
}
