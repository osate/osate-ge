package org.osate.ge.internal.operations;

public class NoOpStepBuilder<PrevReturnType> extends AbstractOperationBuilder<PrevReturnType> {
	// build() for this step builder will return null if the next step is null.

	@Override
	protected Step buildThisStep(final Step nextStep) {
		// TODO: Consider returning a dummy step if nextStep is null?
		return nextStep;
	}
}
