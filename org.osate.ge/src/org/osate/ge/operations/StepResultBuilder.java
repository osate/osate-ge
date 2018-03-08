package org.osate.ge.operations;

import java.util.Objects;

import org.osate.ge.BusinessObjectContext;
import org.osate.ge.internal.operations.DefaultStepResult;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class StepResultBuilder<UserValueType> {
	private UserValueType userValue;
	private Multimap<BusinessObjectContext, Object> containerToBoToShowMap = ArrayListMultimap.create();
	private boolean aborted = false;

	private StepResultBuilder(final UserValueType userValue) {
		this.userValue = userValue;
	}

	// TODO: Rename
	public StepResultBuilder<UserValueType> showNewBusinessObject(final BusinessObjectContext container,
			final Object bo) {
		Objects.requireNonNull(container, "container must not be null");
		Objects.requireNonNull(bo, "bo must not be null");
		containerToBoToShowMap.put(container, bo);
		return this;
	}

	public StepResultBuilder<UserValueType> abort() {
		aborted = true;
		return this;
	}

	public static <UserValue> StepResultBuilder<UserValue> create(final UserValue userValue) {
		return new StepResultBuilder<>(userValue);
	}

	public static StepResultBuilder<Void> create() {
		return new StepResultBuilder<>(null);
	}

	// TODO: Methods to provide hints to add to container. Support multiple values per step.

	public StepResult<UserValueType> build() {
		return new DefaultStepResult<>(userValue, ImmutableMultimap.copyOf(containerToBoToShowMap), aborted);
	}

	@SuppressWarnings("unchecked")
	public static <UserValueType> StepResult<UserValueType> buildAbort() {
		return (StepResult<UserValueType>) create().abort().build();
	}

	public static <UserValueType> StepResult<UserValueType> build(final UserValueType userValue) {
		return create(userValue).build();
	}
}
