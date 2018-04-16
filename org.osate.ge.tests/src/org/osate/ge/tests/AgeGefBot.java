package org.osate.ge.tests;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.platform.GraphitiConnectionEditPart;
import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefConnectionEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.osate.aadl2.AbstractImplementation;
import org.osate.aadl2.AbstractType;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.impl.AbstractTypeImpl;
import org.osate.aadl2.impl.NamedElementImpl;
import org.osate.ge.internal.graphiti.AgeFeatureProvider;
import org.osate.ge.internal.graphiti.ShapeNames;
import org.osate.ge.internal.graphiti.diagram.PropertyUtil;
import org.osate.ge.internal.graphiti.features.BoHandlerDirectEditFeature;
import org.osate.ge.internal.ui.dialogs.ClassifierOperationDialog;

public class AgeGefBot {
	public static class AgeSWTBotGefEditor extends SWTBotGefEditor {
		final Set<SWTBotGefConnectionEditPart> connectionEditParts = new HashSet<>();

		public AgeSWTBotGefEditor(final IEditorReference reference, final SWTWorkbenchBot bot)
				throws WidgetNotFoundException {
			super(reference, bot);
		}

		public List<SWTBotGefConnectionEditPart> allConnections() {
			connectionEditParts.clear();
			findConnectionEditParts(this.rootEditPart());
			return connectionEditParts.stream().collect(Collectors.toList());
		}

		public List<SWTBotGefConnectionEditPart> childConnections(final SWTBotGefEditPart editPart) {
			connectionEditParts.clear();
			findConnectionEditParts(editPart);
			return connectionEditParts.stream().collect(Collectors.toList());
		}

		private void findConnectionEditParts(final SWTBotGefEditPart swtBotGefEditPart) {
			for (final SWTBotGefEditPart editPart : swtBotGefEditPart.children()) {
				findConnectionEditParts(editPart);
			}

			addConnectionEditPart.accept(swtBotGefEditPart);
		}

		final Consumer<SWTBotGefEditPart> addConnectionEditPart = swtBotGefEditPart -> {
			if (swtBotGefEditPart.part() instanceof AbstractGraphicalEditPart) {
				final AbstractGraphicalEditPart agep = (AbstractGraphicalEditPart) swtBotGefEditPart.part();
				for (final Object ob : agep.getTargetConnections()) {
					if (ob instanceof GraphitiConnectionEditPart) {
						connectionEditParts.add(createEditPart((GraphitiConnectionEditPart) ob));
					}
				}

				for (final Object ob : agep.getSourceConnections()) {
					if (ob instanceof GraphitiConnectionEditPart) {
						connectionEditParts.add(createEditPart((GraphitiConnectionEditPart) ob));
					}
				}
			}
		};
	}

	private static class AgeSWTGefBot extends SWTGefBot {
		@Override
		protected SWTBotGefEditor createEditor(final IEditorReference reference, final SWTWorkbenchBot bot) {
			return new AgeSWTBotGefEditor(reference, bot);
		}

		@Override
		public AgeSWTBotGefEditor gefEditor(String fileName) throws WidgetNotFoundException {
			return (AgeSWTBotGefEditor) super.gefEditor(fileName);
		}
	}

	final private AgeSWTGefBot bot = new AgeSWTGefBot();

	// Context menu options
	final public static String associatedDiagram = "Associated Diagram";
	final public static String allFilters = "All Filters";

//    public void createNewProjectAndPackage(final String projectName, final String packageName) {
//        closeWelcomePage();
//        final SWTBotMenu newMenu = bot.menu("Other...", true).click();
//        bot.tree().getTreeItem("AADL").expand().getNode("AADL Project").click();
//        bot.button("Next >").click();
//        bot.text().setText(projectName);
//        bot.button("Finish").click();
//
//        if (!bot.activePerspective().getLabel().equals("AADL")) {
//            // Open AADL Perspective Dialog
//            bot.button("Open Perspective").click();
//        }
//
//        // Create AADL Package
//        newMenu.click();
//        bot.tree().getTreeItem("AADL").expand().getNode("AADL Package").click();
//        bot.button("Next >").click();
//        bot.text().setText(packageName);
//        bot.radio("Graphical Editor").click();
//        bot.button("Finish").click();
//        bot.button("OK").click();
//
//        // Close editor for open test
//        bot.gefEditor(packageName + ".aadl_diagram").close();
//
//        bot.tree().expandNode(new String[] { projectName }).getNode(packageName + ".aadl")
//                .click();
//        bot.tree().contextMenu("Open").click();
//    }

