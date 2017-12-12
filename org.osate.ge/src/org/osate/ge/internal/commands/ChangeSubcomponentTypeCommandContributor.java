package org.osate.ge.internal.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
import org.osate.ge.di.Activate;
import org.osate.ge.internal.util.AadlSubcomponentUtil;

public class ChangeSubcomponentTypeCommandContributor {
	@Activate
	public Collection<Object> activate() {
		final Collection<Object> contributedCommands = new ArrayList<>();
		for (final EClass subcomponentType : AadlSubcomponentUtil.getSubcomponentTypes()) {
			contributedCommands.add(new ChangeSubcomponentTypeCommand(subcomponentType));
		}
		return contributedCommands;
	}
}
