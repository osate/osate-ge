package org.osate.ge.internal.diagram.runtime.types;

import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ModeTransition;
import org.osate.aadl2.Subcomponent;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

public class LegacyDiagramType implements DiagramType {
	private ImmutableCollection<ContentsFilter> contentsFilters = ImmutableSet.of(
			BuiltinContentsFilter.ALLOW_FUNDAMENTAL, BuiltinContentsFilter.ALLOW_TYPE,
			BuiltinContentsFilter.ALLOW_ALL);

	@Override
	public String getName() {
		return "Legacy";
	}

	@Override
	public boolean isCompatibleWithContext(final Object contextBo) {
		return contextBo instanceof AadlPackage || contextBo instanceof Classifier;
	}

	@Override
	public ContentsFilter getDefaultAutoContentsFilter(Object bo) {
		if (bo instanceof Subcomponent || bo instanceof ModeTransition) {
			return BuiltinContentsFilter.ALLOW_TYPE;
		}

		return BuiltinContentsFilter.ALLOW_FUNDAMENTAL;
	}

	@Override
	public ContentsFilter getContentsFilter(final String contentsFilterId) {
		return BuiltinContentsFilter.getById(contentsFilterId);
	}

	@Override
	public ImmutableCollection<ContentsFilter> getApplicableAutoContentsFilters(Object bo) {
		// TODO: Implement
//		return (newFilterValue != BuiltinContentsFilter.ALLOW_TYPE || (bo instanceof Subcomponent || bo instanceof Classifier)) &&
//				!(bo instanceof PropertyValueGroup); // Don't allow setting the content filter for property values
		return contentsFilters;
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
