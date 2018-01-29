package org.osate.ge.internal.diagram.runtime.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.osate.aadl2.Feature;
import org.osate.aadl2.FlowSpecification;
import org.osate.aadl2.Generalization;
import org.osate.aadl2.Mode;
import org.osate.aadl2.ModeTransition;
import org.osate.aadl2.ModeTransitionTrigger;
import org.osate.ge.internal.model.SubprogramCallOrder;
import org.osate.ge.internal.model.Tag;

/**
 * Implementation of built-in contents filters.
 *
 */
public enum BuiltinContentsFilter implements ContentsFilter {
	ALLOW_NONE("none", "None", bo -> false),
	ALLOW_FUNDAMENTAL(ContentsFilter.ALLOW_FUNDAMENTAL_ID, "Minimal", bo -> isFundamental(bo)), // Only include business objects which are manually specified or
	// which are an inherent/fundamental part of the parent.
	ALLOW_TYPE(ContentsFilter.ALLOW_TYPE_ID, "Type", bo -> isInType(bo)), // Include business objects which have been manually specified or which are related to
	// a Classifier Type. For example, features.
	ALLOW_ALL(ContentsFilter.ALLOW_ALL_ID, "All", bo -> true); // Include all business objects

	private static final Map<String, BuiltinContentsFilter> idToContentsFilterMap;
	static {
		final Map<String, BuiltinContentsFilter> modifiableMap = new HashMap<>();
		for(final BuiltinContentsFilter value : BuiltinContentsFilter.values()) {
			modifiableMap.put(value.id, value);
		}
		idToContentsFilterMap = Collections.unmodifiableMap(modifiableMap);
	}

	public static BuiltinContentsFilter getById(final String id) {
		return idToContentsFilterMap.get(id);
	}

	private final String id;// For serialization
	private final String description;
	private final Predicate<Object> filter;

	BuiltinContentsFilter(final String id, final String description, final Predicate<Object> filter) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.description = Objects.requireNonNull(description, "description must not be null");
		this.filter = Objects.requireNonNull(filter, "filter must not be null");
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public boolean test(final Object t) {
		return filter.test(t);
	}

	private static boolean isFundamental(final Object bo) {
		if(bo instanceof Generalization ||
				bo instanceof SubprogramCallOrder) {
			return true;
		}

		if(bo instanceof Tag) {
			final Tag tag = (Tag)bo;
			if(tag.key.equals(Tag.KEY_UNIDIRECTIONAL) ||
					tag.key.equals(Tag.KEY_SUBPROGRAM_CALL_CALLED_SUBPROGRAM)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isInType(final Object bo) {
		if(isFundamental(bo) ||
				bo instanceof Feature ||
				bo instanceof FlowSpecification ||
				bo instanceof Mode ||
				bo instanceof ModeTransition ||
				bo instanceof ModeTransitionTrigger) {
			return true;
		}

		if(bo instanceof Tag) {
			final Tag tag = (Tag)bo;
			if(tag.key.equals(Tag.KEY_SUBCOMPONENT_TYPE)) {
				return true;
			}
		}

		return false;
	}
}