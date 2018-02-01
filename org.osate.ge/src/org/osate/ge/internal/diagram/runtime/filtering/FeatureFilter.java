package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.Classifier;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.BusinessObjectContext;

public class FeatureFilter implements ContentFilter {
	public static final String ID = "typeFeatures";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Features (Type)";
	}

	@Override
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof Classifier || boc.getBusinessObject() instanceof Subcomponent
				|| boc.getBusinessObject() instanceof FeatureGroup;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof Feature;
	}
}
