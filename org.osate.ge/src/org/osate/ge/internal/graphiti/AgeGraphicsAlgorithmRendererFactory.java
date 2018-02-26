package org.osate.ge.internal.graphiti;

import java.io.Closeable;
import java.util.Objects;

import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRendererFactory;
import org.eclipse.graphiti.platform.ga.IRendererContext;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.osate.ge.internal.graphiti.diagram.PropertyUtil;

public class AgeGraphicsAlgorithmRendererFactory implements IGraphicsAlgorithmRendererFactory, Closeable {
	public final static String IMAGE_FIGURE = "org.osate.ge.image.figure";

	private static final FontDescriptor errorFontDesc = FontDescriptor.createFrom(new FontData("Arial", 12, SWT.BOLD));
	private final LocalResourceManager localResourceManager = new LocalResourceManager(
			AgeDiagramTypeProvider.getResources());

	@Override
	public void close() {
		localResourceManager.dispose();
	}

	@Override
	public IGraphicsAlgorithmRenderer createGraphicsAlgorithmRenderer(final IRendererContext context) {
		if (IMAGE_FIGURE.equals(context.getPlatformGraphicsAlgorithm().getId())) {
			final Font errorFont = Objects.requireNonNull(localResourceManager.createFont(errorFontDesc),
					"Unable to create error font");
			return new ImageFigure(PropertyUtil.getImage(context.getGraphicsAlgorithm()), errorFont);
		}

		throw new RuntimeException("Cannot create platform graphics algorithm");
	}
}
