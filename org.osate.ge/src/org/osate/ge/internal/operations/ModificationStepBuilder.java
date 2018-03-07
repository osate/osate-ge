package org.osate.ge.internal.operations;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.operations.Modifier;

class ModificationStepBuilder<TagType, BusinessObjectType extends EObject, PrevResultUserType, ResultUserType>
extends AbstractStepBuilder<ResultUserType> {
	private final TagType tag;
	private final Function<TagType, BusinessObjectType> tagToBoMapper;
	private final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier;

	public ModificationStepBuilder(final TagType tag, final Function<TagType, BusinessObjectType> tagToBoMapper,
			final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier) {
		this.tag = Objects.requireNonNull(tag, "tag must not be null");
		this.tagToBoMapper = Objects.requireNonNull(tagToBoMapper,
				"tagToBoMapper must not be null");
		this.modifier = Objects.requireNonNull(modifier, "modifier must not be null");
	}

	@Override
	protected Step<?> buildThisStep(final Step<?> nextStep) {
		return new ModificationStep<>(nextStep, tag, tagToBoMapper, modifier);
	}
}
