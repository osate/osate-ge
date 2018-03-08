package org.osate.ge.operations;

import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;

public interface OperationBuilder<PrevResultUserType> {
	// TODO: Move outside of interface?
	interface BusinessObjectProvider<TagType, BusinessObjectType, PrevResultUserType> {
		BusinessObjectType getBusinessObject(TagType tag, PrevResultUserType previousUserValue);
	}

	// TODO: Should modifier have access to entire result or just the previous user data?

	<TagType, BusinessObjectType extends EObject, ResultUserType> OperationBuilder<ResultUserType> modifyModel(
			TagType obj, BusinessObjectProvider<TagType, BusinessObjectType, PrevResultUserType> boProvider,
			Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier);

	@SuppressWarnings("unchecked")
	/**
	 *
	 * @param modifier must not return null
	 * @return
	 */
	default <TagType, ResultUserType> OperationBuilder<ResultUserType> modifyPreviousResult(
			final Function<PrevResultUserType, StepResult<ResultUserType>> modifier) {

		return modifyModel(null, (tag, prevResult) -> {
			if (!(prevResult instanceof EObject)) {
				throw new IllegalStateException("Previous result must be an EObject. Previous result: " + prevResult);
			}
			return (EObject) prevResult;
		}, (tag, boToModify, prevResult) -> modifier.apply((PrevResultUserType) boToModify));
	}

	// TODO: Rename.
	<ResultUserType> OperationBuilder<ResultUserType> transform(
			Transformer<PrevResultUserType, ResultUserType> stepHandler);

	default <ResultUserType> OperationBuilder<ResultUserType> supply(
			final Supplier<StepResult<ResultUserType>> supplier) {
		return transform(prevResult -> supplier.get());
	}
}
