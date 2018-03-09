package org.osate.ge.operations;

import java.util.function.Consumer;

import org.osate.ge.internal.operations.DefaultOperationBuilder;

/**
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface Operation {
	public static Operation create(final Consumer<OperationBuilder<?>> operationBuilder) {
		final DefaultOperationBuilder rootOpBuilder = new DefaultOperationBuilder();
		operationBuilder.accept(rootOpBuilder);
		return rootOpBuilder.build();
	}
}
