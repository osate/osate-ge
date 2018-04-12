package org.osate.ge.internal.model;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.osate.aadl2.AadlPackage;
import org.osate.ge.internal.services.ProjectReferenceService;
import org.osate.ge.internal.services.ReferenceService;
import org.osate.ge.internal.services.impl.DeclarativeReferenceBuilder;

public class PackageProxy {
	private final String name;
	private final IProject project;

	public PackageProxy(final String name, final IProject project) {
		this.name = Objects.requireNonNull(name, "name must not be null");
		this.project = Objects.requireNonNull(project, "project must not be null");
	}

	// TODO: Rename name to qualified name?
	public final String getName() {
		return name;
	}

	public AadlPackage resolve(final ReferenceService refService) {
		final ProjectReferenceService projectReferenceService = refService.getProjectReferenceService(project);
		final Object resolvedPackage = projectReferenceService.resolve(DeclarativeReferenceBuilder.buildPackageCanonicalReference(name));
		return resolvedPackage instanceof AadlPackage ? ((AadlPackage) resolvedPackage) : null;
	}
}
