// Based on OSATE Graphical Editor. Modifications are:
/*
Copyright (c) 2016, Rockwell Collins.
Developed with the sponsorship of Defense Advanced Research Projects Agency (DARPA).

Permission is hereby granted, free of charge, to any person obtaining a copy of this data,
including any software or models in source or binary form, as well as any drawings, specifications,
and documentation (collectively "the Data"), to deal in the Data without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Data, and to permit persons to whom the Data is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Data.

THE DATA IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS, SPONSORS, DEVELOPERS, CONTRIBUTORS, OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE DATA OR THE USE OR OTHER DEALINGS IN THE DATA.
 */
/*******************************************************************************
 * Copyright (C) 2013 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The US Government has unlimited rights in this work in accordance with W31P4Q-10-D-0092 DO 0073.
 *******************************************************************************/
package org.osate.ge.internal.diagram.runtime.layout;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.osate.ge.DockingPosition;
import org.osate.ge.graphics.Point;
import org.osate.ge.internal.Activator;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramElementPredicates;
import org.osate.ge.internal.diagram.runtime.DiagramModification;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.DockArea;
import org.osate.ge.internal.preferences.Preferences;
import org.osate.ge.internal.query.Queryable;

/**
 * Performs incremental layout on the diagram.
 */

// TODO: Document.Contents will layout container if it doesn't have anything already layed out.
public class IncrementalLayoutUtil {
	private static final String layoutLabel = "Incremental Layout";

	public enum IncrementalLayoutMode {
		LAYOUT_DIAGRAM("org.osate.ge.layout.incremental.mode.diagram"), LAYOUT_CONTAINER(
				"org.osate.ge.layout.incremental.mode.container"), LAYOUT_CONTENTS(
						"org.osate.ge.layout.incremental.mode.contents");

		public final String id;

		IncrementalLayoutMode(final String id) {
			this.id = id;
		}

		public static Optional<IncrementalLayoutMode> getById(final String id) {
			for(final IncrementalLayoutMode mode : IncrementalLayoutMode.values()) {
				if(mode.id.equals(id)) {
					return Optional.of(mode);
				}
			}

			return Optional.empty();
		}
	}

	public static void layout(final AgeDiagram diagram, final DiagramModification mod) {
		final IncrementalLayoutMode currentLayoutMode = IncrementalLayoutMode
				.getById(Activator.getDefault().getPreferenceStore()
						.getString(Preferences.INCREMENTAL_LAYOUT_MODE))
				.orElse(IncrementalLayoutMode.LAYOUT_CONTENTS);

		final boolean preferLayoutContainer = currentLayoutMode != IncrementalLayoutMode.LAYOUT_CONTENTS;
		final List<DiagramNode> nodesToLayout = DiagramElementLayoutUtil
				.filterUnusedNodes(getDiagramNodesToLayout(diagram, preferLayoutContainer, new HashSet<>()));

		if (nodesToLayout.size() == 0) {
			return;
		}

		if (currentLayoutMode == IncrementalLayoutMode.LAYOUT_DIAGRAM) {
			DiagramElementLayoutUtil.layout(layoutLabel, diagram, new LayoutOptionsBuilder().build());
		} else {
			DiagramElementLayoutUtil.layout(mod, nodesToLayout, new LayoutOptionsBuilder().build());

			// Set Position. Need to do this when just laying out contents
			// TODO: Improve algorithm.
			for (final DiagramNode dn : nodesToLayout) {
				if (dn instanceof DiagramElement) {
					final DiagramElement de = (DiagramElement) dn;
					if (!de.hasPosition()) {
						if (de.getDockArea() == null) {
							mod.setPosition(de, new Point(0.0, 0.0));
						} else if (de.getDockArea() != DockArea.GROUP && de.getParent() instanceof DiagramElement) {
							final DiagramElement parent = (DiagramElement)de.getParent();
							final DockingPosition defaultDockingPosition = de
									.getGraphicalConfiguration().defaultDockingPosition;
							final DockArea defaultDockArea = defaultDockingPosition.getDockArea();

							if(parent.hasSize()) {
								// TODO: Rename?
								final Stream<DiagramElement> others = parent.getDiagramElements().stream()
										.filter(c -> c.hasPosition() && c.hasSize()
												&& c.getDockArea() == defaultDockArea);

								double t; // TODO: Rename
								if (defaultDockingPosition == DockingPosition.TOP
										|| defaultDockingPosition == DockingPosition.BOTTOM) {
									t = others.max(Comparator.comparingDouble(c -> c.getY()))
											.map(c -> c.getX() + c.getWidth()).orElse(0.0);
								} else {
									t = others.max(Comparator.comparingDouble(c -> c.getY()))
											.map(c -> c.getY() + c.getHeight()).orElse(0.0);
								}

								// TODO: Need padding
								// TODO: Will this cause parent to resize?

								// Set position based on the docking position
								switch (defaultDockingPosition) {
								case TOP:
									mod.setPosition(de, new Point(t, 0));
									break;
								case BOTTOM:
									mod.setPosition(de, new Point(t, parent.getHeight()));
									break;
								case LEFT:
									mod.setPosition(de, new Point(0, t));
									break;
								case RIGHT:
									mod.setPosition(de, new Point(parent.getWidth(), t));
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

	// TODO: Rename. Similar to what is in LayoutUtil
	// TODO: Document what alwaysLayoutContainer is. Could pass mode instead? Rename? prefer?
	private static Set<DiagramNode> getDiagramNodesToLayout(final DiagramNode node,
			final boolean alwaysLayoutContainer,
			final Set<DiagramNode> results) {

		for (final DiagramElement child : node.getDiagramElements()) {
			// TODO: Handle case there the node is added after the child is.. Don't want to layout both the child and the parent.
			if (DiagramElementPredicates.isShape(child)) {
				// TODO: Rename
				final boolean positionIsSet = child.hasPosition() || !DiagramElementPredicates.isMoveable(child);
				final boolean sizeIsSet = child.hasSize() || !DiagramElementPredicates.isResizeable(child);

				// The position is set but the size isn't, then layout the child.
				// This occurs when a user has created an element using the palette
				if (positionIsSet && !sizeIsSet) {
					results.add(child);
				} else {
					if (sizeIsSet && positionIsSet) {
						getDiagramNodesToLayout(child, alwaysLayoutContainer, results);
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
}
