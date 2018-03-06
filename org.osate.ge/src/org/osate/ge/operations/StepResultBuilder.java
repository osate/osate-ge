package org.osate.ge.operations;

import org.osate.ge.internal.operations.DefaultStepResult;

// TODO: is a builder appropriate?
public class StepResultBuilder<UserValue> {
	private UserValue userValue;

	public StepResultBuilder(final UserValue userValue) {
		this.userValue = userValue;
	}

	public static <UserValue> StepResultBuilder<UserValue> create(final UserValue userValue) {
		return new StepResultBuilder<>(userValue);
	}
	// BO

	// Add to container

	public StepResult<UserValue> build() {
		return new DefaultStepResult<>(userValue);
	}
}
