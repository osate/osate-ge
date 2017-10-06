package org.osate.ge.internal.elk;

import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;

public class AgeElkProperties {
	// Property used to provide the layout configurator with the layout mapping. Set on the root node.
	public final static IProperty<LayoutMapping> LAYOUT_MAPPING = new Property<>(
			"org.osate.ge.elk.layoutMapping");
}
