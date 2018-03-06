package org.osate.ge.internal.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.operations.Modifier;
import org.osate.ge.operations.OperationBuilder;
import org.osate.ge.operations.Transformer;

import com.google.common.collect.ImmutableList;

// TODO: Rename
abstract class AbstractOperationBuilder<PrevReturnType> implements OperationBuilder<PrevReturnType> {
	private final List<AbstractOperationBuilder<?>> nextStepBuilders = new ArrayList<>();

	@Override
	public <I, E extends EObject, ReturnType> OperationBuilder<ReturnType> modify(I obj,
			Function<I, E> objToBoToModifyMapper,
			Modifier<E, PrevReturnType, ReturnType> modifier) {
		return addNextStepBuilder(new ModificationStepBuilder<>(obj, objToBoToModifyMapper, modifier));
	}

	@Override
	public <ReturnType> OperationBuilder<ReturnType> transform(
			final Transformer<PrevReturnType, ReturnType> stepHandler) {
		return addNextStepBuilder(new TransformerStepBuilder<>(stepHandler));
	}

	private <ReturnType> OperationBuilder<ReturnType> addNextStepBuilder(
			AbstractOperationBuilder<ReturnType> nextStepBuilder) {
		nextStepBuilders.add(nextStepBuilder);
		return nextStepBuilder;
	}

	public final Step build() {
		final Step nextStep;
		if (nextStepBuilders.isEmpty()) {
			nextStep = null;
		} else if (nextStepBuilders.size() == 1) {
			nextStep = nextStepBuilders.get(0).build();
		} else {
			nextStep = new SplitStep(
					nextStepBuilders.stream().map(b -> b.build()).collect(ImmutableList.toImmutableList()));
		}

		return buildThisStep(nextStep);
	}

	// TODO: Rename
	// nextStep may be null
	protected abstract Step buildThisStep(final Step nextStep);
}