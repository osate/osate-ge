package org.osate.ge.internal.graphiti;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public class ImageFigure extends RectangleFigure
implements IGraphicsAlgorithmRenderer {
	private Image image;

	public ImageFigure(final Image image) {
		this.image = image;
	}

	@Override
	protected void fillShape(final Graphics g) {
		if (image != null) {
			// scaling required
			double scalefactorX = 1.0;
			double scalefactorY = 1.0;
			// get image data from default image
			final ImageData originalImageData = image.getImageData();
			final int imageWidth = originalImageData.width;
			final int imageHeight = originalImageData.height;

			scalefactorX = getBounds().preciseWidth() / (double) imageWidth;
			scalefactorY = getBounds().preciseHeight() / (double) imageHeight;

			// create scaled image
			double d = imageWidth * scalefactorX;
			double e = imageHeight * scalefactorY;
			g.drawImage(image, 0, 0, imageWidth, imageHeight, getLocation().x, getLocation().y, (int) d, (int) e);
		}
	}

	@Override
	protected void outlineShape(Graphics g) {
	}
}
