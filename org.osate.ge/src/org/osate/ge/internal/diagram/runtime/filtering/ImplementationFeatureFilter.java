package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.InternalFeature;
import org.osate.aadl2.ProcessorFeature;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectContext;

public class ImplementationFeatureFilter implements ContentFilter {
	public static final String ID = "implementationFeatures";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Features (Implementation)";
	}

	@Override
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof ComponentImplementation
				|| boc.getBusinessObject() instanceof Subcomponent;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof ProcessorFeature || bo instanceof InternalFeature;
	}
}
