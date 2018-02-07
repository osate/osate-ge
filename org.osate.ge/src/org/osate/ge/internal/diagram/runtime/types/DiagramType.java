package org.osate.ge.internal.diagram.runtime.types;

import org.osate.ge.internal.diagram.runtime.filtering.ContentFilter;
import org.osate.ge.internal.diagram.runtime.filtering.ContentFilterProvider;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

public interface DiagramType {
	/**
	 * Returns a unique identifier for the diagram type.
	 * @return
	 */
	String getId();

	/**
	 * Returns the UI friendly name for the diagram type.
	 * @return
	 */
	String getName();

	/**
	 * Returns a string that provides a text description of the diagram type for display to the user.
	 * @return
	 */
	String getDescription();

	/**
	 * Returns whether the specified business object is a valid context for the diagram type.
	 * @param contextBo
	 * @return
	 */
	boolean isCompatibleWithContext(final Object contextBo);

	/**
	 * Returns the IDs of the default content filters for a business object. Must never return null.
	 * @param bo
	 * @return
	 */
	ImmutableSet<String> getDefaultContentFilters(Object bo);

	/**
	 * Returns the set of all AADL properties that are added to the diagram configuration by default. The user may choose to remove such properties.
	 * Changing this set will not result in changes to existing diagrams.
	 * @return
	 */
	ImmutableCollection<String> getDefaultAadlPropertyNames();

	/**
	 * Returns the default value for the connection primary labels visible configuration option.
	 * @return
	 */
	default boolean getDefaultConnectionPrimaryLabelsVisible() {
		return false;
	}

	/**
	 * Returns whether the diagram type is one that should be provided to the user as an option when creating a diagram.
	 * @return
	 */
	default boolean isUserCreatable() {
		return true;
	}

	public default ImmutableSet<ContentFilter> getApplicableDefaultContentFilters(final Object bo,
			final ContentFilterProvider contentFilterProvider) {
		return getDefaultContentFilters(bo).stream()
				.map(id -> contentFilterProvider.getContentFilterById(id).orElse(null))
				.filter(cf -> cf != null && cf.isApplicable(bo))
				.collect(ImmutableSet.toImmutableSet());
	}
}
