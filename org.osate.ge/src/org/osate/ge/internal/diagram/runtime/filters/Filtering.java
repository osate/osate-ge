package org.osate.ge.internal.diagram.runtime.filters;

import org.osate.ge.internal.model.PropertyValueGroup;
import org.osate.ge.internal.model.SubprogramCallOrder;
import org.osate.ge.internal.model.Tag;

public class Filtering {
	public static boolean isFundamental(final Object bo) {
		if(bo instanceof Tag) {
			final Tag tag = (Tag) bo;
			if (tag.key.equals(Tag.KEY_UNIDIRECTIONAL)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isConfigurable(final Object bo) {
		if (bo instanceof SubprogramCallOrder || bo instanceof PropertyValueGroup) {
			return false;
		}

		return !isFundamental(bo);
	}
}
