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
abstract class AbstractStepBuilder<PrevResultUserType> implements OperationBuilder<PrevResultUserType> {
	private final List<AbstractStepBuilder<?>> nextStepBuilders = new ArrayList<>();

	@Override
	public <TagType, BusinessObjectType extends EObject, ResultUserType> OperationBuilder<ResultUserType> modify(final TagType tag,
			final Function<TagType, BusinessObjectType> tagToBoMapper,
			final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier) {
		return addNextStepBuilder(new ModificationStepBuilder<>(tag, tagToBoMapper, modifier));
	}

	@Override
	public <ResultUserType> OperationBuilder<ResultUserType> transform(
			final Transformer<PrevResultUserType, ResultUserType> stepHandler) {
		return addNextStepBuilder(new TransformerStepBuilder<>(stepHandler));
	}

	private <ResultUserType> OperationBuilder<ResultUserType> addNextStepBuilder(
			final AbstractStepBuilder<ResultUserType> nextStepBuilder) {
		nextStepBuilders.add(nextStepBuilder);
		return nextStepBuilder;
	}

	public final Step<?> build() {
		final Step<?> nextStep;
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
	protected abstract Step<?> buildThisStep(final Step<?> nextStep);
}