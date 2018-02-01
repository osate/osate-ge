package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.ge.BusinessObjectContext;

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
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof ComponentInstance;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof ComponentInstance;
	}
}
