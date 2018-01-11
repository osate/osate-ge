package org.osate.ge.internal.ui.dialogs.classifier;

public enum ClassifierOperation {
	NEW_COMPONENT_TYPE, NEW_COMPONENT_IMPLEMENTATION, NEW_FEATURE_GROUP_TYPE, EXISTING;

	public static boolean isCreate(ClassifierOperation op) {
		return op == ClassifierOperation.NEW_COMPONENT_TYPE || op == ClassifierOperation.NEW_COMPONENT_IMPLEMENTATION
				|| op == ClassifierOperation.NEW_FEATURE_GROUP_TYPE;
	}
}