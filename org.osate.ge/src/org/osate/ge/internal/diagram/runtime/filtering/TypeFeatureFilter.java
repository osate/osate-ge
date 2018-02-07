package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.Classifier;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.Subcomponent;

public class TypeFeatureFilter implements ContentFilter {
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
	public boolean isApplicable(final Object bo) {
		return bo instanceof Classifier || bo instanceof Subcomponent
				|| bo instanceof FeatureGroup;
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof Feature;
	}
}
