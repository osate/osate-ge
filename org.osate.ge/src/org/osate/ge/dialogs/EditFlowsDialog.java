/*******************************************************************************
 * Copyright (C) 2013 University of Alabama in Huntsville (UAH)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The US Government has unlimited rights in this work in accordance with W31P4Q-10-D-0092 DO 0073.
 *******************************************************************************/
package org.osate.ge.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.Connection;
import org.osate.aadl2.Context;
import org.osate.aadl2.Element;
import org.osate.aadl2.EndToEndFlow;
import org.osate.aadl2.EndToEndFlowElement;
import org.osate.aadl2.EndToEndFlowSegment;
import org.osate.aadl2.Feature;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.Flow;
import org.osate.aadl2.FlowElement;
import org.osate.aadl2.FlowEnd;
import org.osate.aadl2.FlowImplementation;
import org.osate.aadl2.FlowKind;
import org.osate.aadl2.FlowSegment;
import org.osate.aadl2.FlowSpecification;
import org.osate.aadl2.ModalPath;
import org.osate.aadl2.Mode;
import org.osate.aadl2.ModeFeature;
import org.osate.aadl2.ModeTransition;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Subcomponent;
import org.osate.ge.services.NamingService;
import org.osate.ge.services.PrototypeService;
import org.osate.ge.util.StringUtil;

/**
 * Dialog for editing flow implementations and end to end flows.
 * @author philip.alldredge
 *
 */
public class EditFlowsDialog extends TitleAreaDialog {
	private final ComponentImplementation ci;
	private final List<FlowSegmentInfo> potentialFlowSegments = new ArrayList<FlowSegmentInfo>();
	private final PrototypeService prototypeService;
	private final NamingService namingService;
	private final int deleteWidth = 50;
	private final int segmentWidth = 50;
	private final LabelProvider flowSegmentInfoLabelProvider = new LabelProvider() {
    	@Override
    	public String getText(final Object element) {
    		final FlowSegmentInfo fsi = (FlowSegmentInfo)element;
    		if(fsi == null) {
    			return "";
    		}
    		
    		return getSegmentName(fsi.context, fsi.flowElement);
    	}
    };
    private Flow currentFlow = null;
    private org.eclipse.swt.widgets.List flowList;
    private Button addImplFlowBtn;
    private Button addETEFlowBtn;
    private Button configureInModesBtn;
    private Button deleteFlowBtn;    
    private Composite flowDetailsPane;
    private final List<Flow> flows = new ArrayList<Flow>();
    		
