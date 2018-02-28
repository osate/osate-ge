package org.osate.ge.internal.graphiti;

import java.io.Closeable;

import org.eclipse.core.runtime.Path;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRendererFactory;
import org.eclipse.graphiti.platform.ga.IRendererContext;
import org.eclipse.jface.resource.LocalResourceManager;
import org.osate.ge.internal.graphiti.diagram.PropertyUtil;

public class AgeGraphicsAlgorithmRendererFactory implements IGraphicsAlgorithmRendererFactory, Closeable {
	public final static String IMAGE_FIGURE = "org.osate.ge.image.figure";
	private final LocalResourceManager localResourceManager = new LocalResourceManager(
			AgeDiagramTypeProvider.getResources());

	@Override
	public void close() {
		localResourceManager.dispose();
	}

	@Override
	public IGraphicsAlgorithmRenderer createGraphicsAlgorithmRenderer(final IRendererContext context) {
		if (IMAGE_FIGURE.equals(context.getPlatformGraphicsAlgorithm().getId())) {
			return new ImageFigure(Path.fromPortableString(PropertyUtil.getImage(context.getGraphicsAlgorithm())));
		}

		throw new RuntimeException("Cannot create platform graphics algorithm");
	}
}
