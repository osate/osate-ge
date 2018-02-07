package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.ConnectionReference;

public class ConnectionReferenceFilter implements ContentFilter {
	public static final String ID = "connectionReferences";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Connection Instances";
	}

	@Override
	public boolean isApplicable(final Object bo) {
		return bo instanceof ComponentInstance;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof ConnectionReference;
	}
}
