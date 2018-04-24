package org.osate.ge.tests;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
import org.hamcrest.CustomMatcher;
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
import org.osate.ge.internal.ui.dialogs.ClassifierOperationDialog;

public class AgeGefBot {
	public static class AgeSWTBotGefEditor extends SWTBotGefEditor {
		private final Set<SWTBotGefConnectionEditPart> connectionEditParts = new HashSet<>();

		public AgeSWTBotGefEditor(final IEditorReference reference, final SWTWorkbenchBot bot)
				throws WidgetNotFoundException {
			super(reference, bot);
		}

		public List<SWTBotGefConnectionEditPart> allConnections() {
			connectionEditParts.clear();
			findConnectionEditParts(this.rootEditPart());
			return connectionEditParts.stream().collect(Collectors.toList());
		}

		private List<SWTBotGefConnectionEditPart> childConnections(final SWTBotGefEditPart editPart) {
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

		public List<SWTBotGefConnectionEditPart> editPartConnections(Matcher<? extends EditPart> matcher)
				throws WidgetNotFoundException {
			return allConnections();
		}

		private final Consumer<SWTBotGefEditPart> addConnectionEditPart = swtBotGefEditPart -> {
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
		private final Robot robot;

		public AgeSWTGefBot() {
			robot = Objects.requireNonNull(getRobot(), "Robot cannot be null.");
			robot.setAutoDelay(300);
		}

		private Robot getRobot() {
			try {
				return new Robot();
			} catch (final AWTException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected SWTBotGefEditor createEditor(final IEditorReference reference, final SWTWorkbenchBot bot) {
			return new AgeSWTBotGefEditor(reference, bot);
		}

		@Override
		public AgeSWTBotGefEditor gefEditor(String fileName) throws WidgetNotFoundException {
			return (AgeSWTBotGefEditor) super.gefEditor(fileName);
		}

		public void mouseLeftClick(final int x, final int y) {
			mouseMove(x, y);
			mouseLeftClickPress();
			mouseLeftClickRelease();
		}

		public void mouseLeftClickPress() {
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		}

		public void mouseLeftClickRelease() {
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		}

		public void mouseMove(int x, int y) {
			robot.mouseMove(x, y);
		}

		public void setAutoDelay(int delay) {
			robot.setAutoDelay(delay);
		}
	}

	private final AgeSWTGefBot bot = new AgeSWTGefBot();
	// Context menu options
	final public static String associatedDiagram = "Associated Diagram";
	final public static String allFilters = "All Filters";

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
		clickRadio("Diagram Editor");
		clickButton("Finish");
		clickButton("OK");
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

	public void clickRadio(final String text) {
		bot.radio(text).click();
	}

	public void clickTableOption(final String text) {
		bot.table().getTableItem(text).click();
	}

	public void createImplementation(final SWTBotGefEditor editor, final String toolType, final String typeName,
			final String elementName, final Point point, final String... parentName) {
		editor.setFocus();
		createToolItem(editor, toolType, point, parentName);
		waitUntilShellIsActive("Create Component Implementation");
		bot.shell("Create Component Implementation").setFocus();
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

	public void createToolItemAndRename(final AgeSWTBotGefEditor editor, final Class<?> clazz, final Point p,
			final String newName, final String... editPathPath) {
		final SWTBotGefEditPart editPart = editor
				.editParts(new FindEditPart(getAgeFeatureProvider(editor), editPathPath)).get(0);
		editor.select(editPart);
		editor.click(editPart);
		createToolItem(editor, ToolTypes.getToolItem(clazz), p, editPathPath);
		final SWTBotGefEditPart newEditPart = getNewElement(editor, clazz);
		renameElement(editor, newEditPart, newName);
	}

	public SWTBotGefEditPart getNewElement(final AgeSWTBotGefEditor editor, final Class<?> clazz) {
		final NewElementCondition newElementCondition = new NewElementCondition(editor, new NewElementMatcher(editor),
				clazz);
		waitUntil(newElementCondition, 5000);
		return newElementCondition.getNewElementEditPart();
	}

	public SWTBotGefConnectionEditPart getNewConnection(final AgeSWTBotGefEditor editor, final Class<?> clazz) {
		final NewConnectionCondition newConnectionCondition = new NewConnectionCondition(editor,
				new NewElementMatcher(editor), clazz);
		waitUntil(newConnectionCondition, 5000);
		return newConnectionCondition.getNewConnectionEditPart();
	}

	private class NewElementCondition extends DefaultCondition {
		private final Class<?> clazz;
		private final AgeSWTBotGefEditor editor;
		private final Matcher<EditPart> editPartMatcher;
		private List<SWTBotGefEditPart> editParts;

		private NewElementCondition(final AgeSWTBotGefEditor editor, final Matcher<EditPart> matcher,
				final Class<?> clazz) {
			this.editor = editor;
			this.editPartMatcher = matcher;
			this.clazz = clazz;
		}

		@Override
		public String getFailureMessage() {
			return clazz + " was not created.";
		};

		@Override
		public boolean test() throws Exception {
			editParts = editor.editParts(editPartMatcher);
			return !editParts.isEmpty();
		};

		private SWTBotGefEditPart getNewElementEditPart() {
			return editParts.get(0);
		}
		}

	private class NewConnectionCondition extends DefaultCondition {
		private final Class<?> clazz;
		private final AgeSWTBotGefEditor editor;
		private final Matcher<EditPart> editPartMatcher;
		private List<SWTBotGefConnectionEditPart> editParts;

		private NewConnectionCondition(final AgeSWTBotGefEditor editor, final Matcher<EditPart> matcher,
				final Class<?> clazz) {
			this.editor = editor;
			this.editPartMatcher = matcher;
			this.clazz = clazz;
		}

		@Override
		public String getFailureMessage() {
			return clazz + " was not created.";
		};

		@Override
		public boolean test() throws Exception {
			editParts = editor.editPartConnections(editPartMatcher);
			return !editParts.isEmpty();
		};

		private SWTBotGefConnectionEditPart getNewConnectionEditPart() {
			return editParts.get(0);
		}
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

	public static class NewElementMatcher extends CustomMatcher<EditPart> {
		final private CharSequence charSeq = "new_";
		final private AgeFeatureProvider ageFeatureProvider;

		public NewElementMatcher(final SWTBotGefEditor editor) {
			super("New Element Matcher");
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
	}

	public void openAssociatedDiagramFromContextMenu(final SWTBotGefEditor editor, final String elementName) {
		final List<SWTBotGefEditPart> editPart = editor
				.editParts(new FindEditPart(getAgeFeatureProvider(editor), elementName));
		editor.click(editPart.get(0));
		sleep(2);
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
		openPropertiesView();
		final Widget widget = getWidget(widgetName);
		Assert.assertTrue("widget is not a control", widget instanceof Control);
		selectControl((Control) widget);
	}

	public void selectControl(final Control c) {
		bot.setAutoDelay(25);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(() -> {
			final Point point = display.map(c.getParent(), null, c.getLocation().x, c.getLocation().y);
			bot.mouseLeftClick(point.x, point.y);
		});
		bot.setAutoDelay(300);
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
			final String option, final String... elementPath) {
		editor.setFocus();
		final SWTBotGefEditPart editPart = findEditPart(editor, elementPath);
		editor.click(editPart);
		editor.select(editPart);
		selectTabbedPropertySection(tabTitle);
		clickRadio(option);
	}

	public void setElementOptionComboInPropertiesView(final AgeSWTBotGefEditor editor, final String tabTitle,
			final String comboId, final String selection, final String... elementPath) {
		editor.setFocus();
		final SWTBotGefEditPart editPart = findEditPart(editor, elementPath);
		editor.click(editPart);
		editor.select(editPart);
		selectTabbedPropertySection(tabTitle);
		clickCombo(comboId, selection);
	}

	public void setElementOptionButtonInPropertiesView(final AgeSWTBotGefEditor editor, final String tabTitle,
			final String option, final String[]... elementPath) {
		editor.setFocus();
		clickElements(editor, elementPath);
		selectElements(editor, elementPath);
		selectTabbedPropertySection(tabTitle);
		clickButton(option);
//		editor.setFocus();
//		final SWTBotGefEditPart editPart = findEditPart(editor, elementPath);
//		editor.click(editPart);
//		editor.select(editPart);
//		openPropertiesView();
//		selectTabbedPropertySection(tabTitle);
//		clickButton(option);
	}

	public void openPropertiesView() {
		bot.widgets(new PrintWidgetMatcher());
		// TODO try show()?
		bot.viewByTitle("Properties").setFocus();
		bot.widgets(new PrintWidgetMatcher());
	}

	private class PrintWidgetMatcher extends CustomMatcher<Widget> {
		public PrintWidgetMatcher() {
			super("Print Widget Matcher");
			System.err.println("<Printing Widgets>");
		}

		@Override
		public boolean matches(Object item) {
			System.err.println(item + " item");
			return true;
		}
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
		// editor.click(swtBotGefEditPart);
		editor.select(swtBotGefEditPart);
		swtBotGefEditPart.resize(PositionConstants.SOUTH_WEST, newSize.x, newSize.y);
	}

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
		final java.awt.Point renameLocation = new java.awt.Point();
		final Display display = editor.getWidget().getDisplay();
		display.syncExec(() -> {
			final FigureCanvas canvas = (FigureCanvas) display.getFocusControl();
			final Point point = PlatformUI.getWorkbench().getDisplay().map(canvas, null, midPoint.x, midPoint.y);
			renameLocation.x = point.x - canvas.getHorizontalBar().getSelection();
			renameLocation.y = point.y - canvas.getVerticalBar().getSelection();
		});

		// Click
		bot.setAutoDelay(300);
		bot.mouseLeftClick(renameLocation.x, renameLocation.y);
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

	public void renameElement(final SWTBotGefEditor editor, final SWTBotGefEditPart newEditPart, final String newName) {
		final java.awt.Point renameLocation = new java.awt.Point();
		setRenameLocation(editor, newEditPart, renameLocation);
		bot.setAutoDelay(300);
		bot.mouseLeftClick(renameLocation.x, renameLocation.y);
		bot.mouseLeftClick(renameLocation.x, renameLocation.y);
		sleep(2);
		editor.directEditType(newName);
		waitUntilElementExists(editor, newName);
	}

	private void setRenameLocation(final SWTBotGefEditor editor, final SWTBotGefEditPart newEditPart,
			final java.awt.Point renameLocation) {
		final GraphitiShapeEditPart gsep = (GraphitiShapeEditPart) newEditPart.part();
		final ContainerShape cs = (ContainerShape) gsep.getPictogramElement();
		final GraphicsAlgorithm labelGA = getLabelShape(cs).getGraphicsAlgorithm();
		editor.setFocus();
		editor.select(newEditPart);

		final Display display = editor.getWidget().getDisplay();
		editor.getWidget().getDisplay().syncExec(() -> {
			final FigureCanvas canvas = (FigureCanvas) display.getFocusControl();
			final Rectangle bounds = gsep.getFigure().getBounds();
			final Point point = PlatformUI.getWorkbench().getDisplay().map(display.getFocusControl(), null, bounds.x,
					bounds.y);
			renameLocation.x = point.x - canvas.getHorizontalBar().getSelection() + labelGA.getX()
					+ labelGA.getWidth() / 2;
			renameLocation.y = point.y - canvas.getVerticalBar().getSelection() + labelGA.getY()
					+ labelGA.getHeight() / 2;
		});
	}

	public void renameConnection(final SWTBotGefEditor editor, final SWTBotGefConnectionEditPart conEditPart,
			final ConnectionPoint connectionPoint, final String newName) {
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

	public void createAbstractTypeAndImplementation(final String packageName, final Point location) {
		final AgeSWTBotGefEditor editor = getEditor(packageName);
		editor.setFocus();
		editor.click(packageName);
		resizeEditPart(editor, new Point(600, 600), packageName);
		createToolItem(editor, ToolTypes.getToolItem(AbstractType.class), location, ElementNames.packageName);
		final SWTBotGefEditPart newEditPart = getNewElement(editor, AbstractTypeImpl.class);
		renameElement(editor, newEditPart, ElementNames.abstractTypeName);
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

	public void clickElements(final SWTBotGefEditor editor, final String[]... elementPaths) {
		for (final String[] elementPath : elementPaths) {
			editor.click(findEditPart(editor, elementPath));
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
	public void dragShellAwayFromEditor(final AgeSWTBotGefEditor editor, final String shellTitle) {
		final Display display = editor.getWidget().getDisplay();
		display.syncExec(() -> {
			final int y = display.getActiveShell().getBounds().height;
			final Shell shell = bot.shell(shellTitle).widget;
			shell.setFocus();
			final org.eclipse.swt.graphics.Rectangle outer = shell.getBounds();
			final org.eclipse.swt.graphics.Rectangle inner = shell.getClientArea();
			bot.setAutoDelay(50);
			bot.mouseMove(outer.x + outer.width / 2, outer.y + (outer.height - inner.height) / 2);
			bot.mouseLeftClickPress();
			bot.mouseMove(outer.width / 2, y / 2 - outer.height / 2);
			bot.mouseLeftClickRelease();
			bot.setAutoDelay(300);
		});
	}

	public void setFocusShell(final String title) {
		bot.shell(title).setFocus();
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

	public void setConnectionOptionComboInPropertiesView(final AgeSWTBotGefEditor editor,
			final SWTBotGefConnectionEditPart connectionEditPart, final String tab, final String comboId,
			final String selection) {
		editor.select(connectionEditPart);
		clickConnection(editor, ((GraphitiConnectionEditPart) connectionEditPart.part()).getConnectionFigure());
		selectTabbedPropertySection("Appearance");
		clickCombo(comboId, selection);
	}
}