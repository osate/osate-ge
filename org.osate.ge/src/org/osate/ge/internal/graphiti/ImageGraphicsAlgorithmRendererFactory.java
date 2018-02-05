package org.osate.ge.internal.graphiti;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRendererFactory;
import org.eclipse.graphiti.platform.ga.IRendererContext;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osate.ge.internal.graphiti.diagram.PropertyUtil;

public class ImageGraphicsAlgorithmRendererFactory implements IGraphicsAlgorithmRendererFactory {
	public final static String IMAGE_FIGURE = "org.osate.ge.image.figure";

	@Override
	public IGraphicsAlgorithmRenderer createGraphicsAlgorithmRenderer(final IRendererContext context) {
		if (IMAGE_FIGURE.equals(context.getPlatformGraphicsAlgorithm().getId())) {
			return createImageFigure(
					PropertyUtil.getImage(context.getGraphicsAlgorithm()));
		}

		return null;
	}

	private ImageFigure createImageFigure(final String imageKey) {
		final Image image = new Image(Display.getCurrent(),
				ResourcesPlugin.getWorkspace().getRoot().getLocation() + imageKey);
		return new ImageFigure(image);
	}
}
