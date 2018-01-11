package org.osate.ge.internal.ui.dialogs;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.osate.aadl2.Aadl2Factory;
import org.osate.ge.internal.ui.dialogs.classifier.ClassifierOperation;
import org.osate.ge.internal.ui.dialogs.classifier.CreateSelectClassifierDialog;
import org.osate.ge.internal.util.ScopedEMFIndexRetrieval;

public class DefaultSelectClassifierDialogModel implements CreateSelectClassifierDialog.Model {
	private final IProject project;

	public DefaultSelectClassifierDialogModel(final IProject project) {
		this.project = Objects.requireNonNull(project, "project must not be null");
	}

	@Override
	public String getTitle() {
		return "Select Element";
	}

	@Override
	public String getMessage() {
		return "Select an element.";
	}

	@Override
	public Collection<?> getPackageOptions() {
		return ScopedEMFIndexRetrieval
				.getAllEObjectsByType(project, Aadl2Factory.eINSTANCE.getAadl2Package().getAadlPackage()).stream()
				.filter(od -> od.getEObjectURI() != null && !od.getEObjectURI().isPlatformPlugin())
				.collect(Collectors.toList());
	}

	@Override
	public String getPrimarySelectTitle() {
		return "";
	}

	@Override
	public String getPrimarySelectMessage() {
		return "";
	}

	@Override
	public Collection<?> getPrimarySelectOptions() {
		return Collections.emptyList();
	}

	@Override
	public Collection<?> getUnfilteredPrimarySelectOptions() {
		return Collections.emptyList();
	}

	@Override
	public Collection<?> getBaseSelectOptions(final ClassifierOperation primaryOperation) {
		return Collections.emptyList();
	}

	@Override
	public Collection<?> getUnfilteredBaseSelectOptions(final ClassifierOperation primaryOperation) {
		return Collections.emptyList();
	}
}
