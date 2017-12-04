/*******************************************************************************
 * Copyright (C) 2016 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * This material is based upon work supported by
 * Aviation and Missile Research, Development, and Engineering Command (AMRDEC)
 * under W31P4Q-15-D-0062 TO 0045.
 *******************************************************************************/
package org.osate.ge.internal.businessObjectHandlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.ComponentTypeRename;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.GroupExtension;
import org.osate.aadl2.ImplementationExtension;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PackageSection;
import org.osate.aadl2.Realization;
import org.osate.aadl2.TypeExtension;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.Categories;
import org.osate.ge.GraphicalConfiguration;
import org.osate.ge.GraphicalConfigurationBuilder;
import org.osate.ge.PaletteEntry;
import org.osate.ge.PaletteEntryBuilder;
import org.osate.ge.di.CanCreate;
import org.osate.ge.di.CanDelete;
import org.osate.ge.di.CanRename;
import org.osate.ge.di.Create;
import org.osate.ge.di.GetCreateOwner;
import org.osate.ge.di.GetGraphicalConfiguration;
import org.osate.ge.di.GetName;
import org.osate.ge.di.GetNameForEditing;
import org.osate.ge.di.GetPaletteEntries;
import org.osate.ge.di.IsApplicable;
import org.osate.ge.di.Names;
import org.osate.ge.di.ValidateName;
import org.osate.ge.internal.di.InternalNames;
import org.osate.ge.internal.graphics.AadlGraphics;
import org.osate.ge.internal.services.NamingService;
import org.osate.ge.internal.ui.dialogs.ElementSelectionDialog;
import org.osate.ge.internal.util.AadlClassifierUtil;
import org.osate.ge.internal.util.AadlImportsUtil;
import org.osate.ge.internal.util.ImageHelper;
import org.osate.ge.internal.util.Log;
import org.osate.ge.internal.util.ScopedEMFIndexRetrieval;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.query.StandaloneQuery;
import org.osate.ge.services.QueryService;

import com.google.common.collect.Streams;

public class ClassifierHandler {
	private static final StandaloneQuery packageQuery = StandaloneQuery.create((root) -> root.ancestors().filter((fa) -> fa.getBusinessObject() instanceof AadlPackage));

	@IsApplicable
	@CanDelete
	public boolean isApplicable(final @Named(Names.BUSINESS_OBJECT) Classifier bo) {
		return true;
	}

	@GetPaletteEntries
	public PaletteEntry[] getPaletteEntries(final @Named(Names.DIAGRAM_BO) AadlPackage pkg) {
		return Streams
				.concat(AadlClassifierUtil.getComponentTypes().keySet().stream(),
						AadlClassifierUtil.getComponentImplementations().keySet().stream())
				.sorted((eClass1, eClass2) -> StringUtil.camelCaseToUser(eClass1.getName())
						.compareToIgnoreCase(StringUtil.camelCaseToUser(eClass2.getName()))).map(eClass -> createPaletteEntry(eClass)).toArray(PaletteEntry[]::new);
	}

	private static PaletteEntry createPaletteEntry(final EClass classifierType) {
		return PaletteEntryBuilder.create().label(StringUtil.camelCaseToUser(classifierType.getName())).icon(ImageHelper.getImage(classifierType.getName())).category(Categories.CLASSIFIERS).context(classifierType).build();
	}

	@CanCreate
	public boolean canCreate(final @Named(Names.TARGET_BO) EObject bo, final @Named(Names.PALETTE_ENTRY_CONTEXT) EClass classifierType) {
		return bo instanceof AadlPackage || isValidBaseClassifier(bo, classifierType);
	}

	private boolean isValidBaseClassifier(final EObject containerBo, final EClass classifierType) {
		final EClass containerType = containerBo.eClass();

		// Determine if the container is a valid base classifier
		boolean containerIsValidBaseClassifier = false;
		if(isComponentImplementation(classifierType)) {
			for(final EClass superType : classifierType.getESuperTypes()) {
				if(!Aadl2Factory.eINSTANCE.getAadl2Package().getComponentImplementation().isSuperTypeOf(superType)) {
					if(superType.isSuperTypeOf(containerType)) {
						containerIsValidBaseClassifier = true;
						break;
					}
				}
			}
		} else {
			containerIsValidBaseClassifier = classifierType.isSuperTypeOf(containerType) || Aadl2Factory.eINSTANCE.getAadl2Package().getAbstractType().isSuperTypeOf(containerType);
		}

		return containerIsValidBaseClassifier;
	}

