package org.osate.ge.internal.operations;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.osate.ge.operations.Modifier;

class ModificationStep<TagType, BusinessObjectType extends EObject, PrevResultUserType, ResultUserType>
extends AbstractStep<ResultUserType> {
	private final TagType tag;
	private final Function<TagType, BusinessObjectType> tagToBoMapper;
	private final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier;

	public ModificationStep(final Step<?> nextStep, final TagType tag,
			final Function<TagType, BusinessObjectType> tagToBoMapper,
			final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> modifier) {
		super(nextStep);
		this.tag = Objects.requireNonNull(tag, "tag must not be null");
		this.tagToBoMapper = Objects.requireNonNull(tagToBoMapper,
				"tagToBoMapper must not be null");
		this.modifier = Objects.requireNonNull(modifier, "modifier must not be null");
	}

	public final TagType getTag() {
		return tag;
	}

	public final Function<TagType, BusinessObjectType> getTagToBusinessObjectMapper() {
		return tagToBoMapper;
	}

	public final Modifier<TagType, BusinessObjectType, PrevResultUserType, ResultUserType> getModifier() {
		return modifier;
	}
}
