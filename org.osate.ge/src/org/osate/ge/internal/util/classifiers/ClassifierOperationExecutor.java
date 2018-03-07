package org.osate.ge.internal.util.classifiers;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ui.resource.XtextLiveScopeResourceSetProvider;
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
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.internal.services.NamingService;
import org.osate.ge.internal.util.AadlClassifierUtil;
import org.osate.ge.internal.util.AadlImportsUtil;
import org.osate.ge.internal.util.AadlNameUtil;
import org.osate.ge.operations.OperationBuilder;
import org.osate.ge.operations.StepResultBuilder;
import org.osate.xtext.aadl2.ui.internal.Aadl2Activator;

import com.google.inject.Injector;

public class ClassifierOperationExecutor {
	private final ClassifierCreationHelper classifierCreationHelper;
	private final IProject project;

	public ClassifierOperationExecutor(final NamingService namingService, final ResourceSet resourceSet,
			final IProject project) {
		Objects.requireNonNull(resourceSet, "resourceSet must not be null");
		this.project = Objects.requireNonNull(project, "project must not be null");
		this.classifierCreationHelper = new ClassifierCreationHelper(namingService, resourceSet);
	}

	public void execute(final OperationBuilder<Object> operation, final ClassifierOperation classifierOp,
			final BusinessObjectContext primaryPkgBoc) {
		// final Supplier<Classifier> baseOperationResult = addStep(createOp, op.getBasePart(), null, () -> null, null);
		// addStep(createOp, op.getPrimaryPart(), op.getBasePart(), baseOperationResult, primaryPkgBoc);

		final OperationBuilder<Classifier> baseOp = addStep(operation, classifierOp.getBasePart(), null, null);
		addStep(baseOp, classifierOp.getPrimaryPart(), classifierOp.getBasePart(), primaryPkgBoc);
	}

	/**
	 *
	 * @param operation
	 * @param part
	 * @param basePart
	 * @param pkgBoc if non-null then the operation result will indicate that the object should be added to the diagram.
	 * @return
	 */
	private OperationBuilder<Classifier> addStep(final OperationBuilder<?> operation,
			final ClassifierOperationPart part,
			final ClassifierOperationPart basePart, final BusinessObjectContext pkgBoc) {
		Objects.requireNonNull(part, "part must not be null");
		Objects.requireNonNull(part.getType(), "operation part type must not be null");

		switch (part.getType()) {
		case EXISTING:
			return operation.transform(prevResult -> StepResultBuilder
					.create(classifierCreationHelper.getResolvedClassifier(part.getSelectedClassifier())).build());

		case NEW_COMPONENT_IMPLEMENTATION:
		case NEW_COMPONENT_TYPE:
		case NEW_FEATURE_GROUP_TYPE:
			final EClass creationEClass = getCreationEClass(part);

			final PackageSection unmodifiableSection = classifierCreationHelper
					.getResolvedPublicSection(part.getSelectedPackage());

			return operation.modifyModel(unmodifiableSection, (tag, prevResult) -> tag, (tag, section, prevResult) -> {
				// Create the new classifier
				final Classifier newClassifier = section.createOwnedClassifier(creationEClass);

				// Set name
				newClassifier.setName(classifierCreationHelper.getName(part, basePart));

				final Classifier baseOperationResult = resolveWithLiveResourceSetOrThrowIfProxy(
						(Classifier) prevResult.getUserValue());

				// Handle component implementations
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
						throw new RuntimeException("Invalid base classifier: " + baseOperationResult);
					}

					// Get the base component type
					if (baseComponentType == null) {
						throw new RuntimeException("Unable to determine base component type");
					}

					if (!AadlNameUtil.namesAreEqual(section, baseComponentType.getNamespace())) {
						// Import the package if necessary
						final AadlPackage baseComponentTypePkg = (AadlPackage) baseComponentType.getNamespace()
								.getOwner();
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
				} else if (newClassifier instanceof FeatureGroupType
						&& baseOperationResult instanceof FeatureGroupType) {
					final FeatureGroupType newFgt = (FeatureGroupType) newClassifier;
					final GroupExtension extension = newFgt.createOwnedExtension();
					extension.setExtended((FeatureGroupType) baseOperationResult);
				}

				final StepResultBuilder<Classifier> resultBuilder = StepResultBuilder.create(newClassifier);

				// Hint for adding to diagram
				if (pkgBoc != null && newClassifier != null) {
					resultBuilder.showNewBusinessObject(pkgBoc, newClassifier);
				}

				return resultBuilder.build();
			});

		case NONE:
			return operation.transform(prevResult -> StepResultBuilder.create((Classifier) null).build());

		default:
			throw new RuntimeException("Unexpected operation: " + part.getType());
		}

	}

// Resolves the classifier using a new resource set. Using the ClassifierCreationHelper to resolve the base classifier does not
// work if the classifier was just created. Throws if the classifier is a proxy and cannot be resolved
	private Classifier resolveWithLiveResourceSetOrThrowIfProxy(final Classifier c) {
		if (c == null || !c.eIsProxy()) {
			return c;
		}

		// Resolve the base classifier using a new resource set. Using the ClassifierCreationHelper to resolve the base classifier does not
		// work if the classifier was just created.
		final Injector injector = Objects.requireNonNull(
				Aadl2Activator.getInstance().getInjector(Aadl2Activator.ORG_OSATE_XTEXT_AADL2_AADL2),
				"Unable to retrieve injector");
		final XtextLiveScopeResourceSetProvider liveResourceSetProvider = Objects.requireNonNull(
				injector.getInstance(XtextLiveScopeResourceSetProvider.class),
				"Unable to retrieve live scope resource set provider");

		final ResourceSet liveResourceSet = liveResourceSetProvider.get(project);

		final Classifier resolvedClassifier = (Classifier) EcoreUtil.resolve(c, liveResourceSet);
		if (resolvedClassifier == null) {
			throw new RuntimeException("Unable to resolve classifier: " + c);
		}

		return resolvedClassifier;
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
