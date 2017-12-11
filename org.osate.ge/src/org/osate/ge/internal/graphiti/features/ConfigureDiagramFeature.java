package org.osate.ge.internal.graphiti.features;

import java.util.LinkedList;
import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.boTree.BusinessObjectNode;
import org.osate.ge.internal.diagram.runtime.boTree.DiagramToBusinessObjectTreeConverter;
import org.osate.ge.internal.diagram.runtime.boTree.TreeUpdater;
import org.osate.ge.internal.diagram.runtime.layout.DiagramElementLayoutUtil;
import org.osate.ge.internal.diagram.runtime.layout.LayoutInfoProvider;
import org.osate.ge.internal.diagram.runtime.updating.DiagramUpdater;
import org.osate.ge.internal.graphiti.GraphitiAgeDiagramProvider;
import org.osate.ge.internal.graphiti.diagram.GraphitiAgeDiagram;
import org.osate.ge.internal.services.ExtensionService;
import org.osate.ge.internal.services.ProjectProvider;
import org.osate.ge.internal.services.ProjectReferenceService;
import org.osate.ge.internal.ui.dialogs.DefaultDiagramConfigurationDialogModel;
import org.osate.ge.internal.ui.dialogs.DiagramConfigurationDialog;

public class ConfigureDiagramFeature extends AbstractCustomFeature implements ICustomUndoRedoFeature {
	private final TreeUpdater boTreeExpander;
	private final DiagramUpdater diagramUpdater;
	private final GraphitiAgeDiagramProvider graphitiAgeDiagramProvider;
	private final ProjectReferenceService referenceService;
	private final ExtensionService extService;
	private final ProjectProvider projectProvider;
	private final LayoutInfoProvider layoutInfoProvider;

	@Inject
	public ConfigureDiagramFeature(final IFeatureProvider fp,
			final TreeUpdater boTreeExpander,
			final DiagramUpdater diagramUpdater,
			final GraphitiAgeDiagramProvider graphitiAgeDiagramProvider,
			final ProjectReferenceService referenceService,
			final ExtensionService extService,
			final ProjectProvider projectProvider, final LayoutInfoProvider layoutInfoProvider) {
		super(fp);
		this.boTreeExpander = Objects.requireNonNull(boTreeExpander, "boTreeExpander must not be null");
		this.diagramUpdater = Objects.requireNonNull(diagramUpdater, "diagramUpdater must not be null");
		this.graphitiAgeDiagramProvider = Objects.requireNonNull(graphitiAgeDiagramProvider, "graphitiAgeDiagramProvider must not be null");
		this.referenceService = Objects.requireNonNull(referenceService, "referenceService must not be null");
		this.extService = Objects.requireNonNull(extService, "extService must not be null");
		this.projectProvider = Objects.requireNonNull(projectProvider, "projectProvider must not be null");
		this.layoutInfoProvider = Objects.requireNonNull(layoutInfoProvider, "layoutInfoProvider must not be null");
	}

	@Override
	public String getName() {
		return "Configure Diagram...";
	}

	@Override
	public boolean canExecute(final ICustomContext context) {
		return true;
	}

	@Override
	public void execute(final ICustomContext context) {
		final GraphitiAgeDiagram graphitiAgeDiagram = graphitiAgeDiagramProvider.getGraphitiAgeDiagram();
		final AgeDiagram diagram = graphitiAgeDiagram.getAgeDiagram();

		BusinessObjectNode boTree = DiagramToBusinessObjectTreeConverter.createBusinessObjectNode(diagram);

		// Update the tree so that it's business objects are refreshed
		boTree = boTreeExpander.expandTree(diagram.getConfiguration(), boTree, diagram.getMaxElementId() + 1);

		final long nextElementId = BusinessObjectNode.getMaxId(boTree) + 1;

		try(final DefaultDiagramConfigurationDialogModel model = new DefaultDiagramConfigurationDialogModel(referenceService, extService, projectProvider, nextElementId)) {
			// Create a BO path for the initial selection
			Object[] initialSelectionBoPath = null;
			if(context.getPictogramElements() != null && context.getPictogramElements().length == 1) {
				final PictogramElement pe = context.getPictogramElements()[0];
				final DiagramElement element = graphitiAgeDiagram.getDiagramElement(pe);
				if(element != null && model.showBusinessObject(element.getBusinessObject())) { // Only build a selection path if the BO will be shown
					DiagramNode tmp = element;
					final LinkedList<Object> boList = new LinkedList<>();
					while(tmp instanceof DiagramElement) {
						boList.addFirst(tmp.getBusinessObject());
						tmp = tmp.getParent();
					}

					initialSelectionBoPath = boList.toArray();
				}
			}

			// Show the dialog
			final DiagramConfigurationDialog.Result result = DiagramConfigurationDialog.show(null, model, diagram.getConfiguration(), boTree, initialSelectionBoPath);
			if(result != null) {
				diagram.modify("Set Diagram Configuration", m -> {
					m.setDiagramConfiguration(result.getDiagramConfiguration());
					diagramUpdater.updateDiagram(diagram, result.getBusinessObjectTree());
				});
				// Clear ghosts triggered by this update to prevent them from being unghosted during the next update.
				diagramUpdater.clearGhosts();

				diagram.modify("Layout",
						m -> DiagramElementLayoutUtil.layoutIncrementally(diagram, m, layoutInfoProvider));
			}
		}
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
