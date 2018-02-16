package org.osate.ge.internal.ui.diagramNavigator;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.osate.ge.internal.diagram.runtime.CanonicalBusinessObjectReference;
import org.osate.ge.internal.services.DiagramService.DiagramReference;

// TODO: Implement equals and hashcode
public class DiagramGroup {
	public static class Builder {
		private Collection<? extends DiagramReference> projectDiagrams;
		private IProject project;
		private String diagramTypeId;
		private boolean contextRefValid = false;
		private CanonicalBusinessObjectReference contextRef;

		// TODO: Consider argument order
		public Builder(final Collection<? extends DiagramReference> projectDiagrams, final IProject project) {
			this.projectDiagrams = Objects.requireNonNull(projectDiagrams, "projectDiagrams must not be null");
			this.project = Objects.requireNonNull(project, "project must not be null");
		}

		public DiagramGroup build() {
			return new DiagramGroup(projectDiagrams, project, diagramTypeId, contextRefValid, contextRef);
		}

		public Builder project(final IProject value) {
			this.project = value;
			return this;
		}

		public Builder diagramType(final String value) {
			this.diagramTypeId = value;
			return this;
		}

		public Builder contextReference(final CanonicalBusinessObjectReference value) {
			this.contextRef = value;
			this.contextRefValid = true;
			return this;
		}
	}

	public static Builder builder(final Collection<? extends DiagramReference> projectDiagrams,
			final IProject project) {
		return new Builder(projectDiagrams, project);
	}

	public static Builder builder(final DiagramGroup group) {
		final Builder builder = builder(group.projectDiagrams, group.project).diagramType(group.diagramTypeId);
		if (group.contextRefValid) {
			builder.contextReference(group.contextRef);
		}

		return builder;
	}

	private final Collection<? extends DiagramReference> projectDiagrams; // All diagrams in the diagrams. Not just the one that matches the group
	private final IProject project;
	private final String diagramTypeId;
	private boolean contextRefValid; // Indicates whether the contextRef field is valid. The context reference will be null for filtering contextless diagrams.
	private final CanonicalBusinessObjectReference contextRef;

	// TODO: Consider argument order
	public DiagramGroup(final Collection<? extends DiagramReference> projectDiagrams, final IProject project,
			final String diagramTypeId, final boolean contextRefValid,
			final CanonicalBusinessObjectReference contextRef) {
		this.projectDiagrams = Objects.requireNonNull(projectDiagrams, "projectDiagrams must not be null");
		this.project = project;
		this.diagramTypeId = diagramTypeId;
		this.contextRefValid = contextRefValid;
		this.contextRef = contextRef;
	}

	public final IProject getProject() {
		return project;
	}

	public final String getDiagramTypeId() {
		return diagramTypeId;
	}

	public final boolean isContextReferenceValid() {
		return contextRefValid;
	}

	public final CanonicalBusinessObjectReference getContextReference() {
		return contextRef;
	}

	public final Stream<? extends DiagramReference> findMatchingDiagramReferences() {
		return projectDiagrams.stream().filter(dr -> (diagramTypeId == null || diagramTypeId.equals(dr.getDiagramTypeId()))
						&& (!contextRefValid || ((contextRef == null && dr.getContextReference() == null)
						|| contextRef.equals(dr.getContextReference()))));
	}
}