	public void createNewProjectAndPackage(final String projectName, final String packageName) {
		SWTBotPreferences.TIMEOUT = 15000;
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

		bot.tree().expandNode(new String[] { projectName }).getNode(packageName + ".aadl").click();
		bot.tree().contextMenu("Open").click();
	}

	public void createAADLPackage(final String projectName, final String packageName) {
		bot.tree().select(projectName).contextMenu("AADL Package").click();
		bot.text().setText(packageName);
		bot.radio("Diagram Editor").click();
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

	public void createImplementation(final SWTBotGefEditor editor, final String toolType, final String typeName,
			final String elementName, final Point point, final String... parentName) {
		editor.setFocus();
		createToolItem(editor, toolType, point, parentName);
		waitUntilShellIsActive("Create Component Implementation");
		bot.shell("Create Component Implementation").setFocus();
		setText(elementName);
		// printWidgets();
		clickRadio("Existing");
		clickButton("...");
		clickButton("OK");
		clickButton("OK");

		waitUntilElementExists(editor, typeName + "." + elementName);
	}

	private void printWidgets() {
		System.err.println("ge.tests.widgets");
		bot.widget(widgetPrinter);
	}

	private final Matcher<Widget> widgetPrinter = new BaseMatcher<Widget>() {
		@Override
		public boolean matches(Object item) {
			System.err.println(item + " item");
			return true;
		}

		@Override
		public void describeTo(Description description) {
			// TODO Auto-generated method stub

		}
	};

	public void waitUntil(final ICondition condition, final long timeout) {
		bot.waitUntil(condition, timeout);
	}

	public void deleteProject(final String projectName) {
		bot.tree().select(projectName).contextMenu("Delete").click();
		bot.checkBox("Delete project contents on disk (cannot be undone)").click();
		final SWTBotShell shell = bot.activeShell();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
		// bot.saveAllEditors();
		// bot.closeAllEditors();
		// bot.perspectiveByLabel("Resource").activate();
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

	public void createToolItem(final SWTBotGefEditor editor, final String toolItem, final Point p,
			final String... editPartPath) {
		final SWTBotGefEditPart parent = findEditPart(editor, editPartPath);
		editor.setFocus();
		editor.click(parent);
		editor.activateTool(toolItem);
		final Rectangle rect = ((GraphitiShapeEditPart) parent.part()).getFigure().getBounds();
		editor.click(rect.x + p.x, rect.y + p.y);
		editor.activateDefaultTool();
	}

	public void createToolItemAndRename(final SWTBotGefEditor editor, final Class<?> clazz, final Point p,
			final String newName, final String... editPathPath) {
		final SWTBotGefEditPart editPart = editor
				.editParts(new FindEditPart(getAgeFeatureProvider(editor), editPathPath)).get(0);
		editor.select(editPart);
		editor.click(editPart);
		createToolItem(editor, ToolTypes.getToolItem(clazz), p, editPathPath);
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

	public List<SWTBotGefConnectionEditPart> getNewConnectionEditPart(final AgeSWTBotGefEditor editor,
			final Class<?> clazz) {
		final AgeFeatureProvider ageFeatureProvider = getAgeFeatureProvider(editor);
		return editor.allConnections().stream().filter(editPart -> {
			final GraphitiConnectionEditPart gcep = (GraphitiConnectionEditPart) editPart.part();
			final Object bo = ageFeatureProvider.getBusinessObjectForPictogramElement(gcep.getPictogramElement());
			if (bo.getClass() == clazz) {
				return ((NamedElement) bo).getName().contains("new_");
			}

			return false;
		}).collect(Collectors.toList());
	}

	public List<SWTBotGefConnectionEditPart> getConnectionEditParts(final AgeSWTBotGefEditor editor,
			final String connectionName, final String... editPartPath) {
		final AgeFeatureProvider ageFeatureProvider = getAgeFeatureProvider(editor);
		final SWTBotGefEditPart parent = findEditPart(editor, editPartPath);

		return editor.childConnections(parent).stream().filter(editPart -> {
			final GraphitiConnectionEditPart gcep = (GraphitiConnectionEditPart) editPart.part();
			final Object bo = ageFeatureProvider.getBusinessObjectForPictogramElement(gcep.getPictogramElement());
			if (bo instanceof NamedElement) {
				return ((NamedElement) bo).getName().equalsIgnoreCase(connectionName);
			}

			return false;
		}).collect(Collectors.toList());
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
				return !editor.editParts(new FindEditPart(ageFeatureProvider, elementName)).isEmpty();
			};
		}, 5000);
	}

