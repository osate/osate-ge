package org.osate.ge.internal.diagram.runtime.filters;

import com.google.common.collect.ImmutableSet;

public enum LegacyContentFilter {
	// TODO: Configure mapping to new content filters
	ALLOW_FUNDAMENTAL("allow_fundamental", ImmutableSet.of()), ALLOW_TYPE("allow_type",
			ImmutableSet.of()), ALLOW_ALL("allow_all", ImmutableSet.of());

	private final String id;
	private final ImmutableSet<String> contentFilterIds;

	LegacyContentFilter(final String id, final ImmutableSet<String> contentFilterIds) {
		this.id = id;
		this.contentFilterIds = contentFilterIds;
	}

	public String getId() {
		return id;
	}

	public ImmutableSet<String> getContentFilterIds() {
		return contentFilterIds;
	}
}
