package org.osate.ge.internal.graphiti.features;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.internal.CreateOperation;
import org.osate.ge.internal.services.AadlModificationService;

class SimpleCreateOperation implements CreateOperation {
	// Maps from the object being modified to the modifier
	final List<AadlModificationService.Modification<EObject, EObject>> stepMap = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Override
	public <E extends EObject> void addStep(final E objToModify, CreateStepHandler<E> stepHandler) {
//		stepMap.put(objToModify,
//				(resource, bo, obj) -> stepHandler.modify(resource, (E) bo));
		// TODO: Replace with new operation system
	}

	public final boolean isEmpty() {
		return stepMap.isEmpty();
	}
}