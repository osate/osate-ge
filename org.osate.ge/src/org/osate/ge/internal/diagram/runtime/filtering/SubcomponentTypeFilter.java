package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.internal.model.Tag;

public class SubcomponentTypeFilter implements ContentFilter {
	public static final String ID = "subcomponentType";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Subcomponent Type";
	}

	@Override
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof Subcomponent;
	}

	@Override
	public boolean test(Object bo) {
		if (bo instanceof Tag) {
			final Tag tag = (Tag) bo;
			if (tag.key.equals(Tag.KEY_SUBCOMPONENT_TYPE)) {
				return true;
			}
		}

		return false;
	}
}
