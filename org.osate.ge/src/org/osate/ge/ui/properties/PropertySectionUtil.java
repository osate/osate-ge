package org.osate.ge.ui.properties;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.osate.ge.BusinessObjectContext;

public class PropertySectionUtil {
	public static boolean isBocCompatible(final Object toTest, final Predicate<BusinessObjectContext> predicate) {
		final BusinessObjectContext boc = Adapters.adapt(toTest, BusinessObjectContext.class);
		final Object bo = boc == null ? null : boc.getBusinessObject();
		return boc != null && (!(bo instanceof EObject) || !((EObject) bo).eIsProxy()) && predicate.test(boc);
	}

	public static boolean isBoCompatible(final Object toTest, final Predicate<Object> predicate) {
		final BusinessObjectContext boc = Adapters.adapt(toTest, BusinessObjectContext.class);
		final Object bo = boc == null ? null : boc.getBusinessObject();
		return bo != null && (!(bo instanceof EObject) || !((EObject) bo).eIsProxy())
				&& predicate.test(boc.getBusinessObject());
	}

	/**
	 * Copies structural feature values from original to replacement. If replacement does not contain a matching structural feature, the value is ignored. If a feature is not set,
	 * its value is not copied over to the replacement.
	 * @param original
	 * @param replacement
	 */
	public static void transferStructuralFeatureValues(final EObject original, final EObject replacement) {
		for (final EStructuralFeature feature : original.eClass().getEAllStructuralFeatures()) {
			if (feature.isChangeable() && !feature.isDerived()) {
				final Object originalValue = original.eGet(feature, true);

				// Only copy values that are set
				if (original.eIsSet(feature)) {
					if (replacement.eClass().getEAllStructuralFeatures().contains(feature)) {
						if (feature.isMany()) {
							final @SuppressWarnings("unchecked") List<Object> originalList = (List<Object>) originalValue;
							final Object replacementValue = replacement.eGet(feature);
							final @SuppressWarnings("unchecked") List<Object> replacementList = (List<Object>) replacementValue;
							replacementList.addAll(originalList);
						} else {
							replacement.eSet(feature, originalValue);
						}
					}
				}
			}
		}
	}

	public static ComboViewer createComboViewer(final Composite container, final int lblWidth,
			final SelectionAdapter selectionAdapter, final LabelProvider lblProvider) {
		final ComboViewer comboViewer = new ComboViewer(container);
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(lblProvider);

		final Combo combo = comboViewer.getCombo();
		final FormData fd = new FormData();
		fd.left = new FormAttachment(0, lblWidth);
		combo.setLayoutData(fd);
		combo.addSelectionListener(selectionAdapter);
		return comboViewer;
	}

	public static Label createSectionLabel(final Composite container, final Control control,
			final TabbedPropertySheetWidgetFactory widgetFactory, final String lblTxt) {
		final Label label = widgetFactory.createLabel(container, lblTxt);
		final FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(control, 0, SWT.CENTER);
		label.setLayoutData(fd);
		return label;
	}

	public static Button createButton(final TabbedPropertySheetWidgetFactory widgetFactory, final Composite composite,
			final Object data, final SelectionListener listener, final String txt, final int type) {
		final Button btn = widgetFactory.createButton(composite, txt, type);
		btn.setData(data);
		btn.addSelectionListener(listener);
		return btn;
	}

	public static Composite createRowLayoutComposite(final TabbedPropertySheetWidgetFactory widgetFactory,
			final Composite composite, final int offset) {
		final Composite container = widgetFactory.createComposite(composite);
		container.setLayout(RowLayoutFactory.fillDefaults().wrap(false).create());
		final FormData ld = new FormData();
		ld.left = new FormAttachment(0, offset);
		ld.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		container.setLayoutData(ld);
		return container;
	}

	public static TableViewerColumn createTableColumnViewer(final TableViewer tableViewer, final String colHeader,
			final int style, final CellLabelProvider cellLabelProvider) {
		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, style);
		tableViewerColumn.getColumn().setText(colHeader);
		tableViewerColumn.setLabelProvider(cellLabelProvider);
		return tableViewerColumn;
	}
}
