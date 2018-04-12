package org.osate.ge;

import org.osate.aadl2.AadlPackage;
import org.osate.ge.internal.model.PackageProxy;
import org.osate.ge.internal.services.ReferenceService;

// TODO: Stabilize
public class BusinessObjectUtil {
	public static boolean isPackage(final Object bo) {
		return bo instanceof AadlPackage || bo instanceof PackageProxy;
	}

	/**
	 * Returns null if the BO is not a package. Throws an exception if bo is a package proxy which cannot be resolved.
	 * @param bo
	 * @param refService
	 * @return
	 */
	public static AadlPackage getPackage(final Object bo, final ReferenceService refService) {
		final AadlPackage pkg;
		if (bo instanceof AadlPackage) {
			pkg = (AadlPackage) bo;
		} else if (bo instanceof PackageProxy) {
			pkg = ((PackageProxy) bo).resolve(refService);
			if (pkg == null) {
				throw new RuntimeException("Unable to resolve package");
			}
		} else {
			pkg = null;
		}

		return pkg;
	}
}
