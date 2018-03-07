package org.osate.ge.operations;

import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;

public interface OperationBuilder<PrevResultUserType> {
	<TagType, BusinessObjectType extends EObject, ResultUserType> OperationBuilder<ResultUserType> modify(TagType obj,
			Function<TagType, BusinessObjectType> tagToBoMapper,
			Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier);

	// TODO: Rename.
	<ResultUserType> OperationBuilder<ResultUserType> transform(
			Transformer<PrevResultUserType, ResultUserType> stepHandler);
}
