package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenExistingAADLModelTest {
	private final SWTGefBot bot = new SWTGefBot();
	private final Helper helper = new Helper(bot);

	@Before
	public void setUp() {
		helper.createNewProjectAndPackage();
	}

	@After
	public void tearDown() {
		helper.deleteProject();
	}

	@Test
	public void openExistingAADLModel() {
		helper.openDiagram(new String[] { ElementNames.projectName, "packages" }, ElementNames.packageName + ".aadl");
		assertTrue(bot.gefEditor(ElementNames.packageName) != null);
	}
}
