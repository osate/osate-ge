package org.osate.ge.internal.diagram.runtime;

import com.google.common.collect.ImmutableCollection;

public interface DiagramType {
	/**
	 * Returns the UI friendly name for the diagram type.
	 * @return
	 */
	String getName();

	/**
	 * Returns the default contents filter for a business object.
	 * @param bo
	 * @return
	 */
	ContentsFilter getDefaultContentsFilter(Object bo);

	/**
	 * Returns the contents filters which are applicable to the specified business object.
	 * @param bo
	 * @return
	 */
	ImmutableCollection<ContentsFilter> getApplicableContentsFilters(Object bo);

	/**
	 * Returns the set of all AADL properties that are added to the diagram configuration by default. The user may choose to remove such properties.
	 * Changing this set will not result in changes to existing diagrams.
	 * @return
	 */
	ImmutableCollection<String> getDefaultAadlPropertyNames();

	/**
	 * Returns whether the diagram type is one that should be provided to the user as an option when creating a diagram.
	 * @return
	 */
	default boolean isUserCreatable() {
		return true;
	}
}
