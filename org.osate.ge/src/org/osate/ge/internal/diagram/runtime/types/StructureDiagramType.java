package org.osate.ge.internal.diagram.runtime.types;

import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.ge.internal.diagram.runtime.filtering.ClassifierFilter;
import org.osate.ge.internal.diagram.runtime.filtering.ConnectionFilter;
import org.osate.ge.internal.diagram.runtime.filtering.ConnectionReferenceFilter;
import org.osate.ge.internal.diagram.runtime.filtering.FeatureInstanceFilter;
import org.osate.ge.internal.diagram.runtime.filtering.FlowSpecificationFilter;
import org.osate.ge.internal.diagram.runtime.filtering.ImplementationFeatureFilter;
import org.osate.ge.internal.diagram.runtime.filtering.TypeFeatureFilter;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

public class StructureDiagramType implements DiagramType {
	private final ImmutableSet<String> defaultPackageFilters = ImmutableSet.of(ClassifierFilter.ID);
	private final ImmutableSet<String> defaultClassifierOrSubcomponentFilters = ImmutableSet.of(TypeFeatureFilter.ID,
			ImplementationFeatureFilter.ID, ConnectionFilter.ID, FlowSpecificationFilter.ID);
	private final ImmutableSet<String> defaultComponentInstanceFilters = ImmutableSet.of(FeatureInstanceFilter.ID,
			ConnectionReferenceFilter.ID);

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
		} else if (bo instanceof Classifier || bo instanceof Subcomponent) {
			return defaultClassifierOrSubcomponentFilters;
		} else if (bo instanceof ComponentInstance) {
			return defaultComponentInstanceFilters;
		}

		return DiagramTypeUtil.getDefaultContentFilters(bo);
	}

	@Override
	public ImmutableCollection<String> getDefaultAadlPropertyNames() {
		return ImmutableSet.of();
	}
}
