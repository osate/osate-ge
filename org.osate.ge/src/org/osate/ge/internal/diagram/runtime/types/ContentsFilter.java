package org.osate.ge.internal.diagram.runtime.types;

import java.util.function.Predicate;

public interface ContentsFilter extends Predicate<Object> {
	String id();
	String description();

	public final static String ALLOW_FUNDAMENTAL_ID = "allow_fundamental";
	public final static String ALLOW_TYPE_ID = "allow_type";
	public final static String ALLOW_ALL_ID = "allow_all";
}
