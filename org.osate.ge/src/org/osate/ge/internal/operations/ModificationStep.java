package org.osate.ge.internal.operations;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.operations.Modifier;

class ModificationStep<I, E extends EObject, PrevReturnType, ReturnType> extends AbstractStep {
	private final I obj;
	private final Function<I, E> objToBoToModifyMapper;
	private final Modifier<E, PrevReturnType, ReturnType> modifier;

	public ModificationStep(final Step nextStep, final I obj, final Function<I, E> objToBoToModifyMapper,
			final Modifier<E, PrevReturnType, ReturnType> modifier) {
		super(nextStep);
		this.obj = Objects.requireNonNull(obj, "obj must not be null");
		this.objToBoToModifyMapper = Objects.requireNonNull(objToBoToModifyMapper,
				"objToBoToModifyMapper must not be null");
		this.modifier = Objects.requireNonNull(modifier, "modifier must not be null");
	}

	// TODO: REname

	public final I getObject() {
		return obj;
	}

	public final Function<I, E> getObjectToBoToModifyMapper() {
		return objToBoToModifyMapper;
	}

	public final Modifier<E, PrevReturnType, ReturnType> getModifier() {
		return modifier;
	}
}
