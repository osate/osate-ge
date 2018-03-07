package org.osate.ge.internal.operations;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.operations.Modifier;

class ModificationStepBuilder<TagType, BusinessObjectType extends EObject, PrevResultUserType, ResultUserType>
extends AbstractStepBuilder<ResultUserType> {
	private final TagType tag;
	private final BusinessObjectProvider<TagType, BusinessObjectType, PrevResultUserType> boProvider;
	private final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier;

	public ModificationStepBuilder(final TagType tag,
			final BusinessObjectProvider<TagType, BusinessObjectType, PrevResultUserType> boProvider,
			final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier) {
		this.tag = tag;
		this.boProvider = Objects.requireNonNull(boProvider, "boProvider must not be null");
		this.modifier = Objects.requireNonNull(modifier, "modifier must not be null");
	}

	@Override
	protected Step<?> buildThisStep(final Step<?> nextStep) {
		return new ModificationStep<>(nextStep, tag, boProvider, modifier);
	}
}