	private EObject determineBaseClassifier(final EObject targetBo, final EClass classifierType, final IProject project) {
		// Determine the base classifier using the container. The base classifier is the classifier that should be extended or implemented(if any)
		final EObject baseClassifier;

		// Determine if the container is a valid base classifier
		boolean containerIsValidBaseClassifier = isValidBaseClassifier(targetBo, classifierType);

		// Set the base classifier
		if(containerIsValidBaseClassifier) {
			baseClassifier = targetBo;
		} else {
			if(isComponentImplementation(classifierType)) {
				final ElementSelectionDialog dlg = new ElementSelectionDialog(Display.getCurrent().getActiveShell(), "Select a Classifier", "Select a classifier to implement or extend.", getValidBaseClassifierDescriptions(project, classifierType));
				if(dlg.open() == Window.CANCEL) {
					return null;
				}
				baseClassifier = (EObject)dlg.getFirstSelectedElement();
			} else {
				baseClassifier = null;
			}
		}

		return (baseClassifier != null && baseClassifier.eIsProxy()) ? EcoreUtil.resolve(baseClassifier, targetBo.eResource()) : baseClassifier;
	}

	@GetCreateOwner
	public BusinessObjectContext getCreateOwner(final @Named(Names.TARGET_BO) EObject targetBo,
			final @Named(Names.TARGET_BUSINESS_OBJECT_CONTEXT) BusinessObjectContext targetBoc,
			final QueryService queryService) {
		if(targetBo instanceof AadlPackage) {
			return targetBoc;
		} else if(targetBo instanceof Classifier) {
			// Get the AadlPackage based on the query. This ensures that the package is the one represented by the diagram rather than the one in which the
			// target business object is contained.
			return queryService.getFirstResult(packageQuery, targetBoc);
		}

		return null;
	}

	/**
	 * Return a list of EObjectDescriptions for classifiers that would be valid "base" classifiers for the current classifierType.
	 * A "base" classifier is one that will be implemented or extended.
	 * Assumes classifier type is a type of component implementation.
	 * @return
	 */
	private List<IEObjectDescription> getValidBaseClassifierDescriptions(final IProject project, final EClass classifierType) {
		final List<IEObjectDescription> objectDescriptions = new ArrayList<IEObjectDescription>();
		for(final IEObjectDescription desc : ScopedEMFIndexRetrieval.getAllEObjectsByType(project, Aadl2Factory.eINSTANCE.getAadl2Package().getComponentClassifier())) {
			// Add objects that have care either types or implementations of the same category as the classifier type
			for(final EClass superType : classifierType.getESuperTypes()) {
				if(!Aadl2Factory.eINSTANCE.getAadl2Package().getComponentImplementation().isSuperTypeOf(superType)) {
					if(superType.isSuperTypeOf(desc.getEClass())) {
						objectDescriptions.add(desc);
						break;
					}
				}
			}
		}

		return objectDescriptions;
	}

	@Create
	public Classifier createBusinessObject(@Named(Names.MODIFY_BO) final AadlPackage pkg, @Named(Names.TARGET_BO) final EObject targetBo,
			final @Named(Names.PALETTE_ENTRY_CONTEXT) EClass classifierType, final @Named(InternalNames.PROJECT) IProject project,
			final NamingService namingService) {
		final EObject baseClassifier = determineBaseClassifier(targetBo, classifierType, project);
		if(baseClassifier == null && isComponentImplementation(classifierType)) {
			return null;
		}

		final PackageSection section = pkg.getPublicSection();
		if(section == null) {
			return null;
		}

		// Create the new classifier
		final Classifier newClassifier = section.createOwnedClassifier(classifierType);

		// Determine the name
		final String newName = buildNewName(section, classifierType, baseClassifier, namingService);
		if(newName == null) {
			return null;
		}

		// Handle implementations
		if(newClassifier instanceof ComponentImplementation) {
			final ComponentImplementation newImpl = (ComponentImplementation)newClassifier;
			if(baseClassifier instanceof ComponentType) {
				final Realization realization = newImpl.createOwnedRealization();
				realization.setImplemented((ComponentType)baseClassifier);
			} else if(baseClassifier instanceof ComponentImplementation) {
				final ComponentImplementation baseImpl = (ComponentImplementation)baseClassifier;
				final ImplementationExtension extension = newImpl.createOwnedExtension();
				extension.setExtended(baseImpl);

				final Realization realization = newImpl.createOwnedRealization();
				realization.setImplemented(baseImpl.getType());
			}
		} else if(newClassifier instanceof ComponentType && baseClassifier instanceof ComponentType) {
			final ComponentType newType = (ComponentType)newClassifier;
			final TypeExtension extension = newType.createOwnedExtension();
			extension.setExtended((ComponentType)baseClassifier);
		} else if(newClassifier instanceof FeatureGroupType && baseClassifier instanceof FeatureGroupType) {
			final FeatureGroupType newFgt = (FeatureGroupType)newClassifier;
			final GroupExtension extension = newFgt.createOwnedExtension();
			extension.setExtended((FeatureGroupType)baseClassifier);
		}

		// Set the name
		newClassifier.setName(newName);

		Log.info("Created classifier with name: " + newClassifier.getName());

		return newClassifier;
	}

