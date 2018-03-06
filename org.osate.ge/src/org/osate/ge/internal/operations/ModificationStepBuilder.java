package org.osate.ge.internal.operations;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.operations.Modifier;

class ModificationStepBuilder<I, E extends EObject, PrevReturnType, ReturnType>
extends AbstractOperationBuilder<ReturnType> {
	private final I obj;
	private final Function<I, E> objToBoToModifyMapper;
	private final Modifier<E, PrevReturnType, ReturnType> modifier;

	public ModificationStepBuilder(final I obj, final Function<I, E> objToBoToModifyMapper,
			final Modifier<E, PrevReturnType, ReturnType> modifier) {
		this.obj = Objects.requireNonNull(obj, "obj must not be null");
		this.objToBoToModifyMapper = Objects.requireNonNull(objToBoToModifyMapper,
				"objToBoToModifyMapper must not be null");
		this.modifier = Objects.requireNonNull(modifier, "modifier must not be null");
	}

	@Override
	protected Step buildThisStep(final Step nextStep) {
		return new ModificationStep<>(nextStep, obj, objToBoToModifyMapper, modifier);
	}
}
