package org.osate.ge.internal.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

public class PathUtil {
	public static URL determinePath(final String path) {
		try {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path)).getRawLocationURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Cannot find file.");
		}
	}
}