	private String buildNewName(final PackageSection section, final EClass classifierType, final Object contextBo, final NamingService namingService) {
		// Determine the appropriate base name. The base name will be used if there are no conflicts
		final String baseName;
		if(isComponentImplementation(classifierType)) {
			final ComponentType componentType;
			if(contextBo instanceof ComponentImplementation) {
				componentType = ((ComponentImplementation)contextBo).getType();
			} else if(contextBo instanceof ComponentType) {
				componentType = (ComponentType)contextBo;
			} else {
				componentType = null;
			}

			if(componentType == null) {
				return null;
			}

			// Resolve name. Add imports as needed
			final String componentTypeName = resolveComponentTypeName(section, componentType, namingService);

			// Make sure the component type has a name
			if(componentTypeName == null) {
				return null;
			}

			baseName = componentTypeName + ".impl";
		} else {
			baseName = "new_classifier";
		}

		// Build the name and check for conflicts
		return namingService.buildUniqueIdentifier(section, baseName);
	}

	private String resolveComponentTypeName(final PackageSection section, final ComponentType ct, final NamingService namingService) {
		// Ensure the component type has a valid namespace
		if(ct.getNamespace() == null) {
			return null;
		}

		// Check if the component type is in the same package
		if (section.getName() != null && section.getName().equalsIgnoreCase(ct.getNamespace().getName())) {
			return ct.getName();
		}

		// Look for an existing component type renames
		for(final ComponentTypeRename ctr : section.getOwnedComponentTypeRenames()) {
			if(ctr.getRenamedComponentType() == ct && ctr.getName() != null) {
				return ctr.getName();
			}
		}

		// Import the package if necessary
		final AadlPackage ctPkg = (AadlPackage)ct.getNamespace().getOwner();
		AadlImportsUtil.addImportIfNeeded(section, ctPkg);

		// Create a new component type rename
		final String ctFullName = ct.getFullName();
		if(ctFullName == null) {
			return null;
		}

		// Determine a unique name for the new rename
		final String baseAlias = ct.getQualifiedName().replace("::","_");
		final String alias = namingService.buildUniqueIdentifier(section, baseAlias);

		final ComponentTypeRename ctr = section.createOwnedComponentTypeRename();
		ctr.setName(alias);
		ctr.setCategory(ct.getCategory());
		ctr.setRenamedComponentType(ct);

		return alias;
	}

	private static boolean isComponentImplementation(EClass classifierType) {
		return Aadl2Factory.eINSTANCE.getAadl2Package().getComponentImplementation().isSuperTypeOf(classifierType);
	}

	@GetGraphicalConfiguration
	public GraphicalConfiguration getGraphicalConfiguration(final @Named(Names.BUSINESS_OBJECT) Classifier bo) {
		return GraphicalConfigurationBuilder.create().
				graphic(AadlGraphics.getGraphic(bo)).
				style(AadlGraphics.getStyle(bo)).
				build();
	}

	@CanRename
	public boolean canRename(final @Named(Names.BUSINESS_OBJECT) Classifier classifier, final @Named(Names.BUSINESS_OBJECT_CONTEXT) BusinessObjectContext boc, final QueryService queryService) {
		return classifierIsOwnedByPackage(classifier, boc, queryService);
	}

	@GetName
	public String getName(final @Named(Names.BUSINESS_OBJECT) Classifier classifier) {
		return classifier.getName();
	}

	@GetNameForEditing
	public String getName(final @Named(Names.BUSINESS_OBJECT) ComponentImplementation ci) {
		return ci.getImplementationName();
	}

	// Returns whether the classifier is owned by the package in which the diagram element is contained.
	private boolean classifierIsOwnedByPackage(final Classifier classifier, final BusinessObjectContext boc, final QueryService queryService) {
		final AadlPackage containingAadlPackage = (AadlPackage)queryService.getFirstBusinessObject(packageQuery, boc);
		if(containingAadlPackage == null || classifier == null || classifier.getNamespace() == null || classifier.getNamespace().getOwner() == null) {
			return false;
		}

		return containingAadlPackage.getQualifiedName().equalsIgnoreCase(((NamedElement)classifier.getNamespace().getOwner()).getQualifiedName()) ? true : false;
	}

	@ValidateName
	public String validateName(final @Named(Names.BUSINESS_OBJECT) Classifier classifier, final @Named(Names.NAME) String value, final NamingService namingService) {
		final String newQualifiedName;
		final String oldName;

		// Transform value so that is is the full name
		if(classifier instanceof ComponentImplementation) {
			final ComponentImplementation ci = (ComponentImplementation)classifier;
			newQualifiedName = ci.getTypeName() + "." + value;
			oldName = ci.getImplementationName();
		} else {
			newQualifiedName = value;
			oldName = classifier.getName();
		}

		// If the name hasn't changed or has only changed case
		if(value.equalsIgnoreCase(oldName)) {
			return null;
		}

		// Check if the value matches the format for AADL identifiers
		if(!namingService.isValidIdentifier(value)) {
			return "The specified name is not a valid AADL identifier";
		}

		// Check for conflicts in the namespace
		if(namingService.isNameInUse(classifier.getNamespace(), newQualifiedName)) {
			return "The specified name conflicts with an existing member of the namespace.";
		}

		// The value is valid
		return null;
	}
}
