package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.Connection;
import org.osate.aadl2.Subcomponent;

public class ConnectionFilter implements ContentFilter {
	public static final String ID = "connections";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Connections";
	}

	@Override
	public boolean isApplicable(final Object bo) {
		return bo instanceof ComponentImplementation || bo instanceof Subcomponent;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof Connection;
	}
}
