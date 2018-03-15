package org.osate.ge.tests;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.impl.AbstractTypeImpl;
import org.osate.aadl2.impl.NamedElementImpl;
import org.osate.ge.internal.graphiti.AgeFeatureProvider;
import org.osate.ge.internal.graphiti.ShapeNames;
import org.osate.ge.internal.graphiti.diagram.PropertyUtil;
import org.osate.ge.internal.ui.dialogs.ClassifierOperationDialog;

public class AgeGefBot {
	final SWTGefBot bot = new SWTGefBot();
	// Context menu options
	final public static String associatedDiagram = "Associated Diagram";
	final public static String allFilters = "All Filters";

//	public void createNewProjectAndPackage(final String projectName, final String packageName) {
//		closeWelcomePage();
//		final SWTBotMenu newMenu = bot.menu("Other...", true).click();
//		bot.tree().getTreeItem("AADL").expand().getNode("AADL Project").click();
//		bot.button("Next >").click();
//		bot.text().setText(projectName);
//		bot.button("Finish").click();
//
//		if (!bot.activePerspective().getLabel().equals("AADL")) {
//			// Open AADL Perspective Dialog
//			bot.button("Open Perspective").click();
//		}
//
//		// Create AADL Package
//		newMenu.click();
//		bot.tree().getTreeItem("AADL").expand().getNode("AADL Package").click();
//		bot.button("Next >").click();
//		bot.text().setText(packageName);
//		bot.radio("Graphical Editor").click();
//		bot.button("Finish").click();
//		bot.button("OK").click();
//
//		// Close editor for open test
//		bot.gefEditor(packageName + ".aadl_diagram").close();
//
//		bot.tree().expandNode(new String[] { projectName }).getNode(packageName + ".aadl")
//				.click();
//		bot.tree().contextMenu("Open").click();
//	}

	public void createNewProjectAndPackage(final String projectName, final String packageName) {
		closeWelcomePage();
		bot.menu("Other...", true).click();
		bot.tree().getTreeItem("AADL").expand().getNode("AADL Project").click();
		bot.button("Next >").click();
		bot.text().setText(projectName);
		bot.button("Finish").click();

		if (!bot.activePerspective().getLabel().equals("AADL")) {
			// Open AADL Perspective Dialog
			bot.button("Open Perspective").click();
		}

		// Create AADL Package
		createAADLPackage(projectName, packageName);

		// Close editor for open test
		bot.gefEditor(packageName + ".aadl_diagram").close();

		bot.tree().expandNode(new String[] { projectName }).getNode(packageName + ".aadl")
				.click();
		bot.tree().contextMenu("Open").click();
	}


	public void createAADLPackage(final String projectName, final String packageName) {
		bot.tree().select(projectName).contextMenu("AADL Package").click();

		// bot.tree().getTreeItem("AADL").expand().getNode("AADL Package").click();
		// bot.menu("AADL Package").click();
		// bot.button("Next >").click();
		bot.text().setText(packageName);
		bot.radio("Graphical Editor").click();
		bot.button("Finish").click();
		bot.button("OK").click();
	}

	public void closeWelcomePage() {
		for (final SWTBotView view : bot.views()) {
			if (view.getTitle().equals("Welcome")) {
				view.close();
			}
		}
	}

	public void clickButton(final String text) {
			bot.button(text).click();
	}

	public void clickTableOption(final String text) {
		bot.table().getTableItem(text).click();
	}

	// TODO: treeNot used rightnow
	public void clickTreeOption(final String text) {
		bot.tree().getTreeItem(text).click();
	}

	public void clickRadio(final String text) {
		bot.radio(text).click();
	}

	public void createImplementation(final SWTBotGefEditor editor, final String parentName, final String toolType,
			final String typeName,
			final String elementName,
			final Point point) {
		editor.setFocus();
		createToolItem(editor, parentName, toolType, point);
		waitUntilShellIsActive("Create Component Implementation");
		setText(elementName);
		clickRadio("Existing");
		clickButton("...");
		clickButton("OK");
		clickButton("OK");

		waitUntilElementExists(editor, typeName + "." + elementName);
	}

