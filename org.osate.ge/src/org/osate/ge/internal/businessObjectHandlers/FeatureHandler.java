package org.osate.ge.internal.businessObjectHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.Access;
import org.osate.aadl2.AccessSpecification;
import org.osate.aadl2.AccessType;
import org.osate.aadl2.ArrayableElement;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.DirectedFeature;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.Element;
import org.osate.aadl2.EventDataSource;
import org.osate.aadl2.EventSource;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.FeaturePrototypeActual;
import org.osate.aadl2.FeaturePrototypeBinding;
import org.osate.aadl2.InternalFeature;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PortSpecification;
import org.osate.aadl2.ProcessorFeature;
import org.osate.aadl2.PrototypeBinding;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.SubprogramProxy;
import org.osate.aadl2.modelsupport.util.ResolvePrototypeUtil;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.Categories;
import org.osate.ge.DockingPosition;
import org.osate.ge.GraphicalConfiguration;
import org.osate.ge.GraphicalConfigurationBuilder;
import org.osate.ge.PaletteEntry;
import org.osate.ge.PaletteEntryBuilder;
import org.osate.ge.di.CanCreate;
import org.osate.ge.di.CanDelete;
import org.osate.ge.di.CanRename;
import org.osate.ge.di.GetGraphicalConfiguration;
import org.osate.ge.di.GetName;
import org.osate.ge.di.GetNameForEditing;
import org.osate.ge.di.GetPaletteEntries;
import org.osate.ge.di.IsApplicable;
import org.osate.ge.di.Names;
import org.osate.ge.di.ValidateName;
import org.osate.ge.graphics.Style;
import org.osate.ge.graphics.StyleBuilder;
import org.osate.ge.graphics.internal.FeatureGraphic;
import org.osate.ge.internal.CreateOperation;
import org.osate.ge.internal.CreateOperation.CreateStepResult;
import org.osate.ge.internal.di.BuildCreateOperation;
import org.osate.ge.internal.di.InternalNames;
import org.osate.ge.internal.graphics.AadlGraphics;
import org.osate.ge.internal.services.NamingService;
import org.osate.ge.internal.ui.dialogs.ElementSelectionDialog;
import org.osate.ge.internal.util.AadlArrayUtil;
import org.osate.ge.internal.util.AadlFeatureUtil;
import org.osate.ge.internal.util.AadlInheritanceUtil;
import org.osate.ge.internal.util.AadlPrototypeUtil;
import org.osate.ge.internal.util.ImageHelper;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.services.QueryService;

public class FeatureHandler {
	@IsApplicable
	@CanRename
	@CanDelete
	public boolean isApplicable(final @Named(Names.BUSINESS_OBJECT) Object bo) {
		return bo instanceof Feature || bo instanceof InternalFeature || bo instanceof ProcessorFeature;
	}

	@GetPaletteEntries
	public PaletteEntry[] getPaletteEntries(final @Named(Names.DIAGRAM_BO) Object diagramBo) {
		if (!BusinessObjectHandlerUtil.diagramSupportsPackageOrClassifiers(diagramBo)) {
			return null;
		}

		final List<PaletteEntry> entries = new ArrayList<PaletteEntry>();
		for (EClass featureType : AadlFeatureUtil.getFeatureTypes()) {
			entries.add(createPaletteEntry(featureType));
		}

		return entries.toArray(new PaletteEntry[entries.size()]);
	}

	private static PaletteEntry createPaletteEntry(final EClass featureType) {
		return PaletteEntryBuilder.create().label(StringUtil.camelCaseToUser(featureType.getName()))
				.icon(ImageHelper.getImage(featureType.getName())).category(Categories.FEATURES).context(featureType)
				.build();
	}

	@CanCreate
	public boolean canCreate(final @Named(Names.TARGET_BO) EObject targetBo,
			final @Named(Names.PALETTE_ENTRY_CONTEXT) EClass featureType) {
		// Return true if there is a potential owner or if the target is a feature group or subcomponent without a classifier.
		// The latter case is needed to allow displaying an error message.
		return getPotentialOwners(targetBo, featureType).size() > 0
				|| isSubcomponentOrFeatureGroupWithoutClassifier(targetBo);
	}

