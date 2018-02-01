package org.osate.ge.internal.diagram.runtime.filtering;

import com.google.common.collect.ImmutableCollection;

public interface ContentFilterProvider {
	ImmutableCollection<ContentFilter> getContentFilters();
}
