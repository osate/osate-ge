package org.osate.ge.internal.graphiti;

import java.net.URL;

import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRendererFactory;
import org.eclipse.graphiti.platform.ga.IRendererContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osate.ge.internal.graphiti.diagram.PropertyUtil;

public class ImageGraphicsAlgorithmRendererFactory implements IGraphicsAlgorithmRendererFactory {
	public final static String IMAGE_FIGURE = "org.osate.ge.image.figure";
	public final static String DEFAULT_IMAGE = "/icons/DefaultImage.gif";

	@Override
	public IGraphicsAlgorithmRenderer createGraphicsAlgorithmRenderer(final IRendererContext context) {
		if (IMAGE_FIGURE.equals(context.getPlatformGraphicsAlgorithm().getId())) {
			return createImageFigure(PropertyUtil.getImage(context.getGraphicsAlgorithm()));
		}

		throw new RuntimeException("Cannot create platform graphics algorithm");
	}

	private ImageFigure createImageFigure(final String imagePath) {
		final ImageDescriptor imageDesc = getImageDescriptor(imagePath);
		return new ImageFigure(AgeDiagramTypeProvider.getResources().createImage(imageDesc));
	}

	// Create image descriptor
	private ImageDescriptor getImageDescriptor(final String imagePath) {
		try {
			return ImageDescriptor.createFromURL(new URL(imagePath));
		} catch (final Exception e) {
			// Return default image if image path is not valid
			return ImageDescriptor.createFromFile(getClass(), DEFAULT_IMAGE);
		}
	}
}
