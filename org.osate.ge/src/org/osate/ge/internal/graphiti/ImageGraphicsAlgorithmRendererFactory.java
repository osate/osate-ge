package org.osate.ge.internal.graphiti;

import java.net.URL;

import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRendererFactory;
import org.eclipse.graphiti.platform.ga.IRendererContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osate.ge.internal.graphiti.diagram.PropertyUtil;

public class ImageGraphicsAlgorithmRendererFactory implements IGraphicsAlgorithmRendererFactory {
	public final static String IMAGE_FIGURE = "org.osate.ge.image.figure";

	@Override
	public IGraphicsAlgorithmRenderer createGraphicsAlgorithmRenderer(final IRendererContext context) {
		if (IMAGE_FIGURE.equals(context.getPlatformGraphicsAlgorithm().getId())) {
			return createImageFigure(PropertyUtil.getImage(context.getGraphicsAlgorithm()));
		}

		throw new RuntimeException("Cannot create platform graphics algorithm");
	}

	private ImageFigure createImageFigure(final String imagePath) {
		try {
			final Image image = AgeDiagramTypeProvider.getResources()
					.createImage(ImageDescriptor.createFromURL(new URL(imagePath)));
			return new ImageFigure(image);
		} catch (final Exception e) {
			throw new RuntimeException("Cannot find image");
		}
	}
}
