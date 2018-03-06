package org.osate.ge.internal.operations;

import java.util.Objects;

import com.google.common.collect.ImmutableList;

// TODO: Rename
class SplitStep extends AbstractStep {
	private final ImmutableList<Step> steps;

	public SplitStep(final ImmutableList<Step> steps) {
		super(null);
		this.steps = Objects.requireNonNull(steps, "steps must not be null");
	}

	public ImmutableList<Step> getSteps() {
		return steps;
	}
}
