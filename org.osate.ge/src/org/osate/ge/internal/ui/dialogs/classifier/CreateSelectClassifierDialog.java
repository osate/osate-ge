package org.osate.ge.internal.ui.dialogs.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog used for creating a new classifier.
 * If a new implementation is being created, the dialog can also prompt to create a new type.
 *
 */
public class CreateSelectClassifierDialog {
	static String NOT_SELECTED_LABEL = "<Not Selected>";

	public static interface Model {
		String getTitle();

		String getMessage();

		Collection<?> getPackageOptions();

		String getPrimarySelectTitle();

		String getPrimarySelectMessage();

		Collection<?> getPrimarySelectOptions();

		Collection<?> getUnfilteredPrimarySelectOptions();

		Collection<?> getBaseSelectOptions(final ClassifierOperation primaryOperation);

		Collection<?> getUnfilteredBaseSelectOptions(final ClassifierOperation primaryOperation);
	}

	public static class Result {
		private final ConfiguredClassifierOperation primaryOp;
		private final ConfiguredClassifierOperation baseOp;

		private Result(final ConfiguredClassifierOperation primaryOp, final ConfiguredClassifierOperation baseOp) {
			this.primaryOp = Objects.requireNonNull(primaryOp, "primaryOp must not be null");
			this.baseOp = Objects.requireNonNull(baseOp, "baseOp must not be null");
		}

		public final ConfiguredClassifierOperation getPrimaryOperation() {
			return primaryOp;
		}

		public final ConfiguredClassifierOperation getBaseOperation() {
			return baseOp;
		}
	}

	private static class Arguments {
		private Arguments(final Model model, final EnumSet<ClassifierOperation> allowedOperations, final Object defaultPackage,
				final Object defaultSelection, final boolean showPrimaryPackageSelector) {
			this.model = Objects.requireNonNull(model, "model must not be null");
			this.allowedOperations = Objects.requireNonNull(allowedOperations, "allowedOperations must not be null");
			this.defaultPackage = defaultPackage;
			this.defaultSelection = defaultSelection;
			this.showPrimaryPackageSelector = showPrimaryPackageSelector;
		}

		public final Model model;
		public final EnumSet<ClassifierOperation> allowedOperations;
		public final Object defaultPackage;
		public final Object defaultSelection;
		public final boolean showPrimaryPackageSelector;
	}

	public static class ArgumentBuilder {
		private Model model;
		private EnumSet<ClassifierOperation> allowedOperations;
		private Object defaultPackage;
		private Object defaultSelection;
		private boolean showPrimaryPackageSelector = true;

		public ArgumentBuilder(final Model model, final EnumSet<ClassifierOperation> allowedOperations) {
			this.model = Objects.requireNonNull(model, "model must not be null");
			this.allowedOperations = Objects.requireNonNull(allowedOperations, "allowedOperations must not be null");
		}

		public ArgumentBuilder defaultPackage(final Object value) {
			this.defaultPackage = value;
			return this;
		}

		public ArgumentBuilder defaultSelection(final Object value) {
			this.defaultSelection = value;
			return this;
		}

		public ArgumentBuilder showPrimaryPackageSelector(final boolean value) {
			this.showPrimaryPackageSelector = value;
			return this;
		}

		public Arguments create() {
			return new Arguments(model, allowedOperations, defaultPackage, defaultSelection,
					showPrimaryPackageSelector);
		}
	}

	private static class InnerDialog extends TitleAreaDialog {
		private final Arguments args;
		private ConfiguredClassifierOperationEditor primaryWidget;
		private ConfiguredClassifierOperationEditor baseValueWidget;
		private Group baseGroup;

		protected InnerDialog(final Shell parentShell, final Arguments args) {
			super(parentShell);
			this.args = Objects.requireNonNull(args, "args must not be null");
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(args.model.getTitle());
			newShell.setMinimumSize(250, 50);
		}

		@Override
		protected Point getInitialSize() {
			final Point initialSize = super.getInitialSize();
			return new Point(initialSize.x, Math.max(initialSize.y, 500));
		}

		@Override
		public void create() {
			super.create();
			setTitle(args.model.getTitle()); // TODO
			setMessage(args.model.getMessage(), IMessageProvider.INFORMATION); // TODO
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			final Composite area = (Composite) super.createDialogArea(parent);

			// Scrollable
			final ScrolledComposite scrolled = new ScrolledComposite(area, SWT.H_SCROLL | SWT.V_SCROLL);
			scrolled.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			scrolled.setExpandVertical(true);
			scrolled.setExpandHorizontal(true);

			final Composite container = new Composite(scrolled, SWT.NONE);
			container.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

			// TODO: Rename
			// TODO: Only call getPackageOptionsOnce()
			// TODO: Selection options for primary widget
			primaryWidget = new ConfiguredClassifierOperationEditor(container, args.allowedOperations, args.showPrimaryPackageSelector,
					new ConfiguredClassifierOperationEditor.InnerWidgetModel() {
				@Override
				public Collection<?> getPackageOptions() {
					return args.model.getPackageOptions();
				}

				@Override
				public String getSelectTitle() {
					return args.model.getPrimarySelectTitle();
				}

				@Override
				public String getSelectMessage() {
					return args.model.getPrimarySelectMessage();
				}

				@Override
				public Collection<?> getSelectOptions() {
					return args.model.getPrimarySelectOptions();
				}

				@Override
				public Collection<?> getUnfilteredSelectOptions() {
					// TODO
					return args.model.getUnfilteredPrimarySelectOptions();
				}
			});
			primaryWidget.setSelectedElement(args.defaultSelection);
			primaryWidget.setSelectedPackage(args.defaultPackage);
			primaryWidget.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

			baseGroup = new Group(container, SWT.NONE);
			baseGroup.setText("Base");
			baseGroup.setLayout(GridLayoutFactory.swtDefaults().create());
			baseGroup.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false).create());

