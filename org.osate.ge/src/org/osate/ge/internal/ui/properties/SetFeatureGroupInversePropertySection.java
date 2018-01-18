package org.osate.ge.internal.ui.properties;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.ge.BusinessObjectSelection;
import org.osate.ge.internal.util.AadlFeatureUtil;
import org.osate.ge.ui.properties.PropertySectionUtil;

public class SetFeatureGroupInversePropertySection extends AbstractPropertySection {
	public static class Filter implements IFilter {
		@Override
		public boolean select(final Object toTest) {
			return PropertySectionUtil.isBocCompatible(toTest, boc -> {
				if(boc.getBusinessObject() instanceof FeatureGroup) {
					final FeatureGroup fg = (FeatureGroup) boc.getBusinessObject();
					final Classifier classifier = fg.getContainingClassifier();
					if ((classifier instanceof FeatureGroupType || classifier instanceof ComponentType)) {
						return AadlFeatureUtil.getFeatureGroupType(boc, fg) != null;
					}
				}

				return false;
			});
		}
	}

	private BusinessObjectSelection selectedBos;
	private Button trueBtn;
	private Button falseBtn;

	private final SelectionListener inverseSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Button btn = (Button) e.widget;
			if (btn.getSelection()) {
				final boolean isInverse = (boolean) e.widget.getData();
				selectedBos.modify(FeatureGroup.class, fg -> fg.setInverse(isInverse));
			}
		}
	};

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);

		final Composite inverseContainer = PropertySectionUtil.createRowLayoutComposite(getWidgetFactory(), composite,
				STANDARD_LABEL_WIDTH);

		trueBtn = PropertySectionUtil.createButton(getWidgetFactory(), inverseContainer, true,
				inverseSelectionListener, "True", SWT.RADIO);
		falseBtn = PropertySectionUtil.createButton(getWidgetFactory(), inverseContainer, false,
				inverseSelectionListener, "False", SWT.RADIO);

		PropertySectionUtil.createSectionLabel(composite, getWidgetFactory(), "Inverse:");
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		selectedBos = Adapters.adapt(selection, BusinessObjectSelection.class);
	}

	@Override
	public void refresh() {
		final Set<Boolean> selectedDirections = selectedBos.boStream(FeatureGroup.class).map(fg -> fg.isInverse())
				.collect(Collectors.toSet());

		trueBtn.setSelection(selectedDirections.contains(true));
		falseBtn.setSelection(selectedDirections.contains(false));
	}
}
