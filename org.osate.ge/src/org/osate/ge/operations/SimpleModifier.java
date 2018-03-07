package org.osate.ge.operations;

import org.eclipse.emf.ecore.EObject;

// TODO: Rename to something more descriptive?
public interface SimpleModifier<TagType, BusinessObjectType extends EObject, PrevResultUserType, ResultUserType>
extends Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> {
	@Override
	default StepResult<ResultUserType> modify(TagType tag, BusinessObjectType boToModify,
			StepResult<PrevResultUserType> prevResult) {
		return modify(boToModify);
	}

	StepResult<ResultUserType> modify(BusinessObjectType boToModify);
}
