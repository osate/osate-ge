package org.osate.ge.internal.diagram.runtime.filters;

import org.osate.ge.BusinessObjectContext;

public interface ContentFilter {
	String getId();

	String getDescription();

	boolean isApplicable(BusinessObjectContext boc);

	boolean test(Object bo);
}
