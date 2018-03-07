package org.osate.ge.internal.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.internal.services.AadlModificationService;
import org.osate.ge.operations.OperationBuilder;
import org.osate.ge.operations.StepResult;
import org.osate.ge.operations.StepResultBuilder;

public class OperationExecutor {
	private final AadlModificationService modificationService;

	public static interface ResultsProcessor {
		void processResults(final List<StepResult<?>> results);
	}

	public OperationExecutor(final AadlModificationService modificationService) {
		this.modificationService = Objects.requireNonNull(modificationService, "modificationService must not be null");
	}

	/**
	 * @param step
	 * @param resultsProcessor handles the results of the operation. Will not be called if any of the modifications fail.
	 */
	public void execute(final Step<?> step, final ResultsProcessor resultsProcessor) {
		Objects.requireNonNull(resultsProcessor, "resultsProcessor must not be null");

		if (step == null) {
			return;
		}

		final LinkedHashSet<Supplier<? extends StepResult<?>>> pendingStepConsumers = new LinkedHashSet<>();
		final List<AadlModificationService.Modification<?, ?>> modifications = new ArrayList<>();
		final List<StepResult<?>> allResults = new ArrayList<>(); // Will only contain non-null results
		prepareToExecute(step, () -> StepResultBuilder.create().build(), allResults, modifications,
				pendingStepConsumers);

		if (modifications.isEmpty()) {
			finishExecution(resultsProcessor, pendingStepConsumers, allResults);
		} else {
			modificationService.modify(modifications, allSuccessful -> {
				if (allSuccessful) {
					finishExecution(resultsProcessor, pendingStepConsumers, allResults);
				}
			});
		}
	}

	/**
	 * Finishes executing an operation. Ensures that pending step suppliers are called and the results processor is called
	 * @param resultsProcessor
	 * @param pendingStepConsumers
	 * @param allResults
	 */
	private void finishExecution(final ResultsProcessor resultsProcessor,
			LinkedHashSet<Supplier<? extends StepResult<?>>> pendingStepConsumers,
			final List<StepResult<?>> allResults) {
		// Evaluate steps which have not been evaluated.
		while (!pendingStepConsumers.isEmpty()) {
			pendingStepConsumers.iterator().next().get();
		}

		resultsProcessor.processResults(allResults);
	}
	/**
	 *
	 * @param step is the step to process. The steps and all subsequent steps will be processed.
	 * @param prevResultSupplier the supplier for the value of the previous step. Must not be null.
	 * @param allResults a list to which the results of each step should be added. This list will be modified when the actual execution is performed. Will only contain non-null results.
	 * @param modifications a list of modifications that need to be executed by the the AADL modification service.
	 * @param uncalledStepResultSuppliers a list which will contain consumers generated by steps which have not been evaluated. This list will be modified as the consumers are executed.
	 */
	private <PrevResultUserType, ResultUserType> void prepareToExecute(final Step<ResultUserType> step,
			final Supplier<StepResult<PrevResultUserType>> prevResultSupplier,
			final Collection<StepResult<?>> allResults,
			final List<AadlModificationService.Modification<?, ?>> modifications,
			final Collection<Supplier<? extends StepResult<?>>> uncalledStepResultSuppliers) {
		Objects.requireNonNull(step, "step must not be null");

		final Supplier<StepResult<ResultUserType>> stepResultSupplier;

		if (step instanceof SplitStep) {
			for (final Step<?> nextStep : ((SplitStep) step).getSteps()) {
				prepareToExecute(nextStep, prevResultSupplier, allResults, modifications, uncalledStepResultSuppliers);
			}
			stepResultSupplier = () -> null; // Split steps don't produce a result and shouldn't have next steps either.
		} else if (step instanceof ModificationStep) {
			@SuppressWarnings("unchecked")
			final ModificationStep<?, ?, PrevResultUserType, ResultUserType> ms = (ModificationStep<?, ?, PrevResultUserType, ResultUserType>) step;
			stepResultSupplier = prepareToExecuteModification(ms, prevResultSupplier, allResults, modifications);
		} else if (step instanceof TransformerStep) {
			stepResultSupplier = new Supplier<StepResult<ResultUserType>>() {
				private boolean resultIsValid = false;
				private StepResult<ResultUserType> result;

				@Override
				public StepResult<ResultUserType> get() {
					if (!resultIsValid) {
						@SuppressWarnings("unchecked")
						final TransformerStep<PrevResultUserType, ResultUserType> ts = (TransformerStep<PrevResultUserType, ResultUserType>) step;
						result = ts.getHandler().apply(prevResultSupplier.get());
						resultIsValid = true;

						uncalledStepResultSuppliers.remove(this);

						if (result != null) {
							allResults.add(result);
						}
					}

					return result;
				}
			};

			// Only add the step result supplier to the uncalled step suppliers if there isn't a next step.
			// If there is a next step, the supplier will be called when that step is evaluated.
			// Modification step result suppliers aren't added to the list because the result is produced when the modification is executed.
			if (step.getNextStep() == null) {
				uncalledStepResultSuppliers.add(stepResultSupplier);
			}
		} else {
			throw new RuntimeException("Unexpected step: " + step);
		}

		if (step.getNextStep() != null) {
			prepareToExecute(step.getNextStep(), stepResultSupplier, allResults, modifications,
					uncalledStepResultSuppliers);
		}
	}

