package org.osate.ge.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.context.impl.DirectEditingContext;
import org.eclipse.graphiti.internal.util.T;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.osate.aadl2.AbstractType;
import org.osate.ge.internal.services.BusinessObjectResolutionService;
import org.osate.ge.internal.services.impl.DefaultBusinessObjectResolutionService;

public class CreateAADLProjectTest {
	final private SWTGefBot bot = new SWTGefBot();
	final private String projectName = "AADLProject";
	final private String toolType = "Abstract Type";  // "Abstract Type" is the label of the tool in palette
	final private String newName = "new_value";
	
	@Test
	public void renameClassifierTest() {
		bot.menu("File").menu("New").menu("Other...").click();
		bot.tree().getTreeItem("AADL").expand().getNode("AADL Project").click();
		
		sleepForSecond();
		bot.button("Next >").click();
		sleepForSecond();
		bot.textWithLabel("&Project name:").setText(projectName);
		sleepForSecond();
		bot.button("Finish").click();
		bot.button("Yes").click();
		bot.viewByTitle("Outline").show();
		
		sleepForSecond();
		bot.viewByTitle("AADL Navigator").show();
		sleepForSecond();

		bot.toolbarButtonWithTooltip("&Restore").click();
		sleepForSecond();

		bot.tree().getTreeItem(projectName).select().contextMenu("New").menu("Other...").click();
		bot.tree().getTreeItem("AADL").expand().getNode("Aadl Package (Graphical)").click();
		bot.button("Next >").click();
		bot.textWithLabel("Enter the new package's name:").setText(projectName);
		bot.button("Finish").click();
		bot.editorByTitle(projectName).show();

		final SWTBotGefEditor editor = bot.gefEditor(projectName);
		editor.activateTool(toolType);
		editor.drag(55, 55, 0, 0);
		
		sleepForSecond();
		
		final GraphitiShapeEditPart test = (GraphitiShapeEditPart)editor.mainEditPart().part();
		final BusinessObjectResolutionService bor = new DefaultBusinessObjectResolutionService(test.getFeatureProvider());
		List<SWTBotGefEditPart> getActivatedTools = editor.editParts(new MatchTool(bor));
		final GraphitiShapeEditPart shapeEditPart = (GraphitiShapeEditPart)getActivatedTools.get(0).part();
		System.err.println(getActivatedTools.get(0).children().size() + " size");
		System.err.println(shapeEditPart.getChildren().size());
		System.err.println(bor.getBusinessObjectForPictogramElement(shapeEditPart.getPictogramElement()) + " PEEE");
		//for(GraphicsAlgorithm o : shapeEditPart.getPictogramElement().getGraphicsAlgorithm().getGraphicsAlgorithmChildren()) {
			//if(o instanceof Text) {
		//}
	//	getActivatedTools.get(0).descendants(new GetText(bor));
		System.err.println(shapeEditPart.getChildren().size() + " 0");
		for(Object t : getActivatedTools.get(0).part().getChildren()) {
			System.err.println(t + " t");
		}
		//editor.click(getActivatedTools.get(0));
		//getActivatedTools.get(0).click(new Point(0,0));
		//editor.drag(getActivatedTools.get(0), 25, 10);
		bot.sleep(1000);
		editor.select(getActivatedTools.get(0));
		//getActivatedTools.get(0).click(/*new Point(0,0)*/);
		//editor.click(0,0);
		bot.sleep(1000);
		//getActivatedTools.get(0).click(/*new Point(0,0)*/);
		getActivatedTools.get(0).activateDirectEdit();
		editor.click(35, 10);
		bot.sleep(1000);
		final PictogramElement pe = shapeEditPart.getPictogramElement();
		final DirectEditingContext directEditingContext = Objects.requireNonNull(getDirectEditingContext(pe), "Direct Editing Context cannot be null.");
		final IDirectEditingFeature directEditingFeature = shapeEditPart.getFeatureProvider().getDirectEditingFeature(directEditingContext);
		//directEditingFeature.setValue(newName, directEditingContext);
		//getActivatedTools.get(0).activateDirectEdit(directEditingFeature);
		getActivatedTools.get(0).click(new Point(20, 5));
		bot.sleep(100);
		getActivatedTools.get(0).click(new Point(20, 5));
		//editor.click(35, 5);
		//bot.sleep(1000);
		//editor.click(35, 5);
		//bot.sleep(1000);
		//editor.click(35, 10);
		//editor.click(getActivatedTools.get(0));
		//bot.sleep(10000);
		//directEditingFeature.execute(directEditingContext);
		bot.sleep(10000);

		assertTrue(true);
	}
	
