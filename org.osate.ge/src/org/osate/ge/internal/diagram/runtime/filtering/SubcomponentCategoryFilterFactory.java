package org.osate.ge.internal.diagram.runtime.filtering;

import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.osate.aadl2.ComponentCategory;

/**
 * Factory for creating a filter for a specific subcomponent category.
 *
 */
public class SubcomponentCategoryFilterFactory implements IExecutableExtensionFactory, IExecutableExtension {
	private ComponentCategory category;

	@Override
	public Object create() throws CoreException {
		Objects.requireNonNull(category, "category must not be null");
		return new SubcomponentCategoryFilter(category);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		category = Objects.requireNonNull(ComponentCategory.getByName(data.toString()),
				"Unable to find component category: " + data);
	}

}
