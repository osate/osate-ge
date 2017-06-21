package org.osate.ge.tests;


import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateNewAADLPackageTest {
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
	public void createNewAADLPackage() {
		// Node path to package
		final String[] nodePath = new String[] { ElementNames.projectName, "packages" };
		bot.tree().expandNode(nodePath).getNode(ElementNames.packageName + ".aadl").click();
	}
}
