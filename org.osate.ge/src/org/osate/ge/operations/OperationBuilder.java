package org.osate.ge.operations;

import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;

public interface OperationBuilder<PrevResultUserType> {
	// TODO: Move outside of interface?
	interface BusinessObjectProvider<TagType, BusinessObjectType, PrevResultUserType> {
		BusinessObjectType getBusinessObject(TagType tag, StepResult<PrevResultUserType> previousResult);
	}

	// TODO: Should modifer have access to entire result or just the previous user data?

	<TagType, BusinessObjectType extends EObject, ResultUserType> OperationBuilder<ResultUserType> modifyModel(TagType obj,
			BusinessObjectProvider<TagType, BusinessObjectType, PrevResultUserType> boProvider,
			Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier);

	// TODO: Rename arguments?
	default <TagType, BusinessObjectType extends EObject, ResultUserType> OperationBuilder<ResultUserType> modifyModel(
			Function<PrevResultUserType, BusinessObjectType> prevResultUserValueToBusinessObject,
			SimpleModifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier) {
		return modifyModel(null,
				(tag, prevResult) -> prevResultUserValueToBusinessObject.apply(prevResult.getUserValue()), modifier);
	}

	// TODO: Rename.
	<ResultUserType> OperationBuilder<ResultUserType> transform(
			Transformer<PrevResultUserType, ResultUserType> stepHandler);
}
