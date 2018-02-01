package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.ge.BusinessObjectContext;

public interface ContentFilter {
	String getId();

	String getName();

	boolean isApplicable(BusinessObjectContext boc);

	boolean test(Object bo);
}
