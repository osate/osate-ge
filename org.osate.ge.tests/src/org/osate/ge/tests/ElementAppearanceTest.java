package org.osate.ge.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.ui.editor.AgeDiagramBehavior;
import org.osate.ge.internal.ui.properties.AppearancePropertySection;

public class ElementAppearanceTest {
	private final AgeGefBot bot = new AgeGefBot();

	@Before
	public void setUp() {
		bot.maximize();
		bot.createNewProjectAndPackage(ElementNames.projectName, ElementNames.packageName);
		bot.openDiagram(new String[] { ElementNames.projectName }, ElementNames.packageName);
		bot.createAbstractTypeAndImplementation(ElementNames.packageName);
	}

	@After
	public void tearDown() {
		bot.deleteProject(ElementNames.projectName);
	}

	@Test
	public void editAppearance() {
		final SWTBotGefEditor editor = bot.getEditor(ElementNames.packageName);
		bot.openPropertiesView(editor, ElementNames.abstractTypeName);
		bot.selectTabbedPropertySection("Appearance");

		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) editor.getEditPart(ElementNames.abstractTypeName)
				.part();
		final AgeDiagramBehavior ageDiagramBehavior = (AgeDiagramBehavior) AgeGefBot.getAgeFeatureProvider(editor)
				.getDiagramTypeProvider().getDiagramBehavior();
		final DiagramElement de = ageDiagramBehavior.getGraphitiAgeDiagram()
				.getDiagramElement(gsep.getPictogramElement());

		bot.clickCombo(AppearancePropertySection.primaryLabelVisibilityCombo, "Hide");
		assertTrue(!de.getStyle().getPrimaryLabelVisible());

		final Double fontSize = de.getStyle().getFontSize();
		bot.clickCombo(AppearancePropertySection.fontSizeCombo, "Large");
		assertTrue(de.getStyle().getFontSize() != fontSize);

		assertTrue(de.getStyle().getOutlineColor() == null);
		bot.clickButtonWithId(AppearancePropertySection.outlineColorId);
		bot.clickButtonIndexWithId(AppearancePropertySection.presetColorId, 0);
		assertTrue(de.getStyle().getOutlineColor() != null);

		assertTrue(de.getStyle().getFontColor() == null);
		bot.clickButtonWithId(AppearancePropertySection.fontColorId);
		bot.clickButtonIndexWithId(AppearancePropertySection.presetColorId, 1);
		assertTrue(de.getStyle().getFontColor() != null);

		assertTrue(de.getStyle().getBackgroundColor() == null);
		bot.clickButtonWithId(AppearancePropertySection.backgroundColorId);
		bot.clickButtonIndexWithId(AppearancePropertySection.presetColorId, 2);
		assertTrue(de.getStyle().getBackgroundColor() != null);

	}
}
