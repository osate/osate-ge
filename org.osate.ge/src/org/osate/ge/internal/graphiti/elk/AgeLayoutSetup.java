package org.osate.ge.internal.graphiti.elk;

import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.ILayoutSetup;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class AgeLayoutSetup implements ILayoutSetup {

	public AgeLayoutSetup() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean supports(final Object object) {
		/*
		 * if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            for (Object o : collection) {
                if (o instanceof IPictogramElementEditPart || o instanceof PictogramElement) {
                    return true;
                }
            }
            return false;
        }
        return object instanceof DiagramEditor || object instanceof IPictogramElementEditPart
                || object instanceof PictogramElement;
		 */
		// TODO: Support edit parts but only do so if the editor is an AgeDiagramEditor
		System.err.println("TEST: " + object);
		// TODO Auto-generated method stub
		return object instanceof AgeDiagramEditor;
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
