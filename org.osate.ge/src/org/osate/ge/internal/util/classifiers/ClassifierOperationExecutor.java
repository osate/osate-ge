package org.osate.ge.internal.util.classifiers;

import java.util.Objects;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.ComponentTypeRename;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.GroupExtension;
import org.osate.aadl2.ImplementationExtension;
import org.osate.aadl2.PackageSection;
import org.osate.aadl2.Realization;
import org.osate.aadl2.TypeExtension;
import org.osate.ge.internal.services.NamingService;
import org.osate.ge.internal.util.AadlClassifierUtil;
import org.osate.ge.internal.util.AadlImportsUtil;
import org.osate.ge.internal.util.AadlNameUtil;

public class ClassifierOperationExecutor {
	private final ClassifierCreationHelper classifierCreationHelper;

	public ClassifierOperationExecutor(final NamingService namingService, final ResourceSet resourceSet) {
		Objects.requireNonNull(resourceSet, "resourceSet must not be null");
		this.classifierCreationHelper = new ClassifierCreationHelper(namingService, resourceSet);
	}

	public Classifier execute(final ClassifierOperation op) {
		final Classifier baseOperationResult = execute(op.getBasePart(),
				null, null);
		return execute(op.getPrimaryPart(), op.getBasePart(),
				baseOperationResult);
	}

	private Classifier execute(final ClassifierOperationPart part,
			final ClassifierOperationPart basePart, final Classifier baseOperationResult) {
		Objects.requireNonNull(part, "part must not be null");
		Objects.requireNonNull(part.getType(), "operation part type must not be null");

		switch (part.getType()) {
		case EXISTING:
			return classifierCreationHelper.getClassifier(part.getSelectedClassifier());

		case NEW_COMPONENT_IMPLEMENTATION:
		case NEW_COMPONENT_TYPE:
		case NEW_FEATURE_GROUP_TYPE:
			final PackageSection section = classifierCreationHelper.getPublicSection(part.getSelectedPackage());

			// Create the new classifier
			final EClass creationEClass = getCreationEClass(part);
			final Classifier newClassifier = section.createOwnedClassifier(creationEClass);

			// Set name
			newClassifier.setName(classifierCreationHelper.getName(part, basePart));

			// Handle implementations
			if (newClassifier instanceof ComponentImplementation) {
				final ComponentImplementation newImpl = (ComponentImplementation) newClassifier;
				final ComponentType baseComponentType;
				if (baseOperationResult instanceof ComponentType) {
					final Realization realization = newImpl.createOwnedRealization();
					baseComponentType = (ComponentType) baseOperationResult;
					realization.setImplemented(baseComponentType);
				} else if (baseOperationResult instanceof ComponentImplementation) {
					final ComponentImplementation baseImpl = (ComponentImplementation) baseOperationResult;
					final ImplementationExtension extension = newImpl.createOwnedExtension();
					extension.setExtended(baseImpl);

					final Realization realization = newImpl.createOwnedRealization();
					realization.setImplemented(baseImpl.getType());

					baseComponentType = baseImpl.getType();

					// Import the base implementation's package
					final AadlPackage baseImplPkg = (AadlPackage) baseImpl.getNamespace().getOwner();
					AadlImportsUtil.addImportIfNeeded(section, baseImplPkg);
				} else {
					throw new RuntimeException("Invalid base classifier");
				}

				// Get the base component type
				if (baseComponentType == null) {
					throw new RuntimeException("Unable to determine base component type");
				}

				if (!AadlNameUtil.namesAreEqual(section, baseComponentType.getNamespace())) {
					// Import the package if necessary
					final AadlPackage baseComponentTypePkg = (AadlPackage) baseComponentType.getNamespace().getOwner();
					AadlImportsUtil.addImportIfNeeded(section, baseComponentTypePkg);

					// Create an alias for the component type
					final ClassifierCreationHelper.RenamedTypeDetails aliasDetails = classifierCreationHelper
							.getRenamedType(section, baseComponentTypePkg, baseComponentType.getName());
					if (!aliasDetails.exists) {
						final ComponentTypeRename ctr = section.createOwnedComponentTypeRename();
						ctr.setName(aliasDetails.aliasName);
						ctr.setCategory(baseComponentType.getCategory());
						ctr.setRenamedComponentType(baseComponentType);
					}
				}

			} else if (newClassifier instanceof ComponentType && baseOperationResult instanceof ComponentType) {
				final ComponentType newType = (ComponentType) newClassifier;
				final TypeExtension extension = newType.createOwnedExtension();
				extension.setExtended((ComponentType) baseOperationResult);
			} else if (newClassifier instanceof FeatureGroupType && baseOperationResult instanceof FeatureGroupType) {
				final FeatureGroupType newFgt = (FeatureGroupType) newClassifier;
				final GroupExtension extension = newFgt.createOwnedExtension();
				extension.setExtended((FeatureGroupType) baseOperationResult);
			}

			return newClassifier;

		case NONE:
			return null;

		default:
			throw new RuntimeException("Unexpected operation: " + part.getType());
		}

	}

	private static EClass getCreationEClass(final ClassifierOperationPart configuredOp) {
		switch (configuredOp.getType()) {
		case NEW_COMPONENT_TYPE:
			return AadlClassifierUtil.getComponentTypeEClass(configuredOp.getComponentCategory());

		case NEW_COMPONENT_IMPLEMENTATION:
			return AadlClassifierUtil.getComponentImplementationEClass(configuredOp.getComponentCategory());

		case NEW_FEATURE_GROUP_TYPE:
			return Aadl2Package.eINSTANCE.getFeatureGroupType();

		default:
			return null;
		}
	}
}
