package org.osate.ge.internal.graphiti;

import java.net.MalformedURLException;
import java.util.Objects;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class ImageFigure extends RectangleFigure
implements IGraphicsAlgorithmRenderer {
	private final String imagePath;
	private final Font errorFont;
	private final Image image;

	public ImageFigure(final String imagePath, final Font errorFont) {
		this.imagePath = Objects.requireNonNull(imagePath, "image path must not be null");
		this.errorFont = Objects.requireNonNull(errorFont, "errorFont must not be null");
		this.image = createImage(imagePath);
	}

	private static Image createImage(final String imagePath) {
		final ImageDescriptor imageDesc = getImageDescriptor(imagePath);
		if (imageDesc == null) {
			return null;
		}

		final Object image = AgeDiagramTypeProvider.getResources().find(imageDesc);
		return image instanceof Image ? (Image) image : null;
	}

	// Create image descriptor
	private static ImageDescriptor getImageDescriptor(final String imagePath) {
		try {
			final IResource imageResource = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(Path.fromPortableString(imagePath));
			return imageResource == null ? null
					: ImageDescriptor.createFromURL(imageResource.getRawLocationURI().toURL());
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Not a valid image path: " + imagePath);
		}
	}

	@Override
	protected void fillShape(final Graphics g) {
		if (image != null && !image.isDisposed()) {
			g.setAntialias(SWT.ON);
			g.setInterpolation(SWT.HIGH);

			// Get image data
			final ImageData originalImageData = image.getImageData();
			final int imageWidth = originalImageData.width;
			final int imageHeight = originalImageData.height;

			// Scaling
			double scalefactorX = 1.0;
			double scalefactorY = 1.0;
			scalefactorX = getBounds().preciseWidth() / (double) imageWidth;
			scalefactorY = getBounds().preciseHeight() / (double) imageHeight;

			// Scaled width and height
			double scaledWidth = imageWidth * scalefactorX;
			double scaledHeight = imageHeight * scalefactorY;
			// Draw scaled image
			g.drawImage(image, 0, 0, imageWidth, imageHeight, getLocation().x, getLocation().y, (int) scaledWidth, (int) scaledHeight);
		} else {
			// Fill shape white
			g.setBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			super.fillShape(g);

			// Set font color to black
			g.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			// Draw unable to load image text centered
			final Rectangle bounds = getBounds();
			g.setTextAntialias(SWT.ON);
			g.setFont(errorFont);
			final String errorMsg = "Unable to load image: " + imagePath;
			final Dimension textSize = FigureUtilities.getTextExtents(errorMsg, errorFont);
			g.drawText(errorMsg,
					new Point(bounds.x + (bounds.width - textSize.width) / 2,
							bounds.y + (bounds.height - textSize.height) / 2));
		}
	}

	@Override
	protected void outlineShape(final Graphics g) {
		if (image == null) {
			// Draw outline
			super.outlineShape(g);
		}
	}
}
