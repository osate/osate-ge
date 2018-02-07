package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.instance.ComponentInstance;

public class ComponentInstanceFilter implements ContentFilter {
	public final static String ID = "componentInstances";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Component Instances";
	}

	@Override
	public boolean isApplicable(final Object bo) {
		return bo instanceof ComponentInstance;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof ComponentInstance;
	}
}
