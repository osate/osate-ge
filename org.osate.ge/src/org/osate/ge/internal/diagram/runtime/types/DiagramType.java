package org.osate.ge.internal.diagram.runtime.types;

import com.google.common.collect.ImmutableCollection;

public interface DiagramType {
	/**
	 * Returns the UI friendly name for the diagram type.
	 * @return
	 */
	String getName();

	/**
	 * Returns whether the specified business object is a valid context for the diagram type.
	 * @param contextBo
	 * @return
	 */
	boolean isCompatibleWithContext(final Object contextBo);

	/**
	 * Returns the default auto contents filter for a business object. Must never return null.
	 * @param bo
	 * @return
	 */
	ContentsFilter getDefaultAutoContentsFilter(Object bo);

	/**
	 * Returns the contents filters which are applicable to the specified business object.
	 * @param bo
	 * @return
	 */
	ImmutableCollection<ContentsFilter> getApplicableAutoContentsFilters(Object bo);

	/**
	 * Returns the contents filter with the specified contents filter id. The id's are only guaranteed to be unique within the scope of this diagram type.
	 * Returns null if the contents filter could not be returned.
	 */
	ContentsFilter getContentsFilter(final String contentsFilterId);

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
}
