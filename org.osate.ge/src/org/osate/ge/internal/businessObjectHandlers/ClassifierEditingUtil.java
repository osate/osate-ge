package org.osate.ge.internal.businessObjectHandlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.internal.ui.dialogs.ElementSelectionDialog;

public class ClassifierEditingUtil {
	/**
	 * Returns a list of component types and feature group types for editing based on a specified business object.
	 * If the specified object is a component type, only it is returned.
	 * @param bo
	 * @return
	 */
	public static List<Classifier> getPotentialClassifierTypesForEditing(final EObject bo) {
		final List<Classifier> results = new ArrayList<>();

		// Retrieve relevant component types
		if (bo instanceof ComponentType || bo instanceof FeatureGroupType) {
			// If the bo is a classifier type, just return it.
			results.add((Classifier) bo);
		} else if (bo instanceof ComponentImplementation) {
			final ComponentType ct = ((ComponentImplementation) bo).getType();
			addPotentialClassifierTypesForClassifierAndExtended(ct, results);
		} else if (bo instanceof Subcomponent) {
			final ComponentType subcomponentClassifier = ((Subcomponent) bo).getComponentType();
			addPotentialClassifierTypesForClassifierAndExtended(subcomponentClassifier, results);
			if (subcomponentClassifier instanceof ComponentImplementation) {
				addPotentialClassifierTypesForClassifierAndExtended(
						((ComponentImplementation) subcomponentClassifier).getType(),
						results);
			}
		} else if (bo instanceof FeatureGroup) {
			final FeatureGroupType fgType = ((FeatureGroup) bo).getFeatureGroupType();
			addPotentialClassifierTypesForClassifierAndExtended(fgType, results);
		}

		return results;
	}

	private static void addPotentialClassifierTypesForClassifierAndExtended(final Classifier c,
			final List<Classifier> results) {
		if (c != null) {
			for (final Classifier tmpClassifier : c.getSelfPlusAllExtended()) {
				if (tmpClassifier instanceof ComponentType) {
					results.add((ComponentType) tmpClassifier);
				}
			}
		}
	}

	/**
	 * Returns the classifier from the specified list which should be modified. If there are multiple classifiers in the specified list,
	 * the user will be prompted to select one. The first element will be the default. Must be called from the UI thread.
	 * @param potentialClassifiers must have at least one element. If empty, an exception will be thrown.
	 * @return the classifier to modify. Will return null if the user cancels the selection prompt.
	 */
	public static Classifier getClassifierToModify(final List<? extends Classifier> potentialClassifiers) {
		if (potentialClassifiers.isEmpty()) {
			throw new RuntimeException("potentialClassifiers is empty");
		}

		// Determine which classifier should own the new element
		final Classifier selectedClassifier;
		if (potentialClassifiers.size() > 1) {
			// Prompt the user for the classifier
			final ElementSelectionDialog dlg = new ElementSelectionDialog(Display.getCurrent().getActiveShell(),
					"Select a Classifier to Modify", "Select a classifier to modify.", potentialClassifiers);
			dlg.setInitialSelections(new Object[] { potentialClassifiers.get(0) });
			if (dlg.open() == Window.CANCEL) {
				return null;
			}

			selectedClassifier = (Classifier) dlg.getFirstSelectedElement();
		} else {
			selectedClassifier = potentialClassifiers.get(0);
		}

		return selectedClassifier;
	}
}