	@BuildCreateOperation
	public void buildCreateOperation(@Named(InternalNames.OPERATION) final CreateOperation createOp,
			@Named(Names.TARGET_BO) final EObject targetBo,
			final @Named(Names.TARGET_BUSINESS_OBJECT_CONTEXT) BusinessObjectContext targetBoc,
			final @Named(Names.PALETTE_ENTRY_CONTEXT) EClass featureType,
			final @Named(Names.DOCKING_POSITION) DockingPosition dockingPosition,
			final @Named(InternalNames.PROJECT) IProject project,
			final QueryService queryService, final NamingService namingService) {

		if (isSubcomponentOrFeatureGroupWithoutClassifier(targetBo)) {
			final String targetDescription = targetBo instanceof NamedElement
					? ("The element '" + ((NamedElement) targetBo).getQualifiedName() + "'")
							: "The target element";
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Classifier Not Set",
							targetDescription
							+ " does not have a classifier. Please set a classifier before creating a feature.");
		} else {
			final List<Classifier> potentialOwners = getPotentialOwners(targetBo, featureType);
			if (potentialOwners.size() == 0) {
				throw new RuntimeException("Unable to find potential owners");
			}

			// Determine which classifier should own the new element
			final Classifier selectedClassifier;
			if (potentialOwners.size() > 1) {
				// Prompt the user for the classifier
				final ElementSelectionDialog dlg = new ElementSelectionDialog(Display.getCurrent().getActiveShell(),
						"Select a Classifier to Modify", "Select a classifier to modify.", potentialOwners);
				dlg.setInitialSelections(new Object[] { potentialOwners.get(0) });
				if (dlg.open() == Window.CANCEL) {
					return;
				}

				selectedClassifier = (Classifier) dlg.getFirstSelectedElement();
			} else {
				selectedClassifier = potentialOwners.get(0);
			}

			// Create the feature
			createOp.addStep(selectedClassifier, (resource, owner) -> {
				final String newFeatureName = namingService.buildUniqueIdentifier(owner, "new_feature");

				final NamedElement newFeature = AadlFeatureUtil.createFeature(owner, featureType);
				newFeature.setName(newFeatureName);

				// Set in or out based on target docking position
				final boolean isRight = dockingPosition == DockingPosition.RIGHT;
				if (newFeature instanceof DirectedFeature) {
					if (!(newFeature instanceof FeatureGroup)) {
						final DirectedFeature newDirectedFeature = (DirectedFeature) newFeature;
						newDirectedFeature.setIn(!isRight);
						newDirectedFeature.setOut(isRight);
					}
				} else if (newFeature instanceof Access) {
					final Access access = (Access) newFeature;
					access.setKind(isRight ? AccessType.PROVIDES : AccessType.REQUIRES);
				}

				if (owner instanceof ComponentType) {
					((ComponentType) owner).setNoFeatures(false);
				}

				return new CreateStepResult(targetBoc, newFeature);
			});
		}
	}

	/**
	 * Returns potential owners. If there are multiple potential owners, the first one is guaranteed to be the most specific and should be the default value.
	 * @param targetBo
	 * @param featureType
	 * @return
	 */
	private static List<Classifier> getPotentialOwners(final EObject targetBo, final EClass featureType) {
		// Check if the target can own the feature type
		if ((targetBo instanceof FeatureGroupType || targetBo instanceof ComponentType
				|| targetBo instanceof ComponentImplementation)
				&& AadlFeatureUtil.canOwnFeatureType((Classifier) targetBo, featureType)) {
			return Collections.singletonList((Classifier) targetBo);
		}

		final List<Classifier> results = new ArrayList<>();

		// Look for other options if the target cannot own the feature.
		if (targetBo instanceof ComponentImplementation) {
			final ComponentType ct = ((ComponentImplementation) targetBo).getType();
			addPotentialOwnersForClassifierAndExtended(ct, featureType, results);
		} else if (targetBo instanceof Subcomponent) {
			final ComponentClassifier subcomponentClassifier = ((Subcomponent) targetBo).getClassifier();
			addPotentialOwnersForClassifierAndExtended(subcomponentClassifier, featureType, results);
			if (subcomponentClassifier instanceof ComponentImplementation) {
				addPotentialOwnersForClassifierAndExtended(((ComponentImplementation) subcomponentClassifier).getType(),
						featureType, results);
			}
		} else if (targetBo instanceof FeatureGroup) {
			final FeatureGroupType fgType = ((FeatureGroup) targetBo).getFeatureGroupType();
			addPotentialOwnersForClassifierAndExtended(fgType, featureType, results);
		}

		return results;
	}

	private static void addPotentialOwnersForClassifierAndExtended(final Classifier c, final EClass featureType,
			final List<Classifier> results) {
		if (c != null) {
			for (final Classifier tmpClassifier : c.getSelfPlusAllExtended()) {
				if (AadlFeatureUtil.canOwnFeatureType(tmpClassifier, featureType)) {
					results.add(tmpClassifier);
				}
			}
		}
	}

	private static boolean isSubcomponentOrFeatureGroupWithoutClassifier(final Object bo) {
		if (bo instanceof Subcomponent) {
			return ((Subcomponent) bo).getClassifier() == null;
		} else if (bo instanceof FeatureGroup) {
			return ((FeatureGroup) bo).getFeatureGroupType() == null;
		}

		return false;
	}

	@GetGraphicalConfiguration
	public GraphicalConfiguration getGraphicalConfiguration(final @Named(Names.BUSINESS_OBJECT) NamedElement feature,
			final @Named(Names.BUSINESS_OBJECT_CONTEXT) BusinessObjectContext featureBoc) {
		final FeatureGraphic graphic = getGraphicalRepresentation(feature, featureBoc);
		return GraphicalConfigurationBuilder.create().graphic(graphic)
				.annotation(AadlGraphics.getFeatureAnnotation(feature.eClass()))
				.style(StyleBuilder
						.create(AadlInheritanceUtil.isInherited(featureBoc) ? Styles.INHERITED_ELEMENT : Style.EMPTY)
						.backgroundColor(AadlGraphics.getDefaultBackgroundColor(graphic.featureType)).labelsAboveTop()
						.labelsLeft()
						.build())
				.defaultDockingPosition(getDefaultDockingPosition(feature, featureBoc)).build();
	}

	private FeatureGraphic getGraphicalRepresentation(NamedElement feature, BusinessObjectContext featureBoc) {
		// Check to see if it is a prototype feature
		if (feature instanceof AbstractFeature) {
			final AbstractFeature af = (AbstractFeature) feature;
			if (af.getFeaturePrototype() != null) {
				// Lookup the binding
				// Get the proper context (FeatureGroupType or ComponentClassifier) - May be indirectly for example from Subcomponent...
				final Element bindingContext = AadlPrototypeUtil.getPrototypeBindingContext(featureBoc);
				if (bindingContext != null) {
					final PrototypeBinding binding = ResolvePrototypeUtil
							.resolveFeaturePrototype(af.getFeaturePrototype(), bindingContext);
					if (binding instanceof FeaturePrototypeBinding) {
						FeaturePrototypeActual actual = ((FeaturePrototypeBinding) binding).getActual();
						if (actual instanceof PortSpecification) {
							final DirectionType direction = getDirection(actual, featureBoc);
							return AadlGraphics.getFeatureGraphic(((PortSpecification) actual).getCategory(),
									direction);
						} else if (actual instanceof AccessSpecification) {
							final DirectionType direction = getDirection(actual, featureBoc);
							return AadlGraphics.getFeatureGraphic(((AccessSpecification) actual).getCategory(),
									direction);
						}
					}
				}
			}
		}

		final DirectionType direction = getDirection(feature, featureBoc);
		return AadlGraphics.getFeatureGraphic(feature.eClass(), direction);
	}

	private DockingPosition getDefaultDockingPosition(NamedElement feature, BusinessObjectContext featureBoc) {
		final DirectionType direction = getDirection(feature, featureBoc);
		if(direction == DirectionType.IN) {
			return DockingPosition.LEFT;
		} else if(direction == DirectionType.OUT) {
			return DockingPosition.RIGHT;
		} else {
			return DockingPosition.ANY;
		}
	}

	/**
	 *
	 * @param feature a feature or feature specification
	 * @return
	 */
	private DirectionType getDirection(final Element feature, final BusinessObjectContext featureBoc) {
		DirectionType direction;
		if (feature instanceof DirectedFeature) {
			direction = ((DirectedFeature) feature).getDirection();
		} else if (feature instanceof PortSpecification) {
			direction = ((PortSpecification) feature).getDirection();
		} else if (feature instanceof Access) {
			direction = ((Access) feature).getKind() == AccessType.PROVIDES ? DirectionType.OUT : DirectionType.IN;
		} else if (feature instanceof AccessSpecification) {
			direction = ((AccessSpecification) feature).getKind() == AccessType.PROVIDES ? DirectionType.OUT
					: DirectionType.IN;
		} else if (feature instanceof EventSource || feature instanceof EventDataSource
				|| feature instanceof SubprogramProxy) {
			direction = DirectionType.IN;
		} else {
			direction = DirectionType.IN_OUT;
		}

		// Invert the feature as appropriate
		if (AadlFeatureUtil.isFeatureInverted(featureBoc)) {
			if (direction == DirectionType.IN) {
				direction = DirectionType.OUT;
			} else if (direction == DirectionType.OUT) {
				direction = DirectionType.IN;
			}
		}

		return direction;
	}

	@GetName
	public String getName(final @Named(Names.BUSINESS_OBJECT) NamedElement feature) {
		String name = feature.getName() == null ? "" : feature.getName();

		if (feature instanceof ArrayableElement) {
			name += AadlArrayUtil.getDimensionUserString((ArrayableElement) feature);
		}

		return name;
	}

	@GetNameForEditing
	public String getNameForEditing(final @Named(Names.BUSINESS_OBJECT) NamedElement feature) {
		return feature.getName();
	}

	@ValidateName
	public String validateName(final @Named(Names.BUSINESS_OBJECT) NamedElement feature,
			final @Named(Names.NAME) String value, final NamingService namingService) {
		return namingService.checkNameValidity(feature, value);
	}
}
