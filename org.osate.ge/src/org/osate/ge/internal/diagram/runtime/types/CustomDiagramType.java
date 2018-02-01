package org.osate.ge.internal.diagram.runtime.types;

import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

/**
 * Diagram type that includes minimal contents.
 *
 */
public class CustomDiagramType implements DiagramType {
	@Override
	public String getName() {
		return "Custom";
	}

	@Override
	public boolean isCompatibleWithContext(final Object contextBo) {
		return contextBo instanceof AadlPackage || contextBo instanceof Classifier;
	}

	/*
	 * @Override
	 * public ContentsFilter getDefaultAutoContentsFilter(Object bo) {
	 * if (bo instanceof Subcomponent || bo instanceof ModeTransition) {
	 * return BuiltinContentsFilter.ALLOW_TYPE;
	 * }
	 *
	 * return BuiltinContentsFilter.ALLOW_FUNDAMENTAL;
	 * }
	 */

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
