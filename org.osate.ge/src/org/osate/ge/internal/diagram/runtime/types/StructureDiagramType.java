package org.osate.ge.internal.diagram.runtime.types;

import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.ge.internal.diagram.runtime.filtering.ClassifierFilter;
import org.osate.ge.internal.diagram.runtime.filtering.ConnectionFilter;
import org.osate.ge.internal.diagram.runtime.filtering.FeatureFilter;
import org.osate.ge.internal.diagram.runtime.filtering.FlowSpecificationFilter;
import org.osate.ge.internal.diagram.runtime.filtering.InternalFeatureFilter;
import org.osate.ge.internal.diagram.runtime.filtering.ProcessorFeatureFilter;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

public class StructureDiagramType implements DiagramType {
	private final ImmutableSet<String> defaultPackageFilters = ImmutableSet.of(ClassifierFilter.ID);
	private final ImmutableSet<String> defaultClassifierOrSubcomponentFilters = ImmutableSet.of(FeatureFilter.ID,
			InternalFeatureFilter.ID, ProcessorFeatureFilter.ID, ConnectionFilter.ID, FlowSpecificationFilter.ID);

	@Override
	public String getId() {
		return "structure";
	}

	@Override
	public String getName() {
		return "Structure Diagram";
	}

	@Override
	public boolean isCompatibleWithContext(final Object contextBo) {
		return contextBo instanceof AadlPackage || contextBo instanceof Classifier
				|| contextBo instanceof SystemInstance;
	}

	@Override
	public ImmutableSet<String> getDefaultContentFilters(final Object bo) {
		if (bo instanceof AadlPackage) {
			return defaultPackageFilters;
		} else if (bo instanceof Classifier || bo instanceof Subcomponent || bo instanceof ComponentInstance) {
			return defaultClassifierOrSubcomponentFilters;
		}

		return DiagramTypeUtil.getDefaultContentFilters(bo);
	}

	@Override
	public ImmutableCollection<String> getDefaultAadlPropertyNames() {
		return ImmutableSet.of();
	}
}