	public SWTBotTree getTree() {
		return bot.tree();
	}

	public AgeSWTBotGefEditor getEditor(final String editor) {
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

	// TODO cmd needed? click buttons probably different for other diagram opening types.
	// hard code "Associated Diagram" and change method name
	public void openAssociatedDiagramFromContextMenu(final SWTBotGefEditor editor, final String elementName) {
		final List<SWTBotGefEditPart> editPart = editor
				.editParts(new FindEditPart(getAgeFeatureProvider(editor), elementName));
		editor.click(editPart.get(0));
		sleep(4);
		editor.clickContextMenu("Associated Diagram");
		clickButton("Yes");
		clickButton("OK");
	}

	public void openTypeDiagramFromContextMenu(final SWTBotGefEditor editor, final String elementName) {
		final List<SWTBotGefEditPart> editPart = editor
				.editParts(new FindEditPart(getAgeFeatureProvider(editor), elementName));
		editor.select(editPart.get(0)).clickContextMenu("Type Diagram");
		clickButton("Yes");
		clickButton("OK");
	}

	public void executeContextMenuCommand(final SWTBotGefEditor editor, final String elementName,
			final String contextMenuCmd) {
		// Set focus for properties filters
		final AgeFeatureProvider ageFeatureProvider = (AgeFeatureProvider) ((GraphitiShapeEditPart) editor
				.mainEditPart().part()).getFeatureProvider();
		final List<SWTBotGefEditPart> list = editor.editParts(new FindEditPart(ageFeatureProvider, elementName));
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
		bot.getDisplay().syncExec(() -> bot.getDisplay().getActiveShell().setMaximized(true));
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
			final Display display = PlatformUI.getWorkbench().getDisplay();
			display.syncExec(() -> {
				final Control c = widget;
				final Point point = display.map(c.getParent(), null, c.getLocation().x, c.getLocation().y);
				robot.mouseMove(point.x, point.y);
				robot.delay(2000);
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
			final Widget widget = (Widget) item;
			if (!widget.isDisposed()) {
				return widget.toString().equalsIgnoreCase(controlName);
			}

			return false;
		};

		@Override
		public void describeTo(org.hamcrest.Description description) {
		};
	}

	public void setElementOptionRadioInPropertiesView(final SWTBotGefEditor editor, final String tabTitle,
			final String option, final String... elementName) {
		selectElement(editor, elementName);
		selectTabbedPropertySection(tabTitle);
		clickElementsMouse(editor, elementName);
		bot.viewByTitle("Properties").setFocus();
		bot.activeView().bot().radio(option).click();
		editor.setFocus();
	}

	public void setElementOptionButtonInPropertiesView(final SWTBotGefEditor editor, final String tabTitle,
			final String option, final String... elementName) {
		printWidgets();
		openPropertiesView(editor, elementName);
		printWidgets();
		selectElement(editor, elementName);
		printWidgets();
		selectTabbedPropertySection(tabTitle);
		printWidgets();
		// clickElementsMouse(editor, elementName);
		editor.setFocus();
		printWidgets();
		clickButton(option);
	}

	public void openPropertiesView(final SWTBotGefEditor editor, final String... elementName) {
		openProperties();
		editor.setFocus();
		// doubleClickElement(editor, elementName[0]);
		//final SWTBotGefEditPart part = findEditPart(editor, elementName);
		//editor.click(part);
		//editor.doubleClick(findEditPart(editor, elementName));
//        bot.sleep(3000);
//        bot.waitUntil(new DefaultCondition() {
//            @Override
//            public boolean test() throws Exception {
//                return getActiveView().getTitle().equalsIgnoreCase("Properties");
//            };
//
//            @Override
//            public String getFailureMessage() {
//                return "view not opened";
//            };
//        }, 5000);
	}

	public void openProperties() {
		bot.viewByTitle("Properties").setFocus();
	}

	public static AgeFeatureProvider getAgeFeatureProvider(final SWTBotGefEditor editor) {
		return (AgeFeatureProvider) ((GraphitiShapeEditPart) editor.mainEditPart().part()).getFeatureProvider();
	}

	public static class FindEditPart extends BaseMatcher<EditPart> {
		private final List<String> editPartName;
		final AgeFeatureProvider ageFeatureProvider;

		public FindEditPart(final AgeFeatureProvider ageFeatureProvider, final String... editPartName) {
			this.editPartName = Arrays.asList(editPartName);
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
					return editPartName.contains(namedElement.getName());
				}
			} else if (item instanceof AbstractGraphicalEditPart) {
				final AbstractGraphicalEditPart agep = (AbstractGraphicalEditPart) item;
				for (final Object ob : agep.getTargetConnections()) {
					if (ob instanceof GraphitiConnectionEditPart) {
						final GraphitiConnectionEditPart gcep = (GraphitiConnectionEditPart) ob;
						final Object element = ageFeatureProvider
								.getBusinessObjectForPictogramElement(gcep.getPictogramElement());
						if (element instanceof NamedElementImpl) {
							final NamedElementImpl namedElement = (NamedElementImpl) element;
							return editPartName.contains(namedElement.getName());
						}
					}
				}

				for (final Object ob : agep.getSourceConnections()) {
					if (ob instanceof GraphitiConnectionEditPart) {
						final GraphitiConnectionEditPart gcep = (GraphitiConnectionEditPart) ob;
						final Object element = ageFeatureProvider
								.getBusinessObjectForPictogramElement(gcep.getPictogramElement());
						if (element instanceof NamedElementImpl) {
							final NamedElementImpl namedElement = (NamedElementImpl) element;
							return editPartName.contains(namedElement.getName());
						}
					}
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

	public void resizeEditPart(final SWTBotGefEditor editor, final Point newSize, final String... editPartPath) {
		editor.setFocus();
		final SWTBotGefEditPart swtBotGefEditPart = findEditPart(editor, editPartPath);
		editor.click(swtBotGefEditPart);
		editor.select(swtBotGefEditPart);
		swtBotGefEditPart.resize(PositionConstants.SOUTH_WEST, newSize.x, newSize.y);
	}

//	public void resize(final SWTBotGefEditor editor, final Point newSize, final String... editPartPath) {
//		final SWTBotGefEditPart swtBotGefEditPart = findEditPart(editor, editPartPath);
//		final Rectangle bounds = ((GraphitiShapeEditPart) swtBotGefEditPart.part()).getFigure().getBounds();
//		editor.select(swtBotGefEditPart);
//		swtBotGefEditPart.resize(PositionConstants.SOUTH_WEST, 600, 600);
//
//		// clickElements(editor, editPartPath);
//
//		// try {
//		// final Robot robot = new Robot();
//		final Display display = editor.getWidget().getDisplay();
//		display.syncExec(() -> {
//			// swtBotGefEditPart.resize(PositionConstants.SOUTH_WEST, 600, 600);
//
//			// final Point point = display
//			// .map(display.getFocusControl(), null, bounds.x, bounds.y);
//			// robot.setAutoDelay(500);
//			// robot.mouseMove(point.x, point.y);
//			// robot.mousePress(InputEvent.BUTTON1_MASK);
//			// robot.mouseRelease(InputEvent.BUTTON1_MASK);
//
//
//			// robot.mouseMove(point.x + 10, point.y + 10);
//			// robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
//			// robot.mouseMove(point.x + 60, point.y + 60);
//			// robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
//
//			// swtBotGefEditPart.resize(PositionConstants.SOUTH_WEST, 600, 600);
////
////                final Point bottomRightCorner = new Point(point.x + bounds.width, point.y + bounds.height);
////                robot.mouseMove(bottomRightCorner.x, bottomRightCorner.y);
////
////                // robot.mousePress(InputEvent.BUTTON1_MASK);
////
////                final Point newLocation = new Point(newSize.x - bounds.width, newSize.y - bounds.height);
////                robot.mouseMove(bottomRightCorner.x + newLocation.x, bottomRightCorner.y + newLocation.y);
//////                robot.mouseRelease(InputEvent.BUTTON1_MASK);
//////                robot.mouseMove(300, 300);
//		});
//		// } catch (final AWTException e) {
//
//		// }
//
//		bot.sleep(10000);
//		System.err.println("AAA");
//	}

	/*
	 * int fromX = bounds.x;
	 * int fromY = bounds.y + bounds.height;
	 * int toX = bounds.x + bounds.width - newSize.x;
	 * int toY = bounds.y + newSize.y;
	 * // editor.getSWTBotGefViewer().drag(fromX, fromY, toX, toY);
	 */

	public SWTBotGefEditPart getEditPart(final SWTBotGefEditor editor, final String elementName) {
		return editor.editParts(new FindEditPart(getAgeFeatureProvider(editor), elementName)).get(0);
	}

	public List<SWTBotGefEditPart> findChild(final SWTBotGefEditor editor, final SWTBotGefEditPart parent,
			final String childName) {
		return parent.descendants(new FindEditPart(getAgeFeatureProvider(editor), childName));
	}

	public static Object getBusinessObject(final SWTBotGefEditor editor, final PictogramElement pe) {
		return getAgeFeatureProvider(editor).getBusinessObjectForPictogramElement(pe);
	}

	private void doubleClickElement(final SWTBotGefEditor editor, final String... elementName) {
		editor.setFocus();
		final SWTBotGefEditPart element = findEditPart(editor, elementName);
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) element.part();
		try {
			final Robot robot = new Robot();
			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final Rectangle bounds = gsep.getFigure().getBounds();
				final Point point = PlatformUI.getWorkbench().getDisplay().map(canvas, null, bounds.x, bounds.y);

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

	public SWTBotGefEditPart findEditPart(final SWTBotGefEditor editor, final String... editPartPath) {
		final AgeFeatureProvider ageFeatureProvider = getAgeFeatureProvider(editor);
		final Iterator<String> it = Arrays.asList(editPartPath).iterator();
		SWTBotGefEditPart editPartFound = editor.editParts(new FindEditPart(ageFeatureProvider, it.next())).get(0);
		while (it.hasNext()) {
			editPartFound = editPartFound.descendants(new FindEditPart(ageFeatureProvider, it.next())).get(0);
		}

		return editPartFound;
	}

	public void clickConnection(final SWTBotGefEditor editor, final Connection connection) {
		editor.setFocus();
		final org.eclipse.draw2d.geometry.Point midPoint = connection.getPoints().getMidpoint();
		try {
			final Robot robot = new Robot();
			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final Point point = PlatformUI.getWorkbench().getDisplay().map(canvas, null, midPoint.x, midPoint.y);

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

	public void renameElement(final SWTBotGefEditor editor, final String newName) {
		final SWTBotGefEditPart swtGefEditPart = editor.editParts(new AgeGefBot.NewElementMatcher(editor)).get(0);
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) swtGefEditPart.part();
		final ContainerShape cs = (ContainerShape) gsep.getPictogramElement();
		final Shape labelShape = getLabelShape(cs);
		editor.setFocus();
		editor.click(swtGefEditPart);
		editor.select(swtGefEditPart);
		sleep(3);

		final GraphicsAlgorithm labelGA = labelShape.getGraphicsAlgorithm();
		try {
			final Robot robot = new Robot();
			robot.setAutoDelay(100);
			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final Rectangle bounds = gsep.getFigure().getBounds();
				final Point point = PlatformUI.getWorkbench().getDisplay()
						.map(editor.getWidget().getDisplay().getFocusControl(), null, bounds.x, bounds.y);
				robot.mouseMove(0, 0);
				robot.mouseMove(
						point.x - canvas.getHorizontalBar().getSelection() + labelGA.getX() + labelGA.getWidth() / 2,
						point.y - canvas.getVerticalBar().getSelection() + labelGA.getY() + labelGA.getHeight() / 2);
				// robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				// robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			});
		} catch (AWTException e) {
		}

		sleep(3);
		swtGefEditPart.activateDirectEdit(BoHandlerDirectEditFeature.class);
		editor.directEditType(newName);
		waitUntilElementExists(editor, newName);
	}

//    public void renameElement(final SWTBotGefEditor editor,
//            final String newName) {
//        final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) editor
//                .editParts(new AgeGefBot.NewElementMatcher(editor)).get(0).part();
//        final SWTBotGefEditPart swtGefEditPart = editor.editParts(new AgeGefBot.NewElementMatcher(editor)).get(0);
//        editor.select(swtGefEditPart);
//        swtGefEditPart.activateDirectEdit(BoHandlerDirectEditFeature.class);
//        editor.directEditType("AAA");
//
//        final ContainerShape cs = (ContainerShape) gsep.getPictogramElement();
//        final Shape labelShape = getLabelShape(cs);
//
//        final GraphicsAlgorithm labelGA = labelShape.getGraphicsAlgorithm();
//
//        RenameHelper.renameElement(editor, newName,
//                new Point(labelGA.getX() + labelGA.getWidth() / 2, labelGA.getY() + labelGA.getHeight() / 2));
//        waitUntilElementExists(editor, newName);
//    }

	public void selectNewElement(final SWTBotGefEditor editor) {
		final SWTBotGefEditPart gsep = editor.editParts(new AgeGefBot.NewElementMatcher(editor)).get(0);
		editor.select(gsep);
	}

	public void renameConnection(final SWTBotGefEditor editor, final SWTBotGefConnectionEditPart conEditPart,
			final ConnectionPoint connectionPoint,
			final String newName) {
		editor.setFocus();
		editor.select(conEditPart);
		final GraphitiConnectionEditPart gcep = (GraphitiConnectionEditPart) conEditPart.part();

		final ConnectionDecorator cd = getLabelShape((FreeFormConnection) gcep.getPictogramElement());
		final GraphicsAlgorithm labelGA = cd.getGraphicsAlgorithm();
		// Select connection label for rename
		try {
			final Robot robot = new Robot();
			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final org.eclipse.draw2d.geometry.Point refPoint = connectionPoint.getValue(gcep);
				final Point point = PlatformUI.getWorkbench().getDisplay()
						.map(editor.getWidget().getDisplay().getFocusControl(), null, refPoint.x, refPoint.y);
				robot.mouseMove(
						point.x - canvas.getHorizontalBar().getSelection() + labelGA.getX() + labelGA.getWidth() / 2,
						point.y - canvas.getVerticalBar().getSelection() + labelGA.getY() + labelGA.getHeight() / 2);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			});
		} catch (AWTException e) {
		}

		sleep(1);
		final SWTBotGefEditPart cdEditPart = editor.selectedEditParts().get(0);
		cdEditPart.activateDirectEdit();
		editor.directEditType(newName);
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
		editor.click(packageName);
		resizeEditPart(editor, new Point(600, 600), packageName);
		clickElementsMouse(editor, new String[] { packageName });
		createToolItem(editor, ToolTypes.getToolItem(AbstractType.class), new Point(25, 25), ElementNames.packageName);
		waitUntilNewElementIsCreated(editor, AbstractTypeImpl.class);
		renameElement(editor, ElementNames.abstractTypeName);
		waitUntilElementExists(editor, ElementNames.abstractTypeName);
		createImplementation(editor, ToolTypes.getToolItem(AbstractImplementation.class), ElementNames.abstractTypeName,
				"impl", new Point(100, 200), packageName);
	}

	public void createTypeAndImplementation(final SWTBotGefEditor editor, final Point point, final String implName,
			final String typeName, final String impl, final String packageName) {
		createToolItem(editor, impl, point, packageName);
		waitUntilShellIsActive("Create Component Implementation");
		setTextWithId(ClassifierOperationDialog.primaryPartIdentifier, implName);
		clickRadio("New Component Type");
		setTextWithId(ClassifierOperationDialog.baseValueIdentifier, typeName);
		clickButton("OK");
		waitUntilElementExists(editor, typeName + "." + implName);
	}

	public void clickElement(final SWTBotGefEditor editor, final String... elementPath) {
		editor.setFocus();
		editor.click(findEditPart(editor, elementPath));
	}

	public void resetMouse() {
		try {
			final Robot robot = new Robot();
			robot.mouseMove(0, 0);
		} catch (AWTException e) {
		}
	}

	public void selectElement(final SWTBotGefEditor editor, final String... elementPath) {
		editor.setFocus();
		editor.select(findEditPart(editor, elementPath));
		// Pause for editor update on selection
		sleep(3);
	}

	public void selectElements(final SWTBotGefEditor editor, final String[]... elementPaths) {
		editor.setFocus();
		final List<SWTBotGefEditPart> editParts = new ArrayList<>();
		for (final String[] elementPath : elementPaths) {
			editParts.add(findEditPart(editor, elementPath));
		}

		editor.select(editParts);
	}

	public void clickElementsMouse(final SWTBotGefEditor editor, final String[]... elementPaths) {
		editor.setFocus();
		final List<GraphitiShapeEditPart> gseps = new ArrayList<>();
		for (final String[] elementPath : elementPaths) {
			gseps.add((GraphitiShapeEditPart) findEditPart(editor, elementPath).part());
		}

		try {
			final Robot robot = new Robot();
			robot.setAutoDelay(50);

			editor.getWidget().getDisplay().asyncExec(() -> {
				final FigureCanvas canvas = (FigureCanvas) editor.getWidget().getDisplay().getFocusControl();
				final Iterator<GraphitiShapeEditPart> it = gseps.iterator();
				Rectangle bounds = it.next().getFigure().getBounds();
				Point point = PlatformUI.getWorkbench().getDisplay()
						.map(editor.getWidget().getDisplay().getFocusControl(), null, bounds.x, bounds.y);
				// TODO enter needed?
				// robot.keyPress(KeyEvent.VK_ENTER);
				// robot.keyRelease(KeyEvent.VK_ENTER);
				robot.mouseMove(point.x - canvas.getHorizontalBar().getSelection() + 5,
						point.y - canvas.getVerticalBar().getSelection() + 5);
				// robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				// robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				while (it.hasNext()) {
					bounds = it.next().getFigure().getBounds();
					point = PlatformUI.getWorkbench().getDisplay()
							.map(editor.getWidget().getDisplay().getFocusControl(), null, bounds.x, bounds.y);
					robot.keyPress(KeyEvent.VK_CONTROL);
					robot.mouseMove(point.x - canvas.getHorizontalBar().getSelection() + 5,
							point.y - canvas.getVerticalBar().getSelection() + 5);
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					robot.keyRelease(KeyEvent.VK_CONTROL);
				}
			});

			robot.mouseMove(300, 300);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static String qualifiedName(final String projectName, final String classifierName) {
		return projectName + "::" + classifierName;
	}

	public SWTBotToolbarButton getToolbarButtonWithTooltip(final String tooltip) {
		return bot.toolbarButtonWithTooltip(tooltip);
	}

	public void clickToolbarButtonWithTooltip(final String tooltip) {
		bot.toolbarButtonWithTooltip(tooltip).click();
	}

	// Drag dialog down and to the left
	public void dragShellAwayFromEditor(final AgeSWTBotGefEditor implEditor, final String shellTitle) {
		try {
			final Robot robot = new Robot();
			robot.setAutoDelay(50);
			final Display display = Display.getDefault();
			display.syncExec(() -> {
				final int y = implEditor.getWidget().getDisplay().getActiveShell().getBounds().height;
				final Shell shell = bot.shell(shellTitle).widget;
				shell.setFocus();
				final org.eclipse.swt.graphics.Rectangle outer = shell.getBounds();
				final org.eclipse.swt.graphics.Rectangle inner = shell.getClientArea();
				robot.mouseMove(outer.x + outer.width / 2, outer.y + (outer.height - inner.height) / 2);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseMove(outer.width / 2, y / 2 - outer.height / 2);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			});

		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public void setFocusShell(final String string) {
		bot.shell(string).setFocus();
	}

	public enum ConnectionPoint {
		FIRST {
			@Override
			public org.eclipse.draw2d.geometry.Point getValue(final GraphitiConnectionEditPart gcep) {
				return gcep.getConnectionFigure().getPoints().getFirstPoint();
			}
		},
		MIDDLE {
			@Override
			public org.eclipse.draw2d.geometry.Point getValue(final GraphitiConnectionEditPart gcep) {
				return gcep.getConnectionFigure().getPoints().getMidpoint();
			}
		},
		LAST {
			@Override
			public org.eclipse.draw2d.geometry.Point getValue(final GraphitiConnectionEditPart gcep) {
				return gcep.getConnectionFigure().getPoints().getLastPoint();
			}
		};

		public abstract org.eclipse.draw2d.geometry.Point getValue(final GraphitiConnectionEditPart gcep);
	}
}