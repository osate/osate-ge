package org.osate.ge;

import java.util.function.Predicate;

import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.ge.internal.businessObjectHandlers.InternalClassifierEditingUtil;
import org.osate.ge.operations.OperationBuilder;
import org.osate.ge.operations.StepResultBuilder;

// TODO: Rename
public class ClassifierEditingUtil {
	// TODO: Rename. These methods are designed specifically for creating children

	// TODO: Create variants without filters

	public static final OperationBuilder<ComponentImplementation> selectComponentImplementation(
			final OperationBuilder<?> operation, final Object targetBo,
			final Predicate<ComponentImplementation> filter) {
		return operation.supply(() -> {
			if (InternalClassifierEditingUtil.showMessageIfSubcomponentOrFeatureGroupWithoutClassifier(targetBo,
					"Set a component implementation before creating this element.")) {
				return StepResultBuilder.buildAbort();
			}

			// Determine which classifier should own the new element
			final ComponentImplementation selectedClassifier = InternalClassifierEditingUtil.getClassifierToModify(
					InternalClassifierEditingUtil.getPotentialComponentImplementations(targetBo, filter));
			if (selectedClassifier == null) {
				return StepResultBuilder.buildAbort();
			}

			return StepResultBuilder.build(selectedClassifier);
		});
	}

	// TODO: Rename
	public static final OperationBuilder<ComponentType> selectComponentType(final OperationBuilder<?> operation,
			final Object targetBo, final Predicate<ComponentType> filter) {
		return operation.supply(() -> {
			if (InternalClassifierEditingUtil.showMessageIfSubcomponentOrFeatureGroupWithoutClassifier(targetBo,
					"Set a component type before creating this element.")) {
				return StepResultBuilder.buildAbort();
			}

			// Determine which classifier should own the new element
			final ComponentType selectedClassifier = InternalClassifierEditingUtil
					.getClassifierToModify(InternalClassifierEditingUtil.getPotentialComponentTypes(targetBo, filter));
			if (selectedClassifier == null) {
				return StepResultBuilder.buildAbort();
			}

			return StepResultBuilder.build(selectedClassifier);
		});
	}

	public static final OperationBuilder<ComponentClassifier> selectComponentClassifier(
			final OperationBuilder<?> operation, final Object targetBo,
			final Predicate<ComponentClassifier> filter) {
		return operation.supply(() -> {
			if (InternalClassifierEditingUtil.showMessageIfSubcomponentOrFeatureGroupWithoutClassifier(targetBo,
					"Set a component classifier before creating this element.")) {
				return StepResultBuilder.buildAbort();
			}

			// Determine which classifier should own the new element
			final ComponentClassifier selectedClassifier = InternalClassifierEditingUtil
					.getClassifierToModify(
							InternalClassifierEditingUtil.getPotentialComponentClassifiers(targetBo, filter));
			if (selectedClassifier == null) {
				return StepResultBuilder.buildAbort();
			}

			return StepResultBuilder.build(selectedClassifier);
		});
	}

	// TODO: Document purpose and why it allows subcomponents without classifiers
	public static final boolean canCreateInComponentImplementation(final Object bo,
			final Predicate<ComponentImplementation> filter) {
		return InternalClassifierEditingUtil.isSubcomponentWithoutClassifier(bo)
				|| InternalClassifierEditingUtil.getPotentialComponentImplementations(bo, filter).size() > 0;
	}

	public static final boolean canCreateInComponentType(final Object bo, final Predicate<ComponentType> filter) {
		return InternalClassifierEditingUtil.isSubcomponentWithoutClassifier(bo)
				|| InternalClassifierEditingUtil.getPotentialComponentTypes(bo, filter).size() > 0;
	}

	public static final boolean canCreateInComponentClassifiers(final Object bo,
			final Predicate<ComponentClassifier> filter) {
		return InternalClassifierEditingUtil.isSubcomponentWithoutClassifier(bo)
				|| InternalClassifierEditingUtil.getPotentialComponentClassifiers(bo, filter).size() > 0;
	}
}
