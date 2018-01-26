package org.osate.ge.internal.diagram.types;

import org.osate.aadl2.ModeTransition;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.internal.diagram.runtime.BuiltinContentsFilter;
import org.osate.ge.internal.diagram.runtime.ContentsFilter;
import org.osate.ge.internal.diagram.runtime.DiagramType;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

public class LegacyDiagramType implements DiagramType {
	private ImmutableCollection<ContentsFilter> contentsFilters = ImmutableSet.of(
			BuiltinContentsFilter.ALLOW_FUNDAMENTAL, BuiltinContentsFilter.ALLOW_FUNDAMENTAL,
			BuiltinContentsFilter.ALLOW_ALL);

	@Override
	public String getName() {
		return "Legacy";
	}

	@Override
	public ContentsFilter getDefaultContentsFilter(Object bo) {
		if (bo instanceof Subcomponent || bo instanceof ModeTransition) {
			return BuiltinContentsFilter.ALLOW_TYPE;
		}

		return BuiltinContentsFilter.ALLOW_FUNDAMENTAL;
	}

	@Override
	public ImmutableCollection<ContentsFilter> getApplicableContentsFilters(Object bo) {
		return contentsFilters;
	}

	@Override
	public ImmutableCollection<String> getDefaultAadlPropertyNames() {
		return ImmutableSet.of();
	}

	@Override
	public boolean isUserCreatable() {
		return false;
	}
}
