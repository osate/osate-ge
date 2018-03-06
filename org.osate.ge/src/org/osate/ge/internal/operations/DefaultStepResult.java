package org.osate.ge.internal.operations;

import org.osate.ge.operations.StepResult;

public class DefaultStepResult<UserType> implements StepResult<UserType> {
	private final UserType userValue;

	public DefaultStepResult(final UserType userValue) {
		this.userValue = userValue;
	}

	@Override
	public UserType getValue() {
		return userValue;
	}
}
