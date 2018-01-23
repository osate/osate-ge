package org.osate.ge.internal;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.services.AadlModificationService;

// Internal API used for multi-step create operations.
public interface CreateOperation {
	public static class CreateStepResult {
		public final DiagramNode container;
		public final Object newBo;

		public CreateStepResult(final DiagramNode container, final Object newBo) {
			this.container = Objects.requireNonNull(container, "container must not be null");
			this.newBo = Objects.requireNonNull(newBo, "newBo must not be null");
		}
	}

	void addStep(final EObject objToModify, AadlModificationService.Modifier<EObject, CreateStepResult> modifier);
}