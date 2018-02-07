package org.osate.ge.internal.diagram.runtime.types;

import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.ge.internal.diagram.runtime.filtering.ClassifierFilter;
import org.osate.ge.internal.diagram.runtime.filtering.GeneralizationFilter;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

public class PackageDiagramType implements DiagramType {
	private final ImmutableSet<String> defaultPackageFilters = ImmutableSet.of(ClassifierFilter.ID);
	private final ImmutableSet<String> defaultClassifierFilters = ImmutableSet.of(GeneralizationFilter.ID);

	@Override
	public String getId() {
		return "package";
	}

	@Override
	public String getName() {
		return "Package Diagram";
	}

	@Override
	public boolean isCompatibleWithContext(final Object contextBo) {
		return contextBo instanceof AadlPackage;
	}

	@Override
	public ImmutableSet<String> getDefaultContentFilters(final Object bo) {
		if (bo instanceof AadlPackage) {
			return defaultPackageFilters;
		} else if (bo instanceof Classifier) {
			return defaultClassifierFilters;
		}

		return DiagramTypeUtil.getDefaultContentFilters(bo);
	}

	@Override
	public ImmutableCollection<String> getDefaultAadlPropertyNames() {
		return ImmutableSet.of();
	}
}
