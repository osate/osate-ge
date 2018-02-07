package org.osate.ge.internal.diagram.runtime.types;

import java.util.Optional;

import com.google.common.collect.ImmutableCollection;

public interface DiagramTypeProvider {
	ImmutableCollection<DiagramType> getDiagramTypes();

	default Optional<DiagramType> getDiagramTypeById(String id) {
		for (final DiagramType diagramType : getDiagramTypes()) {
			if (diagramType.getId().equals(id)) {
				return Optional.of(diagramType);
			}
		}

		return Optional.empty();
	}
}
