package org.osate.ge.internal.diagram.runtime.filtering;

import org.osate.aadl2.Classifier;
import org.osate.ge.BusinessObjectUtil;
import org.osate.ge.ContentFilter;

public class ClassifierFilter implements ContentFilter {
	public static final String ID = "classifiers";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "Classifiers";
	}

	@Override
	public boolean isApplicable(final Object bo) {
		return BusinessObjectUtil.isPackage(bo);
	}

	@Override
	public boolean test(Object bo) {
		return bo instanceof Classifier;
	}
}
