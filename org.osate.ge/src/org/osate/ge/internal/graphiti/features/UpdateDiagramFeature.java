package org.osate.ge.internal.graphiti.features;

import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.layout.DiagramElementLayoutUtil;
import org.osate.ge.internal.diagram.runtime.layout.LayoutInfoProvider;
import org.osate.ge.internal.diagram.runtime.updating.DiagramUpdater;
import org.osate.ge.internal.graphiti.GraphitiAgeDiagramProvider;

public class UpdateDiagramFeature extends AbstractUpdateFeature implements ICustomUndoRedoFeature {
	private final GraphitiAgeDiagramProvider graphitiAgeDiagramProvider;
	private final DiagramUpdater diagramUpdater;
	private final LayoutInfoProvider layoutInfoProvider;

	@Inject
	public UpdateDiagramFeature(final IFeatureProvider fp,
			final GraphitiAgeDiagramProvider graphitiAgeDiagramProvider,
			final DiagramUpdater diagramUpdater, final LayoutInfoProvider layoutInfoProvider) {
		super(fp);
		this.graphitiAgeDiagramProvider = Objects.requireNonNull(graphitiAgeDiagramProvider, "graphitiAgeDiagramProvider must not be null");
		this.diagramUpdater = Objects.requireNonNull(diagramUpdater, "diagramUpdater must not be null");
		this.layoutInfoProvider = Objects.requireNonNull(layoutInfoProvider, "layoutInfoProvider must not be null");
	}

	@Override
	public boolean canUpdate(IUpdateContext context) {
		return context.getPictogramElement() instanceof Diagram;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		return Reason.createTrueReason();
	}

	@Override
	public boolean update(final IUpdateContext context) {
		final AgeDiagram ageDiagram = graphitiAgeDiagramProvider.getGraphitiAgeDiagram().getAgeDiagram();

		// Update the diagram
		diagramUpdater.updateDiagram(ageDiagram);

		// Perform the layout as a separate operation because the sizes for the shapes are assigned by the Graphiti modification listener.
		ageDiagram.modify("Update Diagram",
				m -> DiagramElementLayoutUtil.layoutIncrementally(ageDiagram, m, layoutInfoProvider));

		return true;
	}

	// ICustomUndoRedoFeature
	@Override
	public boolean canUndo(final IContext context) {
		return false;
	}

	@Override
	public void preUndo(final IContext context) {
	}

	@Override
	public void postUndo(final IContext context) {
	}

	@Override
	public boolean canRedo(final IContext context) {
		return false;
	}

	@Override
	public void preRedo(final IContext context) {
	}

	@Override
	public void postRedo(final IContext context) {
	}
}
