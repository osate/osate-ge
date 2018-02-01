package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.FeatureCategory;
import org.osate.aadl2.instance.FeatureInstance;
import org.osate.ge.BusinessObjectContext;

public class FeatureInstanceFilter implements ContentFilter {
	public static final String ID = "featureInstances";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Feature Instances";
	}

	@Override
	public boolean isApplicable(final BusinessObjectContext boc) {
		return boc.getBusinessObject() instanceof ComponentInstance
				|| (boc.getBusinessObject() instanceof FeatureInstance
						&& ((FeatureInstance) boc.getBusinessObject()).getCategory() == FeatureCategory.FEATURE_GROUP);
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof FeatureInstance;
	}
}
