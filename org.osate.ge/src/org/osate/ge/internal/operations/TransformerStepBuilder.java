package org.osate.ge.internal.operations;

import java.util.Objects;

import org.osate.ge.operations.Transformer;

class TransformerStepBuilder<PrevResultUserType, ResultUserType> extends AbstractStepBuilder<ResultUserType> {
	private final Transformer<PrevResultUserType, ResultUserType> handler;

	public TransformerStepBuilder(final Transformer<PrevResultUserType, ResultUserType> handler) {
		this.handler = Objects.requireNonNull(handler, "handler must not be null");
	}

	@Override
	protected Step<?> buildThisStep(final Step<?> nextStep) {
		return new TransformerStep<PrevResultUserType, ResultUserType>(nextStep, handler);
	}
}
