package org.osate.ge.operations;

import org.eclipse.emf.ecore.EObject;

public interface Modifier<E extends EObject, PrevReturnType, ReturnType> {
	StepResult<ReturnType> modify(Object obj, E boToModify, StepResult<PrevReturnType> prevResult);
}
