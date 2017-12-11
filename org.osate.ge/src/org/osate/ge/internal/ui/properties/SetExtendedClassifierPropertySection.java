package org.osate.ge.internal.ui.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.PackageSection;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.ui.dialogs.ElementSelectionDialog;
import org.osate.ge.internal.util.ScopedEMFIndexRetrieval;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class SetExtendedClassifierPropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBoCompatible(toTest, bo -> {
				return bo instanceof ComponentType || bo instanceof ComponentImplementation
						|| bo instanceof FeatureGroupType;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private Label curExtendedClassifier;
	private Button chooseBtn;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		FormData fd;

		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		final Composite container = getWidgetFactory().createComposite(composite);
		final Label sectionLabel = PropertySectionUtil.createSectionLabel(composite, container, getWidgetFactory(), "Extends:");

		container.setLayout(new FormLayout());
		fd = new FormData();
		fd.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		fd.top = new FormAttachment(sectionLabel, 0, SWT.CENTER);
		container.setLayoutData(fd);

		curExtendedClassifier = getWidgetFactory().createLabel(container, "");
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		curExtendedClassifier.setLayoutData(fd);

		chooseBtn = PropertySectionUtil.createButton(getWidgetFactory(), container, null, new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final List<Classifier> classifiers = selectedBos.boStream(Classifier.class)
						.collect(Collectors.toList());
				Iterator<Classifier> it = classifiers.iterator();
				final Classifier classifier = it.next();
				final List<IEObjectDescription> extensibleClassifierDescs = new ArrayList<>(
						getExtensibleClassifierDescriptions(classifier));
				while (it.hasNext()) {
					extensibleClassifierDescs.retainAll(getExtensibleClassifierDescriptions(it.next()));
				}

				// Prompt the user for the element
				final ElementSelectionDialog dlg = new ElementSelectionDialog(Display.getCurrent().getActiveShell(),
						"Select a Classifier", "Select a classifier to extend.",
						extensibleClassifierDescs);
				if (dlg.open() != Window.CANCEL) {
					final AadlPackage pkg = (AadlPackage) classifier.eResource().getContents().get(0);
					final PackageSection section = pkg.getPublicSection();

					// Resolve the selected classifier
					final Classifier selectedClassifier = (dlg.getFirstSelectedElement() != null
							&& ((EObject) dlg.getFirstSelectedElement()).eIsProxy())
							? (Classifier) EcoreUtil.resolve(((EObject) dlg.getFirstSelectedElement()),
									classifier.eResource())
									: (Classifier) dlg.getFirstSelectedElement();
							if (selectedClassifier != null) {
								// Import the package if necessary
								if (selectedClassifier.getNamespace() != null) {
									final AadlPackage selectedClassifierPkg = (AadlPackage) selectedClassifier.getNamespace()
											.getOwner();
									if (pkg != selectedClassifierPkg
											&& !section.getImportedUnits().contains(selectedClassifierPkg)) {
										section.getImportedUnits().add(selectedClassifierPkg);
									}
								}

								selectedBos.modify(Classifier.class, selClassifier -> {
									// Extend the classifier
									if (selClassifier instanceof ComponentType) {
										((ComponentType) selClassifier).createOwnedExtension()
										.setExtended((ComponentType) selectedClassifier);
									} else if (selClassifier instanceof ComponentImplementation) {
										((ComponentImplementation) selClassifier).createOwnedExtension()
										.setExtended((ComponentImplementation) selectedClassifier);
									} else if (selClassifier instanceof FeatureGroupType) {
										((FeatureGroupType) selClassifier).createOwnedExtension()
										.setExtended((FeatureGroupType) selectedClassifier);
									}
								});
							}
				}
			}
		}, "Choose...", SWT.PUSH);

		fd = new FormData();
		fd.left = new FormAttachment(curExtendedClassifier, ITabbedPropertyConstants.HSPACE);
		fd.top = new FormAttachment(curExtendedClassifier, 0, SWT.CENTER);
		chooseBtn.setLayoutData(fd);
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final List<Classifier> elements = selectedBos.boStream(Classifier.class).collect(Collectors.toList());
		final Classifier classifier = elements.get(0);
		final Classifier extClassifier = getExtended(classifier);
		// TODO extClassifier is null when an element is refined???
		if (extClassifier != null) {
			if (extClassifier.getName() != null) {
				curExtendedClassifier.setText(extClassifier == null ? "<None>" : extClassifier.getName());
			}
		} else {
			curExtendedClassifier.setText("<None>");
		}
	}

	private static Classifier getExtended(final Classifier classifier) {
		Object extendObject;
		if (classifier instanceof ComponentType) {
			extendObject = ((ComponentType) classifier).getExtended();
		} else if (classifier instanceof ComponentImplementation) {
			extendObject = ((ComponentImplementation) classifier).getExtended();
		} else if (classifier instanceof FeatureGroupType) {
			extendObject = ((FeatureGroupType) classifier).getExtended();
		} else {
			throw new RuntimeException("Unsupported type: " + classifier);
		}

		return extendObject == null ? null : (Classifier) extendObject;
	}

	/**
	 * Return a list of EObjectDescriptions for classifiers that could be extended.
	 * @return
	 */
	private List<IEObjectDescription> getExtensibleClassifierDescriptions(final Classifier classifier) {
		final List<IEObjectDescription> objectDescriptions = new ArrayList<IEObjectDescription>();
		final String name = classifier.getQualifiedName();

		// Populate the list with valid classifier descriptions
		if (name != null) {
			for (final IEObjectDescription desc : ScopedEMFIndexRetrieval.getAllEObjectsByType(classifier.eResource(),
					classifier.eClass())) {
				if (!name.equalsIgnoreCase(desc.getName().toString("::"))) {
					objectDescriptions.add(desc);
				}
			}

// Ensure that abstract classifiers are in the list
			if (classifier instanceof ComponentType) {
				if (classifier.eClass() != Aadl2Factory.eINSTANCE.getAadl2Package().getAbstractType()) {
					for (final IEObjectDescription desc : ScopedEMFIndexRetrieval.getAllEObjectsByType(
							classifier.eResource(), Aadl2Factory.eINSTANCE.getAadl2Package().getAbstractType())) {
						if (!name.equalsIgnoreCase(desc.getName().toString("::"))) {
							objectDescriptions.add(desc);
						}
					}
				}
			} else if (classifier instanceof ComponentImplementation) {
				if (classifier.eClass() != Aadl2Factory.eINSTANCE.getAadl2Package().getAbstractImplementation()) {
					for (final IEObjectDescription desc : ScopedEMFIndexRetrieval.getAllEObjectsByType(
							classifier.eResource(),
							Aadl2Factory.eINSTANCE.getAadl2Package().getAbstractImplementation())) {
						if (!name.equalsIgnoreCase(desc.getName().toString("::"))) {
							objectDescriptions.add(desc);
						}
					}
				}
			}
		}

		return objectDescriptions;
	}
}
