package org.osate.ge.internal.ui.diagramNavigator;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.osate.ge.DiagramType;
import org.osate.ge.internal.diagram.runtime.types.DiagramTypeProvider;
import org.osate.ge.internal.services.ExtensionRegistryService;
import org.osate.ge.internal.services.ReferenceLabelService;
import org.osate.ge.internal.services.ReferenceService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

// TODO: Rename
public class DiagramsLabelProvider extends DecoratingLabelProvider {
	private final DiagramTypeProvider dtProvider;
	private final ReferenceLabelService referenceLabelService;

	public DiagramsLabelProvider() {
		super(new WorkbenchLabelProvider(), null);

		final Bundle bundle = FrameworkUtil.getBundle(getClass());
		final IEclipseContext context = EclipseContextFactory.getServiceContext(bundle.getBundleContext())
				.createChild();
		dtProvider = Objects.requireNonNull(context.get(ExtensionRegistryService.class),
				"Unable to get extension registry");
		referenceLabelService = Objects.requireNonNull(context.get(ReferenceService.class),
				"Unable to get reference service");
	}

	@Override
	public String getText(final Object element) {
		// TODO
		if (element instanceof DiagramGroup) {
			final DiagramGroup dg = (DiagramGroup) element;
			if (dg.isContextReferenceValid()) {
				if (dg.getContextReference() == null) {
					return "<No Context>";
				} else {
					final String contextLabel = referenceLabelService.getLabel(dg.getContextReference());
					return contextLabel == null ? "Unrecognized Context: " + dg.getContextReference() : contextLabel;
				}
			} else if (dg.getDiagramTypeId() != null) {
				final Optional<DiagramType> dtOpt = dtProvider.getDiagramTypeById(dg.getDiagramTypeId());
				return dtOpt.map(dt -> dt.getPluralName())
						.orElseGet(() -> "Unrecognized Diagram Type : " + dg.getDiagramTypeId());
			} else {
				throw new RuntimeException("Unexpected case. Diagram type and context reference are both null");
			}
		}

		return super.getText(element);
	}

	@Override
	public Image getImage(final Object element) {
		final ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		if (element instanceof DiagramGroup) {
			return images.getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		return super.getImage(element);
	}
}