	private static <TagType, BusinessObjectType extends EObject, PrevResultUserType, ResultUserType> Supplier<StepResult<ResultUserType>> prepareToExecuteModification(
			ModificationStep<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modificationStep,
			final Supplier<StepResult<PrevResultUserType>> prevResultSupplier,
			final Collection<StepResult<?>> allResults,
			final List<AadlModificationService.Modification<?, ?>> modifications) {
		class ModificationStepModifier implements AadlModificationService.Modifier<TagType, BusinessObjectType> {
			StepResult<ResultUserType> result;

			@Override
			public void modify(final TagType tag, final BusinessObjectType boToModify) {
				result = modificationStep.getModifier().modify(tag, boToModify, prevResultSupplier.get());
				if (result != null) {
					allResults.add(result);
				}
			}
		}

		final ModificationStepModifier modifier = new ModificationStepModifier();

		final AadlModificationService.Modification<TagType, BusinessObjectType> modification = AadlModificationService.Modification
				.create(modificationStep.getTag(), modificationStep.getTagToBusinessObjectMapper(), modifier);
		modifications.add(modification);
		return () -> modifier.result;
	}

	// TODO: Convert to unit test.
	public static void main(String[] args) {
		DefaultOperationBuilder rootOpBuilder = new DefaultOperationBuilder();
		final OperationBuilder<Integer> b = rootOpBuilder.transform(arg -> StepResultBuilder.create(5).build());

		b.transform(pr -> StepResultBuilder.create(pr.getUserValue() + 5).build())
		.modify(5, tag -> null, (tag, boToModify, prevResult) -> {
			System.out.println("M1-A: " + prevResult.getUserValue());
			return StepResultBuilder.create(prevResult.getUserValue() + 5).build();
		}).modify(5, tag -> null, (tag, boToModify, prevResult) -> {
			System.out.println("M2-A: " + prevResult.getUserValue());
			return StepResultBuilder.create(prevResult.getUserValue() + 5).build();
		});

		b.transform(pr -> StepResultBuilder.create(pr.getUserValue() + 6).build())
		.modify(5, tag -> null, (tag, boToModify, prevResult) -> {
			System.out.println("M1-B: " + prevResult.getUserValue());
			return StepResultBuilder.create(prevResult.getUserValue() + 6).build();
		}).modify(5, tag -> null, (tag, boToModify, prevResult) -> {
			System.out.println("M2-B: " + prevResult.getUserValue());
			return StepResultBuilder.create(prevResult.getUserValue() + 6).build();
		}).transform(pr -> {
			System.out.println("T3-B: " + pr.getUserValue());
			return null;
		});


		final Step<?> firstStep = rootOpBuilder.build();

		// Create a modification service for the test
		final AadlModificationService modificationService = new AadlModificationService() {
			@Override
			public void modify(List<? extends Modification<?, ?>> modifications,
					ModificationPostprocessor postProcessor) {
				System.out.println("MODIFY");
				for (final Modification<?, ?> m : modifications) {
					executeModification(m);
				}

				postProcessor.modificationCompleted(true);
			}

			private <TagType, BusinessObjectType extends EObject> void executeModification(
					final Modification<TagType, BusinessObjectType> m) {
				m.getModifier().modify(m.getTag(), m.getTagToBusinessObjectMapper().apply(m.getTag()));
			}
		};

		final OperationExecutor executor = new OperationExecutor(modificationService);
		executor.execute(firstStep, (results) -> {
		});

	}

}
