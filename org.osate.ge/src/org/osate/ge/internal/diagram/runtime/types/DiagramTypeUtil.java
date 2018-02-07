package org.osate.ge.internal.diagram.runtime.types;

import org.osate.aadl2.ModeTransition;
import org.osate.ge.internal.diagram.runtime.filtering.ModeTransitionTriggerNameFilter;

import com.google.common.collect.ImmutableSet;

/**
 * Class containing methods which are shared between the various diagram types.
 *
 */
public class DiagramTypeUtil {
	private static final ImmutableSet<String> defaultModeTransitionFilters = ImmutableSet
			.of(ModeTransitionTriggerNameFilter.ID);

	static ImmutableSet<String> getDefaultContentFilters(final Object bo) {
		if (bo instanceof ModeTransition) {
			return DiagramTypeUtil.defaultModeTransitionFilters;
		}

		return ImmutableSet.of();
	}
}
