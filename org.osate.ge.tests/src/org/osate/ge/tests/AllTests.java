package org.osate.ge.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CreateNewAADLPackageTest.class,
	OpenExistingAADLModelTest.class,
	RenameClassifierTest.class,
	OpenAssociatedDiagramTest.class,
	OpenElementPackageDiagramTest.class,
	GoToTypeDiagramTest.class,
	InstantiateAndOpenImplTest.class,
	DeletingClassifierTest.class,
	SelectingDiagramElementContainerTest.class,
	PerformDiagramLayoutTest.class,
	CreateConnectionTest.class,
	SetExtendedClassifierTest.class,
	SetFeatureClassifierTest.class,
	ModesTransitionTest.class,
	MoveShapeUsingKeysTest.class/*,
	BusinessObjectTreeFactoryTests.class*/ })
public class AllTests {
	//TODO set up and tear down?
}