	public EditFlowsDialog(final Shell parentShell, final PrototypeService prototypeService, final NamingService namingService, final ComponentImplementation ci) {
		super(parentShell);
		this.ci = ci;
		this.prototypeService = prototypeService;
		this.namingService = namingService;
		this.setHelpAvailable(false);
		populatePotentialFlowSegmentList();	 
		
		setShellStyle(getShellStyle() | SWT.RESIZE); 
	}
	
	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit Flows");
		newShell.setMinimumSize(550, 250);
	}
	
	private void populatePotentialFlowSegmentList() {
		// Populate the list of potential flow segments
		addElementsToPotentialFlowSegmentList(null, ci.getAllConnections());
		addElementsToPotentialFlowSegmentList(null, ci.getAllSubcomponents());
		addElementsToPotentialFlowSegmentList(null, ci.getAllFeatures());
		addElementsToPotentialFlowSegmentList(null, ci.getAllEndToEndFlows());
		
		// Subcomponent flow specifications
		for(final Subcomponent sc : ci.getAllSubcomponents()) {
			final ComponentClassifier scClassifier = prototypeService.getComponentClassifier(ci, sc);
			if(scClassifier instanceof ComponentType) {
				addElementsToPotentialFlowSegmentList(sc, ((ComponentType) scClassifier).getAllFlowSpecifications());
			} else if(scClassifier instanceof ComponentImplementation && ((ComponentImplementation) scClassifier).getType() != null) {
				addElementsToPotentialFlowSegmentList(sc, ((ComponentImplementation) scClassifier).getType().getAllFlowSpecifications());
			}
		}
		
		// Feature group features
		for(final Feature f : ci.getAllFeatures()) {			
			if(f instanceof FeatureGroup) {
				final FeatureGroup fg = (FeatureGroup)f;
				final FeatureGroupType fgt = prototypeService.getFeatureGroupType(ci, fg);
				if(fgt != null) {
					addElementsToPotentialFlowSegmentList(fg, fgt.getAllFeatures());
				}
			}
		}
	}
	
	private void addElementsToPotentialFlowSegmentList(final Context context, final List<? extends NamedElement> elements) {
		for(final NamedElement el : elements) {
			if(el instanceof FlowElement || el instanceof EndToEndFlowElement) {
				potentialFlowSegments.add(new FlowSegmentInfo(context, el));
			}
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle("Flows");
	}	

	@Override
  	protected Control createDialogArea(final Composite parent) {
	    final Composite area = (Composite)super.createDialogArea(parent);
	    
	    final Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); 
	    final GridLayout layout = new GridLayout(2, false);
	    container.setLayout(layout);

	    final Composite flowListPane = new Composite(container, SWT.NONE);
	    flowListPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
	    flowListPane.setLayout(new GridLayout(1, false));
	    
	    // Flow List
	    flowList = new org.eclipse.swt.widgets.List(flowListPane, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
	    final GridData flowListGridData = new GridData(GridData.FILL_BOTH);
	    flowListGridData.widthHint = 300;
	    flowListGridData.heightHint = 300;
	    flowListGridData.grabExcessHorizontalSpace = true;
	    flowList.setLayoutData(flowListGridData);

	    final ListViewer flowListViewer = new ListViewer(flowList);
	    flowListViewer.setContentProvider(new ArrayContentProvider());
	    flowListViewer.setLabelProvider(new LabelProvider() {
	    	@Override
	    	public String getText(final Object element) {
	    		final String txt;
	    		if(element instanceof EndToEndFlow) {
	    			txt = ((EndToEndFlow)element).getName() + "(End-To-End)";
	    		} else {
	    			final FlowImplementation fi = (FlowImplementation)element;
	    			txt = fi.getSpecification() == null ? "<Invalid Flow Implementation>" : fi.getSpecification().getName() + "(" + StringUtil.camelCaseToUser(fi.getSpecification().getKind().toString()) + ")";
	    		}
	    		
	    		return txt;
	    	}
	    });
	    
	    flowListPane.pack();

	    // Buttons
	    final Composite listBtns = new Composite(flowListPane, SWT.NONE);
	    listBtns.setLayout(new RowLayout(SWT.HORIZONTAL));
	    
	    // Add - Flow Impl/ETE
	    addImplFlowBtn = new Button(listBtns, SWT.PUSH);
	    addImplFlowBtn.setText("Create Impl...");
	    addImplFlowBtn.setToolTipText("Create a new flow implementation");
	    
	    addETEFlowBtn = new Button(listBtns, SWT.PUSH);
	    addETEFlowBtn.setText("Create ETE...");
	    addETEFlowBtn.setToolTipText("Create a new End-To-End flow");

	    // Configure In Modes...
	    configureInModesBtn = new Button(listBtns, SWT.PUSH);
	    configureInModesBtn.setText("Modes...");	    
	    
	    // Delete
	    deleteFlowBtn = new Button(listBtns, SWT.PUSH);
	    deleteFlowBtn.setText("Delete");	    
    
	    // Flow Details Pane
	    final ScrolledComposite detailsScrolled = new ScrolledComposite(container, SWT.V_SCROLL | SWT.BORDER);
	    final GridData detailsScrolledGridData = new GridData(GridData.FILL_BOTH);
	    detailsScrolledGridData.widthHint = 350;
	    detailsScrolledGridData.grabExcessHorizontalSpace = true;
	    detailsScrolledGridData.grabExcessVerticalSpace = true;
	    detailsScrolled.setLayoutData(detailsScrolledGridData);
	    detailsScrolled.setExpandHorizontal(true);
	    detailsScrolled.setLayout(new GridLayout(1, false));	
		
	    flowDetailsPane = new Composite(detailsScrolled, SWT.NONE);
	    final GridData flowDetailsGridData = new GridData(GridData.FILL_BOTH);
	    flowDetailsGridData.grabExcessHorizontalSpace = true;
	    flowDetailsGridData.widthHint = 250;
	    flowDetailsPane.setLayout(new GridLayout(3, false));
	    detailsScrolled.setContent(flowDetailsPane);
	    
	    flowListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final StructuredSelection selection = (StructuredSelection)event.getSelection();
				final Flow flow = (Flow)selection.getFirstElement();
				updateFlowDetails(flow);
			}	    	
	    });
	    
	    // Make a copy of the flows and use it as the input to the flow list viewer.
	    // The copies share flow specifications but have a different set of owned flow segments
	    flows.clear();
	    for(final FlowImplementation fi : ci.getOwnedFlowImplementations()) {
	    	if(fi.getInModeOrTransitions().size() == 0) {
    			flows.add(EcoreUtil.copy(fi));
    		} else {
    			// Use an alternative mechanism to copy flows if the in modes clause is not empty. This is needed because EcoreUtil.copy does not work properly in that case. It throws an exception.
    			// See osate2-core issue #598
    			final EClass flowClass = fi.eClass();
    			final FlowImplementation copiedFlowImplementation = (FlowImplementation)flowClass.getEPackage().getEFactoryInstance().create(flowClass);
    			copiedFlowImplementation.setName(fi.getName());
    			copiedFlowImplementation.setKind(fi.getKind());
    			copiedFlowImplementation.setSpecification(fi.getSpecification());
    			copiedFlowImplementation.getInModeOrTransitions().addAll(EcoreUtil.copyAll(fi.getInModeOrTransitions()));
    			copiedFlowImplementation.getOwnedComments().addAll(EcoreUtil.copyAll(fi.getOwnedComments()));
    			copiedFlowImplementation.getOwnedElements().addAll(EcoreUtil.copyAll(fi.getOwnedElements()));
    			copiedFlowImplementation.getOwnedFlowSegments().addAll(EcoreUtil.copyAll(fi.getOwnedFlowSegments()));
    			copiedFlowImplementation.getOwnedPropertyAssociations().addAll(EcoreUtil.copyAll(fi.getOwnedPropertyAssociations()));	    			
    			flows.add(copiedFlowImplementation);
    		}
	    }

	    // Add all end to end flows that are not refinements
	    for(final EndToEndFlow eteFlow : ci.getOwnedEndToEndFlows()) {
	    	if(eteFlow.getRefined() == null) {
	    		if(eteFlow.getInModeOrTransitions().size() == 0) {
	    			flows.add(EcoreUtil.copy(eteFlow));
	    		} else {
	    			// Use an alternative mechanism to copy flows if the in modes clause is not empty. This is needed because EcoreUtil.copy does not work properly in that case. It throws an exception.
	    			// See osate2-core issue #598
	    			final EClass flowClass = eteFlow.eClass();
	    			final EndToEndFlow copiedEndToEndFlow = (EndToEndFlow)flowClass.getEPackage().getEFactoryInstance().create(flowClass);
	    			copiedEndToEndFlow.setName(eteFlow.getName());
	    			copiedEndToEndFlow.getInModeOrTransitions().addAll(EcoreUtil.copyAll(eteFlow.getInModeOrTransitions()));
	    			copiedEndToEndFlow.getOwnedComments().addAll(EcoreUtil.copyAll(eteFlow.getOwnedComments()));
	    			copiedEndToEndFlow.getOwnedElements().addAll(EcoreUtil.copyAll(eteFlow.getOwnedElements()));
	    			copiedEndToEndFlow.getOwnedEndToEndFlowSegments().addAll(EcoreUtil.copyAll(eteFlow.getOwnedEndToEndFlowSegments()));
	    			copiedEndToEndFlow.getOwnedPropertyAssociations().addAll(EcoreUtil.copyAll(eteFlow.getOwnedPropertyAssociations()));	    			
	    			flows.add(copiedEndToEndFlow);
	    		}
	    	}
	    }
	    flowListViewer.setInput(flows);

	    addImplFlowBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Prompt user to select a flow specification
				final ElementSelectionDialog flowSpecSelectionDlg = new ElementSelectionDialog(getShell(), "Create Flow Implementation", "Select a Flow Specification", ci.getType().getAllFlowSpecifications());
				if(flowSpecSelectionDlg.open() == Dialog.CANCEL) {
					return;
				}

				final Object selectedFlowSpecification = flowSpecSelectionDlg.getFirstSelectedElement();
				if(selectedFlowSpecification instanceof FlowSpecification) {
					// Create a flow implementation
					final Aadl2Package pkg = Aadl2Factory.eINSTANCE.getAadl2Package();
					final FlowImplementation newFlow = (FlowImplementation)pkg.getEFactoryInstance().create(pkg.getFlowImplementation());
	
					// Set the flow specification
					final FlowSpecification flowSpec = (FlowSpecification)selectedFlowSpecification;
					
					newFlow.setSpecification(flowSpec);
					newFlow.setKind(flowSpec.getKind());
					
					// Add the flow to the list and refresh the flow list viewer
					flows.add(newFlow);
					flowListViewer.refresh();
					flowListViewer.setSelection(new StructuredSelection(newFlow));
				}
			}
	    });
	    
	    addETEFlowBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Prompt the user for a name for the new end to end flow
				final InputDialog namePromptDlg = new NameInputDialog(getShell(), "Create End-to-End Flow", "Enter a name for the new End-to-End flow", "", new IInputValidator() {
					@Override
					public String isValid(final String newText) {
						// Check if the name is a valid identifier and is not being used
						boolean invalid = false;
						if(!namingService.isValidIdentifier(newText) || namingService.isNameInUse(ci, newText)) {
							invalid = true;
						}
						
						// Check that none of the flows being edited have the specified name
						for(final Flow flow : flows) {
							if(newText.equalsIgnoreCase(flow.getName())) {
								invalid = true;
								break;
							}
						}
						
						return invalid ? "The specified name is not valid." : null;
					}	
			
				});
				
				if(namePromptDlg.open() == Dialog.CANCEL) {
					return;
				}

				// Create a new end to end flow
				final Aadl2Package pkg = Aadl2Factory.eINSTANCE.getAadl2Package();
				final EndToEndFlow newFlow = (EndToEndFlow)pkg.getEFactoryInstance().create(pkg.getEndToEndFlow());
				
				// Set the name
				newFlow.setName(namePromptDlg.getValue());
				
				// Add the flow to the list and refresh the flow list viewer
				flows.add(newFlow);
				flowListViewer.refresh();
				flowListViewer.setSelection(new StructuredSelection(newFlow));				
			}
	    });
	    
	    configureInModesBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// Allow editing the modes of the current flow
				if(currentFlow instanceof ModalPath) {					
					final ModalPath mp = (ModalPath)currentFlow;					
					final List<ModeFeature> localModeFeatures = new ArrayList<ModeFeature>(ci.getAllModes());
					localModeFeatures.addAll(ci.getAllModeTransitions());

					final Map<String, String> localToChildModeMap = new HashMap<String, String>();
					for(final ModeFeature mf : mp.getInModeOrTransitions()) {
						localToChildModeMap.put(mf.getName(), null);
					}		
					
					final List<String> localModeFeatureNames = new ArrayList<String>();
					final List<String> localModeTransitionFeatureNames = new ArrayList<String>();
					for(final ModeFeature tmpMode : localModeFeatures) {
						if (tmpMode instanceof Mode){
							localModeFeatureNames.add(tmpMode.getName());
						} else {
							if(tmpMode instanceof ModeTransition){
								localModeTransitionFeatureNames.add(tmpMode.getName());
							}
						}
					}
					
					// Show the dialog
					final SetInModeFeaturesDialog dlg = new SetInModeFeaturesDialog(getShell(), localModeFeatureNames, localModeTransitionFeatureNames, null, localToChildModeMap);
					if(dlg.open() == Dialog.CANCEL) {
						return;
					}
					
					mp.getInModeOrTransitions().clear();
					for(final Entry<String, String> localToChildModeMapEntry : dlg.getLocalToChildModeMap().entrySet()) {
						final ModeFeature localMode = findModeByName(localModeFeatures, localToChildModeMapEntry.getKey());
						if(localMode != null) {
							mp.getInModeOrTransitions().add(localMode);
						}
					}
				}
			}
	    });
	    
	    deleteFlowBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// Remove the current flow from the flow list and refresh the viewer
				if(currentFlow != null) {
					flows.remove(currentFlow);
					flowListViewer.refresh();
					flowListViewer.setSelection(null);
							
					refreshWidgetEnabledStates();
				}
			}
	    });
	    
	    refreshWidgetEnabledStates();
	    
	    return area;
	}
	
	private ModeFeature findModeByName(final List<? extends ModeFeature> modeFeatures, final String name) {
		for(final ModeFeature tmpModeFeature : modeFeatures) {
			if(name.equalsIgnoreCase(tmpModeFeature.getName())) {
				return tmpModeFeature;
			}
		}
		
		return null;
	}
	
	private boolean isValid(final Flow flow) {
		// TODO: Cleanup. Is it possible to have 1 function that is used for all the checks?
		// Perform a basic validity check. Ensure that the number of elements is reasonable, that there aren't any null segments, and that connections are not adjacent in the segment list
		if(flow instanceof EndToEndFlow) {
			final EndToEndFlow eteFlow = (EndToEndFlow)flow;
			
			// Check for minimum number of flow segments
			if(eteFlow.getAllFlowSegments().size() < 3 || (eteFlow.getAllFlowSegments().size() % 2) != 1) {
				return false;
			}
			
			EndToEndFlowSegment prevSegment = null;
			for(final EndToEndFlowSegment seg : eteFlow.getAllFlowSegments()) {
				if(seg.getFlowElement() == null) {
					return false;
				}

				if(prevSegment == null) {
					// start_subcomponent_flow_or_etef_identifier
					if(seg.getFlowElement() instanceof Connection) {
						return false;
					}
				} else {
					// Ensure that two connections or two flow paths are not connected to one another
					if((prevSegment.getFlowElement() instanceof Connection) == (seg.getFlowElement() instanceof Connection)) {
						return false;
					}
				}
				
				prevSegment = seg;
			}			
			
		} else if(flow instanceof FlowImplementation) {
			final FlowImplementation fi = (FlowImplementation)flow;
			if(fi.getKind() == FlowKind.SOURCE) {
				//{ subcomponent_flow_identifier -> connection_identifier -> }*
				FlowSegment prevSegment = null;				
				if((fi.getOwnedFlowSegments().size() % 2) != 0) {
					return false;
				}
				
				for(final FlowSegment seg : fi.getOwnedFlowSegments()) {
					if(seg.getFlowElement() == null) {
						return false;
					}
					
					if(prevSegment == null) {
						if(seg.getFlowElement() instanceof Connection) {
							return false;
						}
					} else if((prevSegment.getFlowElement() instanceof Connection) == (seg.getFlowElement() instanceof Connection)) { 
						// Ensure that two connections or two flow paths are not connected to one another
						return false;
					}
					
					prevSegment = seg;
				}
			} else if(fi.getKind() == FlowKind.SINK) {
				//{ -> connection_identifier -> subcomponent_flow_identifier }*
				FlowSegment prevSegment = null;				
				if((fi.getOwnedFlowSegments().size() % 2) != 0) {
					return false;
				}
				
				for(final FlowSegment seg : fi.getOwnedFlowSegments()) {
					if(seg.getFlowElement() == null) {
						return false;
					}
					
					if(prevSegment == null) {
						if(!(seg.getFlowElement() instanceof Connection)) {
							return false;
						}
					} else if((prevSegment.getFlowElement() instanceof Connection) == (seg.getFlowElement() instanceof Connection)) {
						// Ensure that two connections or two flow paths are not connected to one another
						return false;
					}
					
					prevSegment = seg;
				}
			} else if(fi.getKind() == FlowKind.PATH) {
				//	[ { -> connection_identifier -> subcomponent_flow_identifier }+
				//  -> connection_identifier ]
				FlowSegment prevSegment = null;				
				if((fi.getOwnedFlowSegments().size() % 2) != 1) {
					return false;
				}
				
				for(final FlowSegment seg : fi.getOwnedFlowSegments()) {
					if(seg.getFlowElement() == null) {
						return false;
					}
					
					if(prevSegment == null) {
						if(!(seg.getFlowElement() instanceof Connection)) {
							return false;
						}
					} else if((prevSegment.getFlowElement() instanceof Connection) == (seg.getFlowElement() instanceof Connection)) {
						// Ensure that two connections or two flow paths are not connected to one another
						return false;
					}
					
					prevSegment = seg;
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	public List<Flow> getFlows() {
		return flows;
	}

	private void updateFlowDetails(final Flow flow) {
		// Set the current flow
		currentFlow = flow;
		refreshWidgetEnabledStates();
		
		// Clear
		for(Control child : flowDetailsPane.getChildren()) {
			if(!child.isDisposed()) {
				child.dispose();
			}
	    }
	
		int insertIndex = 0;
		if(flow instanceof EndToEndFlow) {
			final EndToEndFlow f = (EndToEndFlow)flow;
			addInsertRow(insertIndex++);
			for(final EndToEndFlowSegment fs : f.getAllFlowSegments()) {
				addEditableFlowSegment(fs, new FlowSegmentInfo(fs.getContext(), fs.getFlowElement()), insertIndex++);
			}
		} else if(flow instanceof FlowImplementation) {
			final FlowImplementation f = (FlowImplementation)flow;
			final FlowSpecification flowSpec = f.getSpecification();
			if(flowSpec != null) {
				switch(flowSpec.getKind()) {
				case SOURCE: {
					final FlowEnd outEnd = flowSpec.getOutEnd();
					
					if(outEnd != null) {
						addInsertRow(insertIndex++);
						for(final FlowSegment fs : f.getOwnedFlowSegments()) {
							addEditableFlowSegment(fs, new FlowSegmentInfo(fs.getContext(), fs.getFlowElement()), insertIndex++);
						}
						addReadOnlyDetailsRow(outEnd.getContext(), outEnd.getFeature(), false, insertIndex++);		
					}
					break;
				}
					
				case SINK: {
					final FlowEnd inEnd = flowSpec.getInEnd();
					if(inEnd != null) {
						addReadOnlyDetailsRow(inEnd.getContext(), inEnd.getFeature(), true, insertIndex++);	
					
						for(final FlowSegment fs : f.getOwnedFlowSegments()) {
							addEditableFlowSegment(fs, new FlowSegmentInfo(fs.getContext(), fs.getFlowElement()), insertIndex++);
						}
					}
					break;
				}
					
				case PATH:	{
					final FlowEnd inEnd = flowSpec.getInEnd();
					final FlowEnd outEnd = flowSpec.getOutEnd();
					if(inEnd != null & outEnd != null) {
						addReadOnlyDetailsRow(inEnd.getContext(), inEnd.getFeature(), true, insertIndex++);
						for(final FlowSegment fs : f.getOwnedFlowSegments()) {
							addEditableFlowSegment(fs, new FlowSegmentInfo(fs.getContext(), fs.getFlowElement()), insertIndex++);
						}						
						addReadOnlyDetailsRow(outEnd.getContext(), outEnd.getFeature(), false, insertIndex++);
					}
						
					break;
				}
				}
			}
		}

		flowDetailsPane.pack();
		flowDetailsPane.layout(true);
	}
	
	private String getSegmentName(final Context ctx, final NamedElement flowElement) {
		String name = "";
		if(ctx != null) {
			name += ctx.getName() == null ? "<unknown>" : ctx.getName();
			name += ".";
		}
		
		name += flowElement.getName() == null ? "<unknown>" : flowElement.getName();
		return name;
	}
	
	private GridData createSegmentGridData() {
		final GridData segGridData = new GridData(GridData.FILL_HORIZONTAL);
    	segGridData.grabExcessHorizontalSpace = true;
    	segGridData.widthHint = segmentWidth;
    	return segGridData;
	}
	
	private void addInsertRow(final int insertIndex) {
		// Create row of widgets. Just insert box
		new Label(flowDetailsPane, SWT.NONE).setLayoutData(GridDataFactory.fillDefaults().hint(deleteWidth, SWT.DEFAULT).create());
		new Label(flowDetailsPane, SWT.NONE).setLayoutData(createSegmentGridData());		
		addInsertButton(insertIndex);
	}
	
	private void addInsertButton(final int insertIndex) {
		final Button insertSegmentBtn = new Button(flowDetailsPane, SWT.PUSH);
    	insertSegmentBtn.setText("Insert");
    	insertSegmentBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Create segment at the correct position
				if(currentFlow instanceof EndToEndFlow) {
					final EndToEndFlow eteFlow = (EndToEndFlow)currentFlow;
					eteFlow.getOwnedEndToEndFlowSegments().move(insertIndex, eteFlow.createOwnedEndToEndFlowSegment());
				} else if(currentFlow instanceof FlowImplementation) {
					final FlowImplementation fi = (FlowImplementation)currentFlow;
					fi.getOwnedFlowSegments().move(insertIndex, fi.createOwnedFlowSegment());
				}

				updateFlowDetails(currentFlow);
			}
	    });
	}
	
	private void addEditableFlowSegment(final Element flowSegment, final FlowSegmentInfo selectedSegment, final int insertIndex) {		
    	final Button deleteSegmentBtn = new Button(flowDetailsPane, SWT.PUSH);
	    deleteSegmentBtn.setText("Delete");
	    deleteSegmentBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Remove the segment
				EcoreUtil.remove(flowSegment);

				// Refresh details
				updateFlowDetails(currentFlow);
			}
	    });

	    final ComboViewer cmb = new ComboViewer(flowDetailsPane, SWT.DROP_DOWN | SWT.READ_ONLY);
    	cmb.getCombo().setLayoutData(createSegmentGridData());
    	cmb.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final StructuredSelection selection = (StructuredSelection)event.getSelection();
				final FlowSegmentInfo newSegmentInfo = (FlowSegmentInfo)selection.getFirstElement();
				
				if(flowSegment instanceof EndToEndFlowSegment) {
					final EndToEndFlowSegment fs = (EndToEndFlowSegment)flowSegment;
					if(newSegmentInfo == null) {
						fs.setContext(null);
						fs.setFlowElement(null);
					} else {
						fs.setContext(newSegmentInfo.context);
						fs.setFlowElement((EndToEndFlowElement)newSegmentInfo.flowElement);
					}
				} else if(flowSegment instanceof FlowSegment) {
					final FlowSegment fs = (FlowSegment)flowSegment;
					if(newSegmentInfo == null) {
						fs.setContext(null);
						fs.setFlowElement(null);
					} else {
						fs.setContext(newSegmentInfo.context);
						fs.setFlowElement((FlowElement)newSegmentInfo.flowElement);
					}
				}
				
				refreshWidgetEnabledStates();
			}	    	
	    });
    	cmb.setContentProvider(new ArrayContentProvider());
	    cmb.setLabelProvider(flowSegmentInfoLabelProvider);	    
    	cmb.setInput(potentialFlowSegments);
    	cmb.setSelection(new StructuredSelection(selectedSegment));
    	
    	if(flowSegment instanceof EndToEndFlowSegment) {
			cmb.setFilters(new ViewerFilter[]{endToEndFlowElementFilter});
		} else if(flowSegment instanceof FlowSegment) {
			cmb.setFilters(new ViewerFilter[]{flowElementFilter});
		}

    	addInsertButton(insertIndex);
	}
	
	private ViewerFilter endToEndFlowElementFilter = new ViewerFilter() {
		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
			return ((FlowSegmentInfo)element).flowElement instanceof EndToEndFlowElement;
		}		
	};
	
	private ViewerFilter flowElementFilter = new ViewerFilter() {
		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
			return ((FlowSegmentInfo)element).flowElement instanceof FlowElement;
		}		
	};	
	
	private void addReadOnlyDetailsRow(final Context ctx, final NamedElement flowElement, boolean showInsertButton, final int insertIndex) {
		new Label(flowDetailsPane, SWT.NONE).setLayoutData(GridDataFactory.fillDefaults().hint(deleteWidth, SWT.DEFAULT).create());

		final Label segmentLbl = new Label(flowDetailsPane, SWT.NONE);
		final String lblTxt = getSegmentName(ctx, flowElement);
		segmentLbl.setLayoutData(createSegmentGridData());
		segmentLbl.setText(lblTxt);

		if(showInsertButton) {
			addInsertButton(insertIndex);
		} else {
			new Label(flowDetailsPane, SWT.NONE);
		}
	}
		
	private void refreshWidgetEnabledStates() {
		setNavigationButtonsEnabled(currentFlow == null ? true : isValid(currentFlow));
        configureInModesBtn.setEnabled(currentFlow != null);
	    deleteFlowBtn.setEnabled(currentFlow != null); 
	}
	
    private void setNavigationButtonsEnabled(final boolean enabled) {
    	flowList.setEnabled(enabled);
        addImplFlowBtn.setEnabled(enabled);
        addETEFlowBtn.setEnabled(enabled);
        final Button okBtn = getButton(IDialogConstants.OK_ID);
        if(okBtn != null) {
        	getButton(IDialogConstants.OK_ID).setEnabled(enabled);
        }
    }
    	
	private static class FlowSegmentInfo {
		public final Context context;
		public final NamedElement flowElement;
		
		public FlowSegmentInfo(final Context context, final NamedElement flowElement) {
			this.context = context;
			this.flowElement = flowElement;
		}
		
		public int hashCode() {
	        return (context == null ? 0 : context.hashCode()) + (flowElement == null ? 0 : flowElement.hashCode());
	    }

	    public boolean equals(Object obj) {
	        if(obj == null)
	            return false;
	        
	        if(obj == this)
	            return true;
	        
	        if(!(obj instanceof FlowSegmentInfo))
	            return false;

	        final FlowSegmentInfo rhs = (FlowSegmentInfo)obj;
	        return ((context == null && rhs.context == null) || (context != null && context.equals(rhs.context))) &&
	        		((flowElement == null && rhs.flowElement == null) || (flowElement != null && flowElement.equals(rhs.flowElement)));
	    }
	}
	
	private class NameInputDialog extends InputDialog {
		public NameInputDialog(final Shell parentShell, final String dialogTitle, final String dialogMessage, final String initialValue, final IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}
		
		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setMinimumSize(225, 200);
		}
	}
}