	public void waitUntil(final ICondition condition, final long timeout) {
		bot.waitUntil(condition, timeout);
	}

	public void deleteProject(final String projectName) {
		bot.tree().select(projectName).contextMenu("Delete").click();
		bot.checkBox("Delete project contents on disk (cannot be undone)").click();
		final SWTBotShell shell = bot.activeShell();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
		bot.saveAllEditors();
		bot.closeAllEditors();
		bot.perspectiveByLabel("Resource").activate();
	}

	public List<? extends SWTBotEditor> getEditors() {
		return bot.editors();
	}

	public void openDiagram(final String[] nodePath, final String fileName) {
		bot.tree().expandNode(nodePath).getNode(fileName + ".aadl").click();
		bot.tree().contextMenu("Open Diagram").click();
	}

	public void waitUntilShellIsActive(final String shellTitle) {
		bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive(shellTitle));
	}

	public SWTBotShell getActiveShell() {
		return bot.activeShell();
	}

	public void waitUntilShellCloses(final SWTBotShell shell) {
		bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses(shell));
	}

	public void createToolItem(final SWTBotGefEditor editor, final String parentName, final String toolItem,
			final Point p) {
		final GraphitiShapeEditPart parent = (GraphitiShapeEditPart) editor
				.editParts(new FindEditPart(parentName, getAgeFeatureProvider(editor))).get(0)
				.part();
		final GraphicsAlgorithm containerGA = parent.getPictogramElement().getGraphicsAlgorithm();
		editor.setFocus();
		editor.activateTool(toolItem);
		editor.click(containerGA.getX() + p.x, containerGA.getY() + p.y);
	}

	public void createToolItemAndRename(final SWTBotGefEditor editor, final String parentName, final String toolItem,
			final Point p, final Class<?> clazz, final String newName) {
		createToolItem(editor, parentName, toolItem, p);
		waitUntilNewElementIsCreated(editor, clazz);
		renameElement(editor, newName);
	}

	public void waitUntilNewElementIsCreated(final SWTBotGefEditor editor, final Class<?> clazz) {
		waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return clazz + " not created";
			};

			@Override
			public boolean test() throws Exception {
				return !editor.editParts(new NewElementMatcher(editor)).isEmpty();
			};
		}, 5000);
	}

	public void waitUntilElementExists(final SWTBotGefEditor editor, final String elementName) {
		final AgeFeatureProvider ageFeatureProvider = getAgeFeatureProvider(editor);
		waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return "Element not renamed: " + elementName;
			};

			@Override
			public boolean test() throws Exception {
				return !editor.editParts(new FindEditPart(elementName, ageFeatureProvider)).isEmpty();
			};
		}, 5000);
	}

	public SWTBotTree getTree() {
		return bot.tree();
	}

	public SWTBotGefEditor getEditor(final String editor) {
		return bot.gefEditor(editor + ".aadl_diagram");
	}

	public SWTBotEditor getActiveEditor() {
		return bot.activeEditor();
	}

	public void sleep(final int secs) {
		bot.sleep(secs * 1000);
	}

	public boolean isActiveEditor(final String title) {
		return bot.activeEditor().getTitle().equalsIgnoreCase(title + ".aadl_diagram");
	}

	public static class NewElementMatcher extends BaseMatcher<EditPart> {
		final private CharSequence charSeq = "new_";
		final private AgeFeatureProvider ageFeatureProvider;

		public NewElementMatcher(final SWTBotGefEditor editor) {
			this.ageFeatureProvider = getAgeFeatureProvider(editor);
		}

		@Override
		public boolean matches(final Object item) {
			if (item instanceof GraphitiShapeEditPart) {
				final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) item;
				final Object element = ageFeatureProvider
						.getBusinessObjectForPictogramElement(gsep.getPictogramElement());
				if (element instanceof NamedElement && gsep.isActive()) {
					final NamedElement namedElement = (NamedElement) element;
					return namedElement.getName().contains(charSeq);
				}
			}

			return false;
		}

		@Override
		public void describeTo(Description description) {
		}
	}

	public void selectElement(final SWTBotGefEditor editor, final String elementName) {
		editor.select(editor.editParts(new FindEditPart(elementName, getAgeFeatureProvider(editor))));
	}

	// TODO cmd needed? click buttons probably different for other diagram opening types.
	// hard code "Associated Diagram" and change method name
	public void openAssociatedDiagramFromContextMenu(final SWTBotGefEditor editor, final String elementName) {
		final List<SWTBotGefEditPart> editPart = editor
				.editParts(new FindEditPart(elementName, getAgeFeatureProvider(editor)));
		editor.select(editPart.get(0)).clickContextMenu("Associated Diagram");
		clickButton("Yes");
		clickButton("OK");
	}

	public void executeContextMenuCommand(final SWTBotGefEditor editor, final String elementName,
			final String contextMenuCmd) {
		// Set focus for properties filters
		final AgeFeatureProvider ageFeatureProvider = (AgeFeatureProvider) ((GraphitiShapeEditPart) editor
				.mainEditPart().part()).getFeatureProvider();
		final List<SWTBotGefEditPart> list = editor.editParts(new FindEditPart(elementName, ageFeatureProvider));
		editor.setFocus();
		editor.select(list.get(0)).clickContextMenu(contextMenuCmd);
	}

	public void setText(final String text) {
		bot.text().setText(text);
	}

	public void setTextWithId(final String id, final String text) {
		bot.textWithId(id).setText(text);
	}

	public void maximize() {
		bot.getDisplay()
				.syncExec(() -> bot.getDisplay().getActiveShell().setMaximized(true));
	}

	public SWTBotView getActiveView() {
		return bot.activeView();
	}

	public Widget getWidget(final String name) {
		return bot.widget(new SelectControlMatcher(name));
	}

	public void selectTabbedPropertySection(final String widgetName) {
		final Widget widget = getWidget(widgetName);
		Assert.assertTrue("widget is not a control", widget instanceof Control);
		selectControl((Control) widget);
	}

	public void selectControl(final Control widget) {
		try {
			final Robot robot = new Robot();
			robot.setAutoDelay(25);
			getActiveEditor().getWidget().getDisplay().syncExec(() -> {
				final Control c = widget;
				final Point point = PlatformUI.getWorkbench().getDisplay().map(c.getParent(), null, c.getLocation().x,
						c.getLocation().y);
				robot.mouseMove(point.x, point.y);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			});
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public class SelectControlMatcher extends BaseMatcher<Widget> {
		final String controlName;

		public SelectControlMatcher(final String controlName) {
			this.controlName = controlName;
		}
		@Override
		public boolean matches(final Object item) {
			return item.toString().equalsIgnoreCase(controlName);
		};

		@Override
		public void describeTo(org.hamcrest.Description description) {
		};
	}

	public void setElementOptionRadioInPropertiesView(final SWTBotGefEditor editor, final String elementName,
			final String viewTitle,
			final String tabTitle, final String option) {
		doubleClickElement(editor, elementName);
		bot.waitUntil(new DefaultCondition() {
			@Override
			public boolean test() throws Exception {
				return getActiveView().getTitle().equalsIgnoreCase(viewTitle);
			};

			@Override
			public String getFailureMessage() {
				return "view not opened";
			};
		}, 5000);

		selectTabbedPropertySection(tabTitle);
		clickRadio(option);
	}

	public void setElementOptionButtonInPropertiesView(final SWTBotGefEditor editor, final String elementName, final String viewTitle,
			final String tabTitle, final String option) {
		openPropertiesView(editor, elementName, viewTitle);
		selectTabbedPropertySection(tabTitle);
		clickButton(option);
	}

	public void openPropertiesView(final SWTBotGefEditor editor, final String elementName, final String viewTitle) {
		doubleClickElement(editor, elementName);
		bot.waitUntil(new DefaultCondition() {
			@Override
			public boolean test() throws Exception {
				return getActiveView().getTitle().equalsIgnoreCase(viewTitle);
			};

			@Override
			public String getFailureMessage() {
				return "view not opened";
			};
		}, 5000);
	}

	/* private */public static AgeFeatureProvider getAgeFeatureProvider(final SWTBotGefEditor editor) {
		return (AgeFeatureProvider) ((GraphitiShapeEditPart) editor.mainEditPart().part()).getFeatureProvider();
	}

	public static class FindEditPart extends BaseMatcher<EditPart> {
		final String editPartName;
		final AgeFeatureProvider ageFeatureProvider;

		public FindEditPart(final String editPartName, final AgeFeatureProvider ageFeatureProvider) {
			this.editPartName = editPartName;
			this.ageFeatureProvider = ageFeatureProvider;
		}
		@Override
		public void describeTo(final Description description) {
		}

		@Override
		public boolean matches(final Object item) {
			if (item instanceof GraphitiShapeEditPart) {
				final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) item;
				final Object element = ageFeatureProvider
						.getBusinessObjectForPictogramElement(gsep.getPictogramElement());
				if (element instanceof NamedElementImpl) {
					final NamedElementImpl namedElement = (NamedElementImpl) element;
					System.err.println(namedElement.getName() + " namedElement");
					System.err.println(editPartName + " editPartname");
					System.err.println(editPartName.equalsIgnoreCase(namedElement.getName()));
					return editPartName.equalsIgnoreCase(namedElement.getName());
				}
			}

			return false;
		}
	}

	public void showEditor(final SWTBotGefEditor editor) {
		editor.show();
		bot.waitUntil(new DefaultCondition() {
			@Override
			public String getFailureMessage() {
				return "editor not active";
			};

			@Override
			public boolean test() throws Exception {
				return getActiveEditor().getTitle().equalsIgnoreCase(editor.getTitle());
			};
		}, 10000);
	}

	public void resize(final SWTBotGefEditor editor, final String elementName, final Point newSize) {
		editor.setFocus();
		final SWTBotGefEditPart swtBotGefEditPart = getEditPart(editor, elementName);
		final Rectangle bounds = ((GraphitiShapeEditPart) swtBotGefEditPart.part()).getFigure().getBounds();
		editor.click(swtBotGefEditPart);

		try {
			final Robot robot = new Robot();
			final Display display = editor.getWidget().getDisplay();
			display.syncExec(() -> {
				final Point point = PlatformUI.getWorkbench().getDisplay()
						.map(display.getFocusControl(), null, bounds.x, bounds.y);
				robot.setAutoDelay(25);
				robot.mouseMove(point.x, point.y);

				final Point bottomRightCorner = new Point(point.x + bounds.width, point.y + bounds.height);
				robot.mouseMove(bottomRightCorner.x, bottomRightCorner.y);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

				final Point newLocation = new Point(newSize.x - bounds.width, newSize.y - bounds.height);
				robot.mouseMove(bottomRightCorner.x + newLocation.x, bottomRightCorner.y + newLocation.y);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseMove(300, 300);
			});
		} catch (final AWTException e) {

		}
	}

	public SWTBotGefEditPart getEditPart(final SWTBotGefEditor editor, final String elementName) {
		return editor.editParts(new FindEditPart(elementName, getAgeFeatureProvider(editor))).get(0);
	}

	public List<SWTBotGefEditPart> findChild(final SWTBotGefEditor editor, final SWTBotGefEditPart parent,
			final String childName) {
		return parent.descendants(new FindEditPart(childName, getAgeFeatureProvider(editor)));
	}

	public static Object getBusinessObject(final SWTBotGefEditor editor, final PictogramElement pe) {
		return getAgeFeatureProvider(editor).getBusinessObjectForPictogramElement(pe);
	}

	private void doubleClickElement(final SWTBotGefEditor editor, final String elementName) {
		editor.setFocus();
		final SWTBotGefEditPart element = editor
				.editParts(new FindEditPart(elementName, AgeGefBot.getAgeFeatureProvider(editor))).get(0);
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) element.part();
		try {
			final Robot robot = new Robot();
			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final Rectangle bounds = gsep.getFigure().getBounds();
				final Point point = PlatformUI.getWorkbench().getDisplay()
						.map(canvas, null, bounds.x, bounds.y);

				robot.mouseMove(point.x - canvas.getHorizontalBar().getSelection() + 10,
						point.y - canvas.getVerticalBar().getSelection() + 10);
				// Double click
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			});

			robot.mouseMove(300, 300);
		} catch (final AWTException e) {
			throw new RuntimeException(e);
		}
	}

	public void clickConnection(final SWTBotGefEditor editor, final Connection connection) {
		editor.setFocus();
		final org.eclipse.draw2d.geometry.Point midPoint = connection.getPoints().getMidpoint();
		try {
			final Robot robot = new Robot();
			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final Point point = PlatformUI.getWorkbench().getDisplay().map(canvas, null,
						midPoint.x, midPoint.y);

				robot.mouseMove(point.x - canvas.getHorizontalBar().getSelection(),
						point.y - canvas.getVerticalBar().getSelection());

				// Click
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			});

			robot.mouseMove(300, 300);
		} catch (final AWTException e) {
			throw new RuntimeException(e);
		}
	}

	public void clickButtonWithId(final String id) {
		bot.buttonWithId(id).click();
	}

	public void clickButtonIndexWithId(final String id, int i) {
		bot.buttonWithId(id, i).click();
	}

	public void clickCombo(final String id, final String selection) {
		bot.comboBoxWithId(id).setSelection(selection);
	}

	public void clickCheckBoxWithId(final String id) {
		bot.checkBoxWithId(id).click();
	}

	public void renameElement(final SWTBotGefEditor editor,
			final String newName) {
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) editor
				.editParts(new AgeGefBot.NewElementMatcher(editor)).get(0).part();
		final ContainerShape cs = (ContainerShape) gsep.getPictogramElement();
		final Shape labelShape = getLabelShape(cs);
		final GraphicsAlgorithm labelGA = labelShape.getGraphicsAlgorithm();

		RenameHelper.renameElement(editor, newName,
				new Point(labelGA.getX() + labelGA.getWidth() / 2, labelGA.getY() + labelGA.getHeight() / 2));
		waitUntilElementExists(editor, newName);
	}

	public void selectNewElement(final SWTBotGefEditor editor) {
		final SWTBotGefEditPart gsep = editor.editParts(new AgeGefBot.NewElementMatcher(editor)).get(0);
		editor.select(gsep);
	}

	public void renameConnection(final SWTBotGefEditor editor, final PictogramElement pe, final Connection connection,
			final String newName) {
		final ConnectionDecorator cd = getLabelShape((FreeFormConnection) pe);
		final GraphicsAlgorithm labelGA = cd.getGraphicsAlgorithm();

		RenameHelper.renameConnection(editor, newName, connection,
				new Point(labelGA.getX() + labelGA.getWidth() / 2, labelGA.getY() + labelGA.getHeight() / 2));
		waitUntilElementExists(editor, newName);
	}

	private ConnectionDecorator getLabelShape(final FreeFormConnection ffc) {
		for (final ConnectionDecorator cd : ffc.getConnectionDecorators()) {
			if (ShapeNames.primaryLabelShapeName.equalsIgnoreCase(PropertyUtil.getName(cd))) {
				return cd;
			}
		}

		return null;
	}

	private static Shape getLabelShape(final ContainerShape cs) {
		for (final Shape shape : cs.getChildren()) {
			if (ShapeNames.primaryLabelShapeName.equalsIgnoreCase(PropertyUtil.getName(shape))) {
				return shape;
			}
		}

		return null;
	}

	public void createAbstractTypeAndImplementation(final String packageName) {
		final SWTBotGefEditor editor = getEditor(packageName);
		editor.setFocus();
		resize(editor, ElementNames.packageName, new Point(600, 600));

		createToolItem(editor, ElementNames.packageName, ToolTypes.abstractType, new Point(25, 25));
		waitUntilNewElementIsCreated(editor, AbstractTypeImpl.class);
		renameElement(editor, ElementNames.abstractTypeName);
		waitUntilElementExists(editor, ElementNames.abstractTypeName);
		createImplementation(editor, ElementNames.packageName, ToolTypes.abstractImplementation,
				ElementNames.abstractTypeName, "impl", new Point(100, 200));
	}

	public void createTypeAndImplementation(final SWTBotGefEditor editor, final Point point, final String packageName,
			final String implName, final String typeName, final String impl) {
		createToolItem(editor, packageName, impl, point);
		waitUntilShellIsActive("Create Component Implementation");
		setTextWithId(ClassifierOperationDialog.primaryPartIdentifier, implName);
		clickRadio("New Component Type");
		setTextWithId(ClassifierOperationDialog.baseValueIdentifier, typeName);
		clickButton("OK");
		waitUntilElementExists(editor, typeName + "." + implName);
	}

	public void clickElement(final SWTBotGefEditor editor, final String elementName) {

//		editor.editParts(new BaseMatcher<EditPart>() {
//			@Override
//			public boolean matches(Object item) {
//				System.err.println(item + " item");
//				if (item instanceof GraphitiShapeEditPart) {
//					final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) item;
//					if (getAgeFeatureProvider(editor)
//							.getBusinessObjectForPictogramElement(gsep.getPictogramElement()) instanceof NamedElement) {
//						final NamedElement ne = (NamedElement) getAgeFeatureProvider(editor)
//								.getBusinessObjectForPictogramElement(gsep.getPictogramElement());
//						System.err.println(ne.getName() + " GETNAME");
//					}
//					System.err.println(getAgeFeatureProvider(editor)
//							.getBusinessObjectForPictogramElement(gsep.getPictogramElement()));
//				}
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public void describeTo(Description description) {
//				// TODO Auto-generated method stub
//
//			}
//		});

		final List<SWTBotGefEditPart> list = editor
				.editParts(new FindEditPart(elementName, getAgeFeatureProvider(editor)));

		System.err.println(list.size() + " size");
		System.err.println(list.get(0) + " get0");
		System.err.println(list.get(0).part() + " editPart");
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) list.get(0).part();

//		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) editor
//				.editParts(new FindEditPart(elementName, getAgeFeatureProvider(editor))).get(0).part();
		// final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) editor.getEditPart(elementName).part();
		try {
			final Robot robot = new Robot();
			robot.setAutoDelay(50);

			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final Rectangle bounds = gsep.getFigure().getBounds();
				final Point point = PlatformUI.getWorkbench().getDisplay()
						.map(editor.getWidget().getDisplay().getFocusControl(), null, bounds.x, bounds.y);
				robot.keyPress(KeyEvent.VK_ENTER);
				robot.keyRelease(KeyEvent.VK_ENTER);
				robot.mouseMove(point.x - canvas.getHorizontalBar().getSelection() + 5,
						point.y - canvas.getVerticalBar().getSelection() + 5);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			});

			robot.mouseMove(300, 300);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static String qualifiedName(final String projectName, final String classifierName) {
		return projectName + "::" + classifierName;
	}
}
