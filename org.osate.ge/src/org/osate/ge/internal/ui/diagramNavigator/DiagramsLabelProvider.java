package org.osate.ge.internal.ui.diagramNavigator;

import org.eclipse.jface.viewers.LabelProvider;

// TODO: Rename
public class DiagramsLabelProvider extends LabelProvider {
	@Override
	public String getText(final Object element) {
		if (element instanceof DiagramGroup) {
			final DiagramGroup dg = (DiagramGroup) element;
			if (dg.isContextReferenceValid()) {
				// TODO: Need to convert to proper string
				return "C: " + (dg.getContextReference() == null ? "NO CONTEXT" : dg.getContextReference().toString());
			} else if (dg.getDiagramTypeId() != null) {
				// TODO: Need to convert to proper string
				return "DT: " + dg.getDiagramTypeId();
			} else {
				throw new RuntimeException("Unexpected case. Diagram type and context reference are both null");
			}
		}

		return null;
	}

	// TODO: icons

}
