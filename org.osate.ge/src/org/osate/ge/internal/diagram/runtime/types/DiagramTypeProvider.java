package org.osate.ge.internal.diagram.runtime.types;

import java.util.Optional;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public interface DiagramTypeProvider {
	ImmutableCollection<DiagramType> getDiagramTypes();

	default Optional<DiagramType> getDiagramTypeById(final String id) {
		for (final DiagramType diagramType : getDiagramTypes()) {
			if (diagramType.getId().equals(id)) {
				return Optional.of(diagramType);
			}
		}

		return Optional.empty();
	}

	default ImmutableCollection<DiagramType> getApplicableDiagramTypes(final Object contextBo) {
		return getDiagramTypes().stream().filter(dt -> dt.isCompatibleWithContext(contextBo))
				.collect(ImmutableList.toImmutableList());
	}
}
