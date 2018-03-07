package org.osate.ge.operations;

import org.eclipse.emf.ecore.EObject;

// TODO: Rename to ModelModifier?
public interface Modifier<TagType, BusinessObjectType extends EObject, PrevResultUserType, ResultUserType> {
	StepResult<ResultUserType> modify(TagType tag, BusinessObjectType boToModify,
			StepResult<PrevResultUserType> prevResult);
}
