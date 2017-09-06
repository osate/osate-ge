package org.osate.ge.gef;

import java.util.Map;

import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;
import org.osate.ge.gef.parts.AgeShapeContentPart;

import com.google.inject.Inject;
import com.google.inject.Injector;

import javafx.scene.Node;

public class AgeContentPartFactory implements IContentPartFactory {
	@Inject
	private Injector injector;

	@Override
	public IContentPart<? extends Node> createContentPart(final Object content, final Map<Object, Object> contextMap) {
		// TODO: Create appropriate part based on content.
		// Ideally there would be one content part for both shapes and connections but GEF5 doesn't appear to allow recreating the visual.
		return injector.getInstance(AgeShapeContentPart.class);
	}

}
