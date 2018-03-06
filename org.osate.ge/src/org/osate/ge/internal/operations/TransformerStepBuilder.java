package org.osate.ge.internal.operations;

import java.util.Objects;

import org.osate.ge.operations.Transformer;

class TransformerStepBuilder<PrevReturnType, ReturnType> extends AbstractOperationBuilder<ReturnType> {
	private final Transformer<PrevReturnType, ReturnType> handler;

	public TransformerStepBuilder(final Transformer<PrevReturnType, ReturnType> handler) {
		this.handler = Objects.requireNonNull(handler, "handler must not be null");
	}

	@Override
	protected Step buildThisStep(final Step nextStep) {
		return new TransformerStep<PrevReturnType, ReturnType>(nextStep, handler);
	}
}
