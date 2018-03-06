package org.osate.ge.internal.operations;

import java.util.Objects;

import org.osate.ge.operations.Transformer;

class TransformerStep<PrevReturnType, ReturnType> extends AbstractStep {
	private final Transformer<PrevReturnType, ReturnType> handler;

	public TransformerStep(final Step nextStep, final Transformer<PrevReturnType, ReturnType> handler) {
		super(nextStep);
		this.handler = Objects.requireNonNull(handler, "handler must not be null");
	}

	// TODO: Rename?
	public Transformer<PrevReturnType, ReturnType> getHandler() {
		return handler;
	}
}
