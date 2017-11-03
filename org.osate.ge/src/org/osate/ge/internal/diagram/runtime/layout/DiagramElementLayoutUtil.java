package org.osate.ge.internal.diagram.runtime.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.elk.core.IGraphLayoutEngine;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IGraphElementVisitor;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.ui.IEditorPart;
import org.osate.ge.DockingPosition;
import org.osate.ge.graphics.Point;
import org.osate.ge.graphics.internal.AgeShape;
import org.osate.ge.graphics.internal.Label;
import org.osate.ge.internal.Activator;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramElementPredicates;
import org.osate.ge.internal.diagram.runtime.DiagramModification;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.Dimension;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.diagram.runtime.styling.StyleCalculator;
import org.osate.ge.internal.diagram.runtime.styling.StyleProvider;
import org.osate.ge.internal.preferences.Preferences;
import org.osate.ge.internal.query.Queryable;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class DiagramElementLayoutUtil {
	private static final String incrementalLayoutLabel = "Incremental Layout";
	private static final String layoutAlgorithm = "org.eclipse.elk.layered";
	private final static BiMap<DockArea, PortSide> dockAreaToPortSideMap = ImmutableBiMap.of(DockArea.TOP,
			PortSide.NORTH, DockArea.BOTTOM, PortSide.SOUTH, DockArea.LEFT, PortSide.WEST, DockArea.RIGHT,
			PortSide.EAST);

	public static void layout(final String label, final IEditorPart editor,
			final Collection<? extends DiagramNode> diagramNodes,
			final LayoutOptions options) {
		if (!(editor instanceof AgeDiagramEditor)) {
			throw new RuntimeException("Editor must be an " + AgeDiagramEditor.class.getName());
		}

		final AgeDiagramEditor ageDiagramEditor = ((AgeDiagramEditor) editor);
		final LayoutInfoProvider layoutInfoProvider = Adapters.adapt(ageDiagramEditor, LayoutInfoProvider.class);
		layout(label, ageDiagramEditor.getAgeDiagram(), diagramNodes, layoutInfoProvider, options);
	}

	public static void layout(final String label, final AgeDiagram diagram,
			final LayoutInfoProvider layoutInfoProvider,
			final LayoutOptions options) {
		layout(label, diagram, null, layoutInfoProvider, options);
	}

	public static void layout(final String label, final AgeDiagram diagram,
			final Collection<? extends DiagramNode> diagramNodes,
			final LayoutInfoProvider layoutInfoProvider,
			final LayoutOptions options) {
		Objects.requireNonNull(label, "label must not be null");
		Objects.requireNonNull(diagram, "diagram must not be null");
		Objects.requireNonNull(layoutInfoProvider, "layoutInfoProvider must not be null");
		Objects.requireNonNull(options, "options must not be null");

		// Determine the diagram nodes to layout
		final Collection<DiagramNode> nodesToLayout;
		if (diagramNodes == null) {
			nodesToLayout = Collections.singletonList(diagram);
		} else {
			// Only layout shapes. Also filter out any descendants of specified diagram elements
			nodesToLayout = filterUnnecessaryNodes(diagramNodes);
		}

		if (nodesToLayout.isEmpty()) {
			return;
		}

		diagram.modify(label, m -> layout(m, nodesToLayout,
				new StyleCalculator(diagram.getConfiguration(), StyleProvider.EMPTY), layoutInfoProvider, options));
	}

	private static void layout(final DiagramModification m,
			final Collection<? extends DiagramNode> nodesToLayout, final StyleProvider styleProvider,
			final LayoutInfoProvider layoutInfoProvider,
			final LayoutOptions options) {
		Objects.requireNonNull(nodesToLayout, "nodesToLayout must not be null");

		// Layout the nodes
		final IGraphLayoutEngine layoutEngine = new RecursiveGraphLayoutEngine();
		for (final DiagramNode dn : nodesToLayout) {
			final LayoutMapping mapping = ElkGraphBuilder.buildLayoutGraph(dn, styleProvider, layoutInfoProvider);

			mapping.getLayoutGraph().setProperty(CoreOptions.ALGORITHM, layoutAlgorithm);

			// Apply properties for the initial layout
			applyProperties(mapping, options);

			LayoutDebugUtil.saveElkGraphToDebugProject(mapping.getLayoutGraph());

			// Perform the layout
			layoutEngine.layout(mapping.getLayoutGraph(), new BasicProgressMonitor());

			applyShapeLayout(mapping, m);

			LayoutDebugUtil.showGraphInLayoutGraphView(mapping.getLayoutGraph());
		}
	}

	/**
	 * Performs layout on elements in the specified diagram which have not been layed out.
	 * @param diagram
	 * @param mod
	 */
	public static void layoutIncrementally(final AgeDiagram diagram, final DiagramModification mod,
			final LayoutInfoProvider layoutInfoProvider) {
		Objects.requireNonNull(diagram, "diagram must not be null");
		Objects.requireNonNull(mod, "mod must not be null");
		Objects.requireNonNull(layoutInfoProvider, "layoutInfoProvider must not be null");

		final IncrementalLayoutMode currentLayoutMode = IncrementalLayoutMode
				.getById(Activator.getDefault().getPreferenceStore().getString(Preferences.INCREMENTAL_LAYOUT_MODE))
				.orElse(IncrementalLayoutMode.LAYOUT_CONTENTS);

		final Collection<DiagramNode> nodesToLayout = DiagramElementLayoutUtil
				.filterUnnecessaryNodes(getNodesToLayoutIncrementally(diagram, currentLayoutMode, new HashSet<>()));

		if (nodesToLayout.size() == 0) {
			return;
		}

		if (currentLayoutMode == IncrementalLayoutMode.LAYOUT_DIAGRAM) {
			DiagramElementLayoutUtil.layout(incrementalLayoutLabel, diagram, layoutInfoProvider,
					new LayoutOptionsBuilder().build());
		} else {
			DiagramElementLayoutUtil.layout(mod, nodesToLayout,
					new StyleCalculator(diagram.getConfiguration(), StyleProvider.EMPTY),
					layoutInfoProvider,
					new LayoutOptionsBuilder().build());

			// Set Positions of elements which do not have a position set.
			for (final DiagramNode dn : nodesToLayout) {
				if (dn instanceof DiagramElement) {
					final DiagramElement de = (DiagramElement) dn;
					if (!de.hasPosition()) {
						if (de.getDockArea() == null) {
							mod.setPosition(de, new Point(0.0, 0.0));
						} else if (de.getDockArea() != DockArea.GROUP && de.getParent() instanceof DiagramElement) {
							final DiagramElement parent = (DiagramElement) de.getParent();
							final DockingPosition defaultDockingPosition = de
									.getGraphicalConfiguration().defaultDockingPosition;
							final DockArea defaultDockArea = defaultDockingPosition
									.getDefaultDockArea();

							if (parent.hasSize()) {
								final Stream<DiagramElement> otherElementsAlongSide = parent.getDiagramElements().stream().filter(
										c -> c.hasPosition() && c.hasSize() && c.getDockArea() == defaultDockArea);

								// Determine the position of the new element along it's preferred docking position.
								double locationAlongSide;
								if (defaultDockingPosition == DockingPosition.TOP
										|| defaultDockingPosition == DockingPosition.BOTTOM) {
									locationAlongSide = otherElementsAlongSide.max(Comparator.comparingDouble(c -> c.getY()))
											.map(c -> c.getX() + c.getWidth()).orElse(0.0);
								} else {
									locationAlongSide = otherElementsAlongSide.max(Comparator.comparingDouble(c -> c.getY()))
											.map(c -> c.getY() + c.getHeight()).orElse(0.0);
								}

								// Set position based on the docking position
								switch (defaultDockingPosition) {
								case TOP:
									mod.setPosition(de, new Point(locationAlongSide, 0));
									break;
								case BOTTOM:
									mod.setPosition(de, new Point(locationAlongSide, parent.getHeight()));
									break;
								case LEFT:
									mod.setPosition(de, new Point(0, locationAlongSide));
									break;
								case RIGHT:
									mod.setPosition(de, new Point(parent.getWidth(), locationAlongSide));
									break;
								default:
									break;
								}
							}

							mod.setDockArea(de, defaultDockArea);
						}
					}
				}
			}
		}
	}

	private static Set<DiagramNode> getNodesToLayoutIncrementally(final DiagramNode node,
			final IncrementalLayoutMode mode, final Set<DiagramNode> results) {
		final boolean alwaysLayoutContainer = mode != IncrementalLayoutMode.LAYOUT_CONTENTS;

		for (final DiagramElement child : node.getDiagramElements()) {
			if (DiagramElementPredicates.isShape(child)) {
				final boolean positionIsSet = child.hasPosition() || !DiagramElementPredicates.isMoveable(child);
				final boolean sizeIsSet = child.hasSize() || !DiagramElementPredicates.isResizeable(child);

				// The position is set but the size isn't, then layout the child.
				// This occurs when a user has created an element using the palette
				if (positionIsSet && !sizeIsSet) {
					results.add(child);
				} else {
					if (sizeIsSet && positionIsSet) {
						getNodesToLayoutIncrementally(child, mode, results);
					} else {
						// If always layout container is specified, layout container
						// If container does not have any layed out shapes, layout container.
						final boolean layoutContainer = alwaysLayoutContainer
								|| !hasLayedOutShapes(node.getDiagramElements());
						if (layoutContainer) {
							results.add(node);
							break;
						} else {
							results.add(child);
						}
					}
				}
			} else if (DiagramElementPredicates.isConnection(child) && alwaysLayoutContainer
					&& child.getStartElement() != null && child.getEndElement() != null) {
				// Only layout the connection if its bendpoints have not been set regardless of whether it has any bendpoints.
				if (!child.isBendpointsSet()) {
					final Optional<Queryable> ancestor = Queryable.getFirstCommonAncestor(
							child.getStartElement().getContainer(), child.getEndElement().getContainer());
					if (ancestor.isPresent()) {
						results.add((DiagramNode) ancestor.get());
					}
				}
			}
		}

		return results;
	}

	private static boolean hasLayedOutShapes(final Collection<DiagramElement> diagramElements) {
		return diagramElements.stream().anyMatch(de -> (de.hasPosition() || !DiagramElementPredicates.isMoveable(de))
				&& (de.hasSize() || !DiagramElementPredicates.isResizeable(de)));
	}


	/**
	 * Sets the ELK properties of elements in the specified layout mapping based on the layout options.
	 * @param layoutMapping
	 * @param options
	 */
	private static void applyProperties(final LayoutMapping layoutMapping, final LayoutOptions options) {
		final IGraphElementVisitor visitor = element -> {
			// Fix the position of the top level ports if the lock top level ports option is set.
			if(element instanceof ElkNode) {
				final ElkNode n = (ElkNode) element;

				PortConstraints portConstraints = PortConstraints.FIXED_SIDE;
				if (n.getPorts().size() == 0) {
					// Don't constrain ports if there aren't any. As of 2017-10-11, FIXED_POS can affect the layout even if the node does not contain ports.
					portConstraints = PortConstraints.FREE;
				}

				n.setProperty(CoreOptions.PORT_CONSTRAINTS, portConstraints);
			} else if (element instanceof ElkPort) {
				final ElkPort p = (ElkPort) element;
				final DiagramElement de = (DiagramElement) layoutMapping.getGraphMap().get(p);


				// If the default docking position is any and has a previously set side, then use the previous port side. Otherwise, use the default port side.
				final DockingPosition defaultDockingPosition = de.getGraphicalConfiguration().defaultDockingPosition;
				final PortSide portSide = getPortSideForNonGroupDockArea(
						(defaultDockingPosition == DockingPosition.ANY && de.getDockArea() != null) ? de.getDockArea()
								: defaultDockingPosition.getDefaultDockArea());

				p.setProperty(CoreOptions.PORT_SIDE, portSide);

				// Set the port border offset based on the port side
				if (PortSide.SIDES_NORTH_SOUTH.contains(portSide)) {
					p.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getHeight());
				} else {
					p.setProperty(CoreOptions.PORT_BORDER_OFFSET, -de.getWidth());
				}
			}
		};

		ElkUtil.applyVisitors(layoutMapping.getLayoutGraph(),visitor);

		// Set the minimum node size based on the ports and their assigned sides.
		final IGraphElementVisitor minNodeSizeVisitor = element -> {
			if (element instanceof ElkNode) {
				final ElkNode n = (ElkNode) element;

				final double maxLabelWidth = n.getLabels().stream().mapToDouble(l -> l.getWidth()).max().orElse(0.0);
				final double maxLabelHeight = n.getLabels().stream().mapToDouble(l -> l.getHeight()).max().orElse(0.0);

				// Adjust the minimum space of the node so that it will be large enough to avoid overlaps when the labels are centered
				final double extraWidthForLabelAlignment;
				if (n.getProperty(CoreOptions.NODE_LABELS_PLACEMENT).contains(NodeLabelPlacement.H_CENTER)) {
					final double maxLeftPortWidth = n.getPorts().stream()
							.filter(p -> p.getProperty(CoreOptions.PORT_SIDE) == PortSide.WEST)
							.mapToDouble(p -> p.getWidth()).max().orElse(0.0);
					final double maxRightPortWidth = n.getPorts().stream()
							.filter(p -> p.getProperty(CoreOptions.PORT_SIDE) == PortSide.EAST)
							.mapToDouble(p -> p.getWidth()).max().orElse(0.0);

					// Add twice the difference between the width of the left and right ports so that ELK will center the labels and non have them overlap with
					// ports. This happens because ports are inside the node due to the PORT_BORDER_OFFSET and ELK centers the labels in the remaining space.
					extraWidthForLabelAlignment = 2 * Math.abs(maxLeftPortWidth - maxRightPortWidth);
				} else {
					extraWidthForLabelAlignment = 0;
				}

				final double minWidth = Math.max(200, maxLabelWidth + extraWidthForLabelAlignment);
				final double minHeight = Math.max(100, maxLabelHeight);

				n.setProperty(CoreOptions.NODE_SIZE_MINIMUM, new KVector(minWidth, minHeight));
			}
		};

		ElkUtil.applyVisitors(layoutMapping.getLayoutGraph(), minNodeSizeVisitor);

	}

	private static boolean isTopLevel(final ElkGraphElement ge) {
		if(ge instanceof ElkPort) {
			final ElkPort p = (ElkPort) ge;
			return p.getParent() == null || p.getParent().getParent() == null;
		} else if(ge instanceof ElkNode) {
			final ElkNode n = (ElkNode) ge;
			return n.getParent() == null || n.getParent().getParent() == null;
		} else {
			return false;
		}
	}

	/**
	 * Returns a list which contains the specified diagram nodes with unnecessary nodes removed.
	 * It removes nodes which are:
	 *   Not a diagram.
	 *   Not shapes
	 *   Elements which have an ancestor in the specified list.
	 *   Children of a docked element.
	 * @param diagramNodes
	 * @return
	 */
	static Collection<DiagramNode> filterUnnecessaryNodes(final
			Collection<? extends DiagramNode> diagramNodes)
	{
		return diagramNodes.stream().filter(dn -> dn instanceof AgeDiagram || (dn instanceof DiagramElement
				&& DiagramElementPredicates.isShape((DiagramElement) dn) && !containsAnyAncestor(diagramNodes, dn)
				&& ((DiagramElement) dn).getDockArea() != DockArea.GROUP))
				.collect(Collectors.toList());
	}

	/**
	 * Returns true if the specified collection contains any ancestor for the specified diagram node
	 * @param c
	 * @param e
	 * @return
	 */
	private static boolean containsAnyAncestor(final Collection<? extends DiagramNode> c, final DiagramNode n) {
		for (DiagramNode ancestor = n.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
			if (c.contains(ancestor)) {
				return true;
			}
		}

		return false;
	}

	private static PortSide getPortSideForNonGroupDockArea(final DockArea dockArea) {
		final PortSide portSide = dockAreaToPortSideMap.get(dockArea);
		if (portSide == null) {
			throw new RuntimeException("Unexpected dock area: " + dockArea);
		}

		return portSide;
	}

	private static void applyShapeLayout(final LayoutMapping mapping, final DiagramModification m) {
		// Modify shapes
		for (Entry<ElkGraphElement, Object> e : mapping.getGraphMap().entrySet()) {
			final ElkGraphElement elkElement = e.getKey();
			final Object mappedValue = e.getValue();
			final boolean isTopLevelElement = isTopLevel(elkElement);

			if (!(elkElement instanceof ElkShape)) {
				continue;
			}
			final ElkShape elkShape = (ElkShape) elkElement;

			if (!(mappedValue instanceof DiagramElement)) {
				continue;
			}

			final DiagramElement de = (DiagramElement) mappedValue;
			if (!(de.getGraphic() instanceof AgeShape)) {
				continue;
			}

			if (de.getGraphic() instanceof Label) {
				continue;
			}

			// Set Position. Don't set the position of top level elements
			if (!isTopLevelElement) {
				// Position all shapes which are not a children of a docked diagram element.
				if (de.getDockArea() != DockArea.GROUP) {
					m.setPosition(de, new Point(elkShape.getX(), elkShape.getY()));
				}

				if (de.getDockArea() != DockArea.GROUP && de.getDockArea() != null) {
					final DockArea newDockArea = dockAreaToPortSideMap.inverse()
							.get(elkShape.getProperty(CoreOptions.PORT_SIDE));
					if (newDockArea != null) {
						m.setDockArea(de, newDockArea);
					}
				}
			}

			m.setSize(de, new Dimension(elkShape.getWidth(), elkShape.getHeight()));
		}
	}
}
