package org.osate.ge.operations;

import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;

// TODO: Rename
public interface OperationBuilder<PrevReturnType> {
	<I, E extends EObject, ReturnType> OperationBuilder<ReturnType> modify(I obj,
			Function<I, E> objToBoToModifyMapper,
			Modifier<E, PrevReturnType, ReturnType> modifier);

	// TODO: Rename.
	<ReturnType> OperationBuilder<ReturnType> transform(
			Transformer<PrevReturnType, ReturnType> stepHandler);
}
