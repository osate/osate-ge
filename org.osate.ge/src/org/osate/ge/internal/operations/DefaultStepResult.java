package org.osate.ge.internal.operations;

import java.util.Objects;

import org.osate.ge.BusinessObjectContext;
import org.osate.ge.operations.StepResult;

import com.google.common.collect.ImmutableMultimap;

public class DefaultStepResult<UserResultType> implements StepResult<UserResultType> {
	private final UserResultType userValue;
	private final ImmutableMultimap<BusinessObjectContext, Object> containerToBoToShowMap;

	public DefaultStepResult(final UserResultType userValue,
			final ImmutableMultimap<BusinessObjectContext, Object> containerToBoToShowMap) {
		this.userValue = userValue;
		this.containerToBoToShowMap = Objects.requireNonNull(containerToBoToShowMap,
				"containerToBoToShowMap must not be null");
	}

	@Override
	public UserResultType getUserValue() {
		return userValue;
	}

	public ImmutableMultimap<BusinessObjectContext, Object> getContainerToBoToShowMap() {
		return containerToBoToShowMap;
	}
}
