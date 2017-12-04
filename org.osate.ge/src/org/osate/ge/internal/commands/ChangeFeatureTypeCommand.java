package org.osate.ge.internal.commands;

import javax.inject.Named;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.DataSubcomponentType;
import org.osate.aadl2.EventDataPort;
import org.osate.aadl2.Feature;
import org.osate.aadl2.InternalFeature;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.ProcessorFeature;
import org.osate.ge.BusinessObjectContext;
import org.osate.ge.di.Activate;
import org.osate.ge.di.CanActivate;
import org.osate.ge.di.GetLabel;
import org.osate.ge.di.IsAvailable;
import org.osate.ge.di.Names;
import org.osate.ge.internal.di.GetBusinessObjectToModify;
import org.osate.ge.internal.util.AadlFeatureUtil;
import org.osate.ge.internal.util.StringUtil;
import org.osate.ge.query.StandaloneQuery;
import org.osate.ge.services.QueryService;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class ChangeFeatureTypeCommand {
	private static final StandaloneQuery parentQuery = StandaloneQuery.create((root) -> root.ancestor(1));
	private final EClass featureType;

	public ChangeFeatureTypeCommand(final EClass featureType) {
		this.featureType = featureType;
	}

	@GetLabel
	public String getLabel() {
		return "Convert to " + StringUtil.camelCaseToUser(featureType.getName());
	}

	@IsAvailable
	public boolean isAvailable(@Named(Names.BUSINESS_OBJECT) final NamedElement feature,
			@Named(Names.BUSINESS_OBJECT_CONTEXT) final BusinessObjectContext boc,
			final QueryService queryService) {
		// Check that the shape represents a feature and that the classifier can contain features of the type this feature changes features into.
		final Object parent = queryService.getFirstBusinessObject(parentQuery, boc);
		if (!((feature instanceof Feature || feature instanceof InternalFeature || feature instanceof ProcessorFeature)
				&& parent instanceof Classifier)) {
			return false;
		}

		return feature.getContainingClassifier() == parent
				&& AadlFeatureUtil.canOwnFeatureType(feature.getContainingClassifier(), featureType)
				&&
				(!(feature instanceof Feature) || (((Feature)feature).getRefined() == null || ((Feature)feature).getRefined() instanceof AbstractFeature));
	}

	/**
	 *  Only allow when the feature is owned by the container
	 */
	@CanActivate
	public boolean canActivate(@Named(Names.BUSINESS_OBJECT) final NamedElement feature) {
		// Check that the feature is not already of the target type
		return feature.eClass() != featureType;
	}

	@GetBusinessObjectToModify
	public Object getBusinessObjectToModify(@Named(Names.BUSINESS_OBJECT) final NamedElement feature) {
		return feature.getContainingClassifier();
	}

	@Activate
	public boolean activate(@Named(Names.BUSINESS_OBJECT) final NamedElement feature) {
		final Classifier featureOwner = feature.getContainingClassifier();
		final NamedElement replacementFeature = AadlFeatureUtil.createFeature(featureOwner, featureType);

		// Copy structural feature values to the replacement object.
		PropertySectionUtil.transferStructuralFeatureValues(feature, replacementFeature);

		// Handle copying the data feature classifier
		if(replacementFeature instanceof EventDataPort) {
			((EventDataPort) replacementFeature).setDataFeatureClassifier(getDataFeatureClassifier(feature));
		} else if(replacementFeature instanceof DataPort) {
			((DataPort) replacementFeature).setDataFeatureClassifier(getDataFeatureClassifier(feature));
		}

		// Remove the old object
		EcoreUtil.remove(feature);

		return true;
	}

	private DataSubcomponentType getDataFeatureClassifier(final NamedElement feature) {
		if(feature instanceof EventDataPort) {
			return ((EventDataPort)feature).getDataFeatureClassifier();
		} else if(feature instanceof DataPort) {
			return ((DataPort)feature).getDataFeatureClassifier();
		}

		return null;
	}
}
