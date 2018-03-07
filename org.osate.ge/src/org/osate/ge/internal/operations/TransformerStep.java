package org.osate.ge.internal.operations;

import java.util.Objects;

import org.osate.ge.operations.Transformer;

class TransformerStep<PrevResultUserType, ResultUserType> extends AbstractStep<ResultUserType> {
	private final Transformer<PrevResultUserType, ResultUserType> handler;

	public TransformerStep(final Step<?> nextStep, final Transformer<PrevResultUserType, ResultUserType> handler) {
		super(nextStep);
		this.handler = Objects.requireNonNull(handler, "handler must not be null");
	}

	// TODO: Rename?
	public Transformer<PrevResultUserType, ResultUserType> getHandler() {
		return handler;
	}
}
