package org.osate.ge.internal.operations;

public class AbstractStep implements Step {
	private final Step nextStep;

	public AbstractStep(final Step nextStep) {
		this.nextStep = nextStep;
	}

	@Override
	public Step getNextStep() {
		return nextStep;
	}

}