	private void sleepForSecond() {
		bot.sleep(1000);
	}
	
	//Matcher test = allOf(widgetOfType(SWTBotGefEditPart.cl))

	private class MatchTool implements Matcher<GraphitiShapeEditPart> {
		final private BusinessObjectResolutionService bor;
		
		private MatchTool(final BusinessObjectResolutionService bor) {
			this.bor = bor;
		}
		
		@Override
		public void describeTo(Description description) {}
		
		@Override
		public boolean matches(Object item) {
			final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)item;
			System.err.println(bor.getBusinessObjectForPictogramElement(gsep.getPictogramElement()) + " bor.pe");
			System.err.println(gsep.getPictogramElement().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().size() + " gaChildren,size()");
			System.err.println(gsep.getClass() + " class " + gsep.getPictogramElement().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().size() + " size");
			getDirectEditingContext(gsep.getPictogramElement());
			if(bor.getBusinessObjectForPictogramElement(gsep.getPictogramElement()) instanceof AbstractType) {
				return true;
			}
			
			return false;
		}
		
		@Override
		public void describeMismatch(Object item, Description mismatchDescription) {}
		
		@Override
		public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}
	}
	
	private class GetText implements Matcher<EditPart> {
		final private BusinessObjectResolutionService bor;
		
		private GetText(final BusinessObjectResolutionService bor) {
			this.bor = bor;
		}
		
		@Override
		public void describeTo(Description description) {}
		
		@Override
		public boolean matches(Object item) {
			System.err.println("GetText");
			//final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart)item;
			//System.err.println(gsep.getPictogramElement().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().size() + " gaChildren,size()");
			//System.err.println(gsep.getClass() + " class " + gsep.getPictogramElement().getGraphicsAlgorithm().getGraphicsAlgorithmChildren().size() + " size");
			//getDirectEditingContext(gsep.getPictogramElement());
			//if(bor.getBusinessObjectForPictogramElement(gsep.getPictogramElement()) instanceof AbstractType) {
			//	return true;
		//	}
			
			return false;
		}
		
		@Override
		public void describeMismatch(Object item, Description mismatchDescription) {}
		
		@Override
		public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}
	}
	
	private static DirectEditingContext getDirectEditingContext(final PictogramElement pe) {
		if(pe instanceof ContainerShape) {
			for(final Shape shape : ((ContainerShape)pe).getChildren()) {
				final GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
				for(final GraphicsAlgorithm childGa : ga.getGraphicsAlgorithmChildren()) {
					if(childGa instanceof Text) {
						return new DirectEditingContext(ga.getPictogramElement(), childGa);
					}
				}

				if(ga instanceof Text) {
					return new DirectEditingContext(ga.getPictogramElement(), ga);
				}
			}
		} else if(pe instanceof Connection) {
			final Connection con = (Connection)pe;
			for(final ConnectionDecorator conDecorator : con.getConnectionDecorators()) {
				if(conDecorator.isActive()) {
					if(conDecorator.getGraphicsAlgorithm() instanceof Text) {
						return new DirectEditingContext(conDecorator, conDecorator.getGraphicsAlgorithm());
					}
				}
			}
		}

		return null;
	}
}
