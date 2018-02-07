package org.osate.ge.internal.diagram.runtime.filtering;

import java.util.Objects;

import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.ge.internal.util.StringUtil;

public class SubcomponentCategoryFilter implements ContentFilter {
	private final ComponentCategory category;

	public SubcomponentCategoryFilter(final ComponentCategory category) {
		this.category = Objects.requireNonNull(category, "category must not be null");
	}

	@Override
	public String getId() {
		return getId(category);
	}

	@Override
	public String getName() {
		return "Subcomponents - " + StringUtil.capitalize(category.getName());
	}

	@Override
	public boolean isApplicable(final Object bo) {
		return bo instanceof ComponentImplementation || bo instanceof Subcomponent || bo instanceof ComponentInstance;
	}

	@Override
	public boolean test(Object bo) {
		return (bo instanceof Subcomponent && ((Subcomponent) bo).getCategory() == category)
				|| (bo instanceof ComponentInstance && ((ComponentInstance) bo).getCategory() == category);
	}

	public static String getId(final ComponentCategory category) {
		return category.getName() + "Subcomponents";
	}

}
