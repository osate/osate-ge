package org.osate.ge.internal.ui.dialogs.classifier;

public class ConfiguredClassifierOperation {
	private final ClassifierOperation operation;
	private final Object selectedPackage;
	private final Object selectedElement;
	private final String name;

	public ConfiguredClassifierOperation(final ClassifierOperation operation, final Object selectedPackage, final String name,
			final Object selectedElement) {
		this.operation = operation;
		this.selectedPackage = selectedPackage;
		this.name = name;
		this.selectedElement = selectedElement;
	}

	public ClassifierOperation getOperation() {
		return operation;
	}

	//
	// For create operations
	//
	public Object getSelectedPackage() {
		return selectedPackage;
	}

	public String getName() {
		return name;
	}

	//
	// For selecting existing
	//
	public Object getSelectedElement() {
		return selectedElement;
	}

}