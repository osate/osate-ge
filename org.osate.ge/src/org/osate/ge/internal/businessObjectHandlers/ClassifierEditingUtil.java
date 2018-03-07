package org.osate.ge.internal.businessObjectHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.Element;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.internal.ui.dialogs.ElementSelectionDialog;
import org.osate.ge.operations.OperationBuilder;
import org.osate.ge.operations.StepResultBuilder;

public class ClassifierEditingUtil {
	public static final OperationBuilder<ComponentImplementation> modifyComponentImplementation(
			final OperationBuilder<?> operation,
			final Element targetBo, final Predicate<? super ComponentImplementation> filter) {
		return operation.transform(prevResult -> {
			if (targetBo instanceof Subcomponent) {
				final Subcomponent tmpSc = (Subcomponent) targetBo;
				if (!(tmpSc.getClassifier() instanceof ComponentImplementation)) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Component Implementation Not Set",
							"The subcomponent '" + tmpSc.getQualifiedName()
							+ "' does not have a component implementation set. Set a component implementation before creating a subcomponent.");
					// return;
					// TODO
				}
			}

			// Determine which classifier should own the new element
			final ComponentImplementation selectedClassifier = (ComponentImplementation) getClassifierToModify(
					getPotentialComponentImplementations(targetBo, filter));
			if (selectedClassifier == null) {
//			return;
				// TODO
			}

			return StepResultBuilder.create(selectedClassifier).build(); // TODO: Result... abort, etc
		});
	}

	public static List<ComponentImplementation> getPotentialComponentImplementations(final Element bo,
			final Predicate<? super ComponentImplementation> filter) {
		if (bo instanceof ComponentImplementation) {
			final ComponentImplementation ci = (ComponentImplementation) bo;
			if (filter.test(ci)) {
				return Collections.singletonList(ci);
			} else {
				return Collections.emptyList();
			}
		} else if (bo instanceof Subcomponent) {
			final ComponentImplementation ci = ((Subcomponent) bo).getComponentImplementation();
			if (ci == null) {
				return Collections.emptyList();
			} else {
				return ci.getSelfPlusAllExtended().stream()
						.filter(tmp -> tmp instanceof ComponentImplementation
								&& filter.test(ci))
						.map(ComponentImplementation.class::cast).collect(Collectors.toList());
			}
		} else {
			return Collections.emptyList();
		}
	}

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
			addSelfAndExtendedClassifierTypes(ct, results);
		} else if (bo instanceof Subcomponent) {
			final ComponentType subcomponentClassifier = ((Subcomponent) bo).getComponentType();
			addSelfAndExtendedClassifierTypes(subcomponentClassifier, results);
		} else if (bo instanceof FeatureGroup) {
			final FeatureGroupType fgType = ((FeatureGroup) bo).getFeatureGroupType();
			addSelfAndExtendedClassifierTypes(fgType, results);
		}

		return results;
	}

	private static void addSelfAndExtendedClassifierTypes(final Classifier c, final List<Classifier> results) {
		if (c != null) {
			for (final Classifier tmpClassifier : c.getSelfPlusAllExtended()) {
				if (tmpClassifier instanceof ComponentType || tmpClassifier instanceof FeatureGroupType) {
					results.add((Classifier) tmpClassifier);
				}
			}
		}
	}

	/**
	 * Returns a list of component classifiers for editing based on a specified business object.
	 * If the specified object is a component classifier, only it is returned.
	 * @param bo
	 * @return
	 */
	public static List<ComponentClassifier> getPotentialComponentClassifiersForEditing(final EObject bo) {
		final List<ComponentClassifier> results = new ArrayList<>();

		// Retrieve relevant component types
		if (bo instanceof ComponentType || bo instanceof ComponentImplementation) {
			// If the bo is a classifier type or implementation, just return it.
			results.add((ComponentClassifier) bo);
		} else if (bo instanceof Subcomponent) {
			final ComponentClassifier subcomponentClassifier = ((Subcomponent) bo).getClassifier();
			addComponentClassifierAndExtended(subcomponentClassifier, results);
			if (subcomponentClassifier instanceof ComponentImplementation) {
				addComponentClassifierAndExtended(((ComponentImplementation) subcomponentClassifier).getType(),
						results);
			}
		}

		return results;
	}

	private static void addComponentClassifierAndExtended(final ComponentClassifier c,
			final List<ComponentClassifier> results) {
		if (c != null) {
			for (final Classifier tmpClassifier : c.getSelfPlusAllExtended()) {
				if (tmpClassifier instanceof ComponentClassifier) {
					results.add((ComponentClassifier) tmpClassifier);
				}
			}
		}
	}

	public static Classifier getClassifierToModify(final List<? extends Classifier> potentialClassifiers) {
		return getClassifierToModify(potentialClassifiers, false);
	}

	/**
	 * Returns the classifier from the specified list which should be modified. If there are multiple classifiers in the specified list,
	 * the user will be prompted to select one. The first element will be the default. Must be called from the UI thread.
	 * @param potentialClassifiers must have at least one element. If empty, an exception will be thrown.
	 * @return the classifier to modify. Will return null if the user cancels the selection prompt.
	 */
	public static Classifier getClassifierToModify(final List<? extends Classifier> potentialClassifiers,
			final boolean forcePrompt) {
		if (potentialClassifiers.isEmpty()) {
			throw new RuntimeException("potentialClassifiers is empty");
		}

		// Determine which classifier should own the new element
		final Classifier selectedClassifier;
		if (forcePrompt || potentialClassifiers.size() > 1) {
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

	public static boolean isSubcomponentWithoutClassifier(final Object bo) {
		if (bo instanceof Subcomponent) {
			return ((Subcomponent) bo).getClassifier() == null;
		}

		return false;
	}

	public static boolean isSubcomponentOrFeatureGroupWithoutClassifier(final Object bo) {
		if (bo instanceof Subcomponent) {
			return ((Subcomponent) bo).getClassifier() == null;
		} else if (bo instanceof FeatureGroup) {
			return ((FeatureGroup) bo).getFeatureGroupType() == null;
		}

		return false;
	}

	/**
	 * Returns true if the element was a subcomponent or feature group without classifier and the error was showing.
	 * @param bo
	 * @return
	 */
	public static boolean showMessageIfSubcomponentOrFeatureGroupWithoutClassifier(final Object bo,
			final String secondaryMsg) {
		final boolean showMsg = isSubcomponentOrFeatureGroupWithoutClassifier(bo);
		if (ClassifierEditingUtil.isSubcomponentOrFeatureGroupWithoutClassifier(bo)) {
			final String targetDescription = bo instanceof NamedElement
					? ("The element '" + ((NamedElement) bo).getQualifiedName() + "'")
							: "The target element";
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Classifier Not Set",
							targetDescription + " does not have a classifier. " + secondaryMsg);
		}

		return showMsg;
	}
}
