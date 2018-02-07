package org.osate.ge.internal.diagram.runtime.types;

import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ModeTransition;
import org.osate.ge.internal.diagram.runtime.filtering.ModeTransitionTriggerNameFilter;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

/**
 * Diagram type that includes minimal contents.
 *
 */
public class CustomDiagramType implements DiagramType {
	private final ImmutableSet<String> modeTransitionDefaultFilters = ImmutableSet
			.of(ModeTransitionTriggerNameFilter.ID);

	@Override
	public String getName() {
		return "Custom";
	}

	@Override
	public boolean isCompatibleWithContext(final Object contextBo) {
		return contextBo instanceof AadlPackage || contextBo instanceof Classifier;
	}

	@Override
	public ImmutableSet<String> getDefaultContentFilters(final Object bo) {
		if (bo instanceof ModeTransition) {
			return modeTransitionDefaultFilters;
		}

		return ImmutableSet.of();
	}

	@Override
	public ImmutableCollection<String> getDefaultAadlPropertyNames() {
		return ImmutableSet.of();
	}

	@Override
	public boolean getDefaultConnectionPrimaryLabelsVisible() {
		return true;
	}

	@Override
	public boolean isUserCreatable() {
		return false;
	}
}
