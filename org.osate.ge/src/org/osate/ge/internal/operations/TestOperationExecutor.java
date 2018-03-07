package org.osate.ge.internal.operations;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

import org.osate.ge.operations.StepResult;
import org.osate.ge.operations.StepResultBuilder;

public class TestOperationExecutor {
	public void execute(final Step step) {
		// TODO: Create a linked hash set of unevaluated steps statements
		final LinkedHashSet<Supplier<StepResult>> unevaluatedStepConsumers = new LinkedHashSet<>();

		// TODO: Create a linked hash map or whatever is needed to feed into the modification service. Should evaluate previous steps as part of it.. All
		// unevaluated steps should be transform steps.
		// TODO: Execute the statements. Remove steps from a list of unevaluated transform steps.
		// TODO: Execute the modifiers.
//
//		final ListMultimap<Object, MappedObjectModifier> objectsToModifierMap = LinkedListMultimap.create();
//
//		final List<StepResult> allResults = new ArrayList<>(); // TODO: Rename.., Only non-null results
//		prepareToExecute(step, () -> null, allResults);

		// TODO: Do the actual execution...
	}

	// TODO: Type safety on steps and results
	private void prepareToExecute(final Step step, final Supplier<StepResult> prevResultSupplier,
			final Collection<StepResult> allResults) {

		final Supplier<StepResult> stepResultSupplier;

		// TODO: Need to collect results somehow

		// TODO: Allow null?
		if (step instanceof SplitStep) {
			for (final Step nextStep : ((SplitStep) step).getSteps()) {
				prepareToExecute(nextStep, prevResultSupplier, allResults);
			}
			stepResultSupplier = () -> null; // Split steps don't produce a result and shouldn't have next steps either.
		} else if (step instanceof ModificationStep) {
			// TODO
//			final ModificationStep ms = (ModificationStep) step;
//			class ModificationMappedObjectModifier implements MappedObjectModifier {
//				StepResult result;
//
//				@Override
//				public Object modify(Resource resource, Object boToModify, Object obj) {
//					result = ms.getModifier().modify(obj, (EObject) boToModify, prevResultSupplier.get());
//					if (result != null) {
//						allResults.add(result);
//					}
//
//					return null;
//				}
//			}
			// final ModificationMappedObjectModifier modifier = new ModificationMappedObjectModifier();
			// objectsToModifierMap.put(ms.getObject(), modifier);
			// TODO: Store modifier somewhere..

			// stepResultSupplier = () -> modifier.result;
			stepResultSupplier = null;

		} else if (step instanceof TransformerStep) {
			stepResultSupplier = new java.util.function.Supplier<StepResult>() {
				private boolean resultIsValid = false;
				private StepResult<?> result;

				@Override
				public StepResult get() {
					if (!resultIsValid) {
						final TransformerStep<?, ?> ts = (TransformerStep<?, ?>) step;
						result = ts.getHandler().apply(prevResultSupplier.get());
						resultIsValid = true;

						if (result != null) {
							allResults.add(result);
						}
					}

					return result;
				}
			};

			if (step.getNextStep() == null) {
				// TODO: Store supplier and make sure it is evaluated.
			}
		} else {
			throw new RuntimeException("Unexpected sttep: " + step);
		}

		if (step.getNextStep() != null) {
			prepareToExecute(step.getNextStep(), stepResultSupplier, allResults);
		}
	}

	public static void main(String[] args) {
		AbstractOperationBuilder<?> rootOpBuilder = new NoOpStepBuilder<>();
		rootOpBuilder.transform(arg -> StepResultBuilder.create(5).build())
		.transform(pr -> StepResultBuilder.create(pr.getValue() + 5).build());

		// TODO: Try it in a way that will produce split steps
		// TODO: Unit tests

		final Step firstStep = rootOpBuilder.build();

		System.out.println("First step: " + firstStep);

		final TestOperationExecutor executor = new TestOperationExecutor();
		executor.execute(firstStep);

	}

}
