package org.osate.ge.internal.diagram.runtime.filtering;

import java.util.Optional;

import com.google.common.collect.ImmutableCollection;

public interface ContentFilterProvider {
	ImmutableCollection<ContentFilter> getContentFilters();

	default Optional<ContentFilter> getContentFilterById(String id) {
		for (final ContentFilter contentFilter : getContentFilters()) {
			if (contentFilter.getId().equals(id)) {
				return Optional.of(contentFilter);
			}
		}

		return Optional.empty();
	}
}
