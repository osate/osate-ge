package org.osate.ge.internal.diagram.runtime.filtering;

public interface ContentFilter {
	String getId();

	String getName();

	boolean isApplicable(Object bo);

	boolean test(Object bo);
}
