package org.osate.ge.internal.elk;

import java.util.Collection;

import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.ILayoutSetup;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class AgeLayoutSetup implements ILayoutSetup {
	@Override
	public boolean supports(final Object object) {
		// Support collections of diagram nodes
		if (object instanceof Collection) {
			final Collection<?> collection = (Collection<?>) object;
			if (collection.isEmpty()) {
				return false;
			}

			for (Object o : collection) {
				if (!(o instanceof DiagramNode)) {
					return false;
				}
			}
			return true;
		}

		// Support the editor or a single diagram element
		return object instanceof AgeDiagramEditor || object instanceof DiagramNode;
	}

	@Override
	public Injector createInjector(final Module defaultModule) {
		return Guice.createInjector(Modules.override(defaultModule).with(new AgeLayoutModule()));
	}

	public static class AgeLayoutModule implements Module {
		@Override
		public void configure(final Binder binder) {
			binder.bind(IDiagramLayoutConnector.class).to(AgeLayoutConnector.class);
		}
	}

}