			baseValueWidget = new ConfiguredClassifierOperationEditor(baseGroup, EnumSet.allOf(ClassifierOperation.class), true,
					new ConfiguredClassifierOperationEditor.InnerWidgetModel() {
				@Override
				public Collection<?> getPackageOptions() {
					return args.model.getPackageOptions();
				}

				@Override
				public String getSelectTitle() {
					return "Select Base Classifier";
				}

				@Override
				public String getSelectMessage() {
					return "Select a base classifier.";
				}

				@Override
				public Collection<?> getSelectOptions() {
							return args.model
									.getBaseSelectOptions(primaryWidget.getConfiguredOperation().getOperation());
				}

				@Override
				public Collection<?> getUnfilteredSelectOptions() {
							return args.model.getUnfilteredBaseSelectOptions(
									primaryWidget.getConfiguredOperation().getOperation());
				}
			});
			baseValueWidget.setSelectedElement(args.defaultSelection);
			baseValueWidget.setSelectedPackage(args.defaultPackage);
			baseValueWidget.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

			// Update the base whenever the primary widget is updated
			primaryWidget.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					updateBase();
				}
			});
			updateBase();

			// TODO: Set initial base group widget visibility.

			// The set scrolled composite' content
			scrolled.setContent(container);
			scrolled.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			// Update the min size of the scrolled composite whenever the the size of the widgets change.
			final ControlListener resizeListener = new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					scrolled.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			};

			primaryWidget.addControlListener(resizeListener);
			baseGroup.addControlListener(resizeListener);

			return area;
		}

		private void updateBase() {
			final ClassifierOperation primaryOp = primaryWidget.getConfiguredOperation().getOperation();
			baseGroup.setVisible(ClassifierOperation.isCreate(primaryOp));

			if (baseGroup.getVisible()) {
				switch (primaryOp) {
				case NEW_COMPONENT_TYPE:
					baseValueWidget.setAllowedOperations(EnumSet.of(ClassifierOperation.EXISTING));
					break;

				case NEW_COMPONENT_IMPLEMENTATION:
					baseValueWidget.setAllowedOperations(EnumSet.of(ClassifierOperation.NEW_COMPONENT_TYPE, ClassifierOperation.EXISTING));
					break;

				case NEW_FEATURE_GROUP_TYPE:
					baseValueWidget.setAllowedOperations(EnumSet.of(ClassifierOperation.EXISTING));
					break;

				default:

				}
			}
		}
	}

	private final InnerDialog dlg;

	private CreateSelectClassifierDialog(final Shell parentShell, final Arguments args) {
		this.dlg = new InnerDialog(parentShell, args);
	}

	/**
	 * Returns if the user did not select OK.
	 * @return
	 */
	private Result open() {
		if (dlg.open() == Window.OK) {
			return new Result(dlg.primaryWidget.getConfiguredOperation(), dlg.baseValueWidget.getConfiguredOperation());
		} else {
			return null;
		}
	}

	public static Result show(final Shell parentShell, final Arguments args) {
		final CreateSelectClassifierDialog dlg = new CreateSelectClassifierDialog(parentShell, args);

		return dlg.open();
	}

	public static void main(final String[] args) {
		final Model testModel = new Model() {
			@Override
			public String getTitle() {
				return "Select Element";
			}

			@Override
			public String getMessage() {
				return "Select an element.";
			}

			@Override
			public Collection<?> getPackageOptions() {
				final List<Object> result = new ArrayList<>();
				result.add("A");
				result.add("B");
				return result;
			}

			@Override
			public String getPrimarySelectTitle() {
				return "Select Element";
			}

			@Override
			public String getPrimarySelectMessage() {
				return "Select an element.";
			}

			@Override
			public List<Object> getPrimarySelectOptions() {
				final List<Object> result = new ArrayList<>();
				result.add("C");
				result.add("D");
				return result;
			}

			@Override
			public List<Object> getUnfilteredPrimarySelectOptions() {
				final List<Object> result = getPrimarySelectOptions();
				result.add("E");
				result.add("F");
				return result;
			}

			@Override
			public List<Object> getBaseSelectOptions(final ClassifierOperation primaryOperation) {
				final List<Object> result = new ArrayList<>();
				result.add("G");
				result.add("H");
				return result;
			}

			@Override
			public Collection<?> getUnfilteredBaseSelectOptions(final ClassifierOperation primaryOperation) {
				final List<Object> result = getBaseSelectOptions(primaryOperation);
				result.add("I");
				result.add("J");
				return result;
			}
		};

		Display.getDefault().syncExec(() -> {
			// Create Component Implementation
			final Result result = show(new Shell(),
					new ArgumentBuilder(testModel, EnumSet.of(ClassifierOperation.NEW_COMPONENT_IMPLEMENTATION))
					.defaultPackage("Y").defaultSelection("Z").create());

			// TODO: Other cases

			// final Result result = show(new Shell(), testModel, EnumSet.allOf(Operation.class));
			System.err.println("Result: " + result);
		});

	}
}
