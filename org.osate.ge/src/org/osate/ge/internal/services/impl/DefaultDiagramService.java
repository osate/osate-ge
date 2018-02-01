package org.osate.ge.internal.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.osate.aadl2.NamedElement;
import org.osate.ge.EmfContainerProvider;
import org.osate.ge.internal.AgeDiagramProvider;
import org.osate.ge.internal.diagram.runtime.AgeDiagram;
import org.osate.ge.internal.diagram.runtime.CanonicalBusinessObjectReference;
import org.osate.ge.internal.diagram.runtime.DiagramConfigurationBuilder;
import org.osate.ge.internal.diagram.runtime.DiagramElement;
import org.osate.ge.internal.diagram.runtime.DiagramModification;
import org.osate.ge.internal.diagram.runtime.DiagramNode;
import org.osate.ge.internal.diagram.runtime.DiagramSerialization;
import org.osate.ge.internal.diagram.runtime.RelativeBusinessObjectReference;
import org.osate.ge.internal.diagram.runtime.types.CustomDiagramType;
import org.osate.ge.internal.indexing.SavedDiagramIndex;
import org.osate.ge.internal.indexing.SavedDiagramIndexInvalidator;
import org.osate.ge.internal.services.DiagramService;
import org.osate.ge.internal.services.ExtensionRegistryService;
import org.osate.ge.internal.services.ReferenceService;
import org.osate.ge.internal.ui.editor.AgeDiagramBehavior;
import org.osate.ge.internal.ui.editor.AgeDiagramEditor;
import org.osate.ge.internal.ui.util.EditorUtil;
import org.osate.ge.internal.ui.util.SelectionUtil;
import org.osate.ge.internal.util.BusinessObjectProviderHelper;
import org.osate.ge.internal.util.Log;
import org.osate.ge.internal.util.NonUndoableToolCommand;

public class DefaultDiagramService implements DiagramService {
	private static final QualifiedName LEGACY_PROPERTY_NAME_MODIFICATION_TIMESTAMP = new QualifiedName("org.osate.ge", "diagram_name_modification_stamp");
	private static final QualifiedName LEGACY_PROPERTY_DIAGRAM_NAME = new QualifiedName("org.osate.ge", "diagram_name");
	private static final Pattern uuidFilenamePattern = Pattern.compile("[0-9a-f]{4,}-[0-9a-f]{2,}-[0-9a-f]{2,}-[0-9a-f]{2,}-[0-9a-f]{6,}\\.aadl_diagram");

	private final ReferenceService referenceService;
	private final ExtensionRegistryService extRegistry;

	public static class ContextFunction extends SimpleServiceContextFunction<DiagramService> {
		@Override
		public DiagramService createService(final IEclipseContext context) {
			return new DefaultDiagramService(context.get(ReferenceService.class), context.get(ExtensionRegistryService.class));
		}

		@Override
		protected void deactivate() {
			// Dispose the service if it is valid
			final DiagramService service = getService();
			if(service instanceof DefaultDiagramService) {
				((DefaultDiagramService)service).dispose();
			}

			super.deactivate();
		}
	}

	// Implementation of DiagramReference
	private static class InternalDiagramReference implements DiagramReference {
		private final IFile fileResource;
		private final AgeDiagramEditor editor;

		private InternalDiagramReference(final IFile file,
				final AgeDiagramEditor editor) {
			this.fileResource = Objects.requireNonNull(file, "file must not be null");
			this.editor = editor;
		}

		public IFile getFile() {
			return fileResource;
		}

		@Override
		public AgeDiagramEditor getEditor() {
			return editor;
		}

		@Override
		public boolean isOpen() {
			return editor != null;
		}
	}

	private BusinessObjectProviderHelper bopHelper;
	private SavedDiagramIndex savedDiagramIndex;
	private SavedDiagramIndexInvalidator indexUpdater;

	public DefaultDiagramService(final ReferenceService referenceBuilder,
			final ExtensionRegistryService extRegistry) {
		this.referenceService = Objects.requireNonNull(referenceBuilder, "referenceBuilder must not be null");
		this.extRegistry = Objects.requireNonNull(extRegistry, "extRegistry must not be null");

		this.bopHelper = new BusinessObjectProviderHelper(extRegistry);
		this.savedDiagramIndex = new SavedDiagramIndex(referenceBuilder, bopHelper);
		this.indexUpdater = new SavedDiagramIndexInvalidator(savedDiagramIndex);

		// Register the index updater
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(indexUpdater);
	}

	public void dispose() {
		bopHelper.close();

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(indexUpdater);
	}

	@Override
	public List<InternalDiagramReference> findDiagramsByContextBusinessObject(final Object bo) {
		final CanonicalBusinessObjectReference boReference = referenceService.getCanonicalReference(bo);
		if(boReference == null) {
			throw new RuntimeException("Unable to get canonical reference for business object : " + bo);
		}

		final IProject project = getProject(bo);
		if(project == null) {
			throw new RuntimeException("Unable to get project for business object: " + bo);
		}

		// Build a set containing the project containing the business object and all projects which reference that project.
		final HashSet<IProject> relevantProjects = new HashSet<>();
		relevantProjects.add(project);
		for(final IProject referencingProject : project.getReferencingProjects()) {
			if(referencingProject.isAccessible()) {
				relevantProjects.add(referencingProject);
			}
		}

		final Map<IFile, AgeDiagramEditor> fileToEditorMap = getOpenEditorsMap(relevantProjects);

		// Add saved diagram files if they are not open
		return savedDiagramIndex.getDiagramsByContext(relevantProjects.stream(), boReference).stream()
				.map(e -> new InternalDiagramReference(e.diagramFile, fileToEditorMap.get(e.diagramFile)))
				.
				collect(Collectors.toList());
	}

	@Override
	public AgeDiagramEditor openOrCreateDiagramForBusinessObject(final Object bo, final boolean promptForCreate, final boolean promptForConfigureAfterCreate) {
		// Look for an existing diagram
		final List<InternalDiagramReference> diagramRefs = findDiagramsByContextBusinessObject(bo);

		// If there is just one diagram, open it
		if(diagramRefs.size() == 1) {
			final InternalDiagramReference diagramRef = diagramRefs.get(0);
			if(diagramRef.isOpen()) {
				Log.info("Existing diagram found. Activating existing editor...");
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(diagramRef.getEditor());
				return diagramRef.getEditor();
			} else {
				Log.info("Existing diagram found. Opening new editor...");
				return EditorUtil.openEditor(diagramRef.getFile(), false);
			}
		} else if(diagramRefs.size() == 0) {
			// Prompt user to determine whether a new diagram should be created
			if(!promptForCreate || MessageDialog.openQuestion(null, "Create New Diagram?", "An existing diagram was not found for the specified model element.\nCreate new diagram?")) {
				// Create and open a new diagram
				final IFile diagramFile = createDiagram(bo);
				final AgeDiagramEditor editor = EditorUtil.openEditor(diagramFile, promptForConfigureAfterCreate);
				return editor;
			} else {
				return null;
			}
		} else {
			final InternalDiagramReference diagramRef = promptForDiagram(diagramRefs);
			return diagramRef == null ? null : EditorUtil.openEditor(diagramRef.getFile(), false);
		}
	}

	private InternalDiagramReference promptForDiagram(final List<InternalDiagramReference> diagramRefs) {
		// Sort the diagram references
		final InternalDiagramReference[] sortedDiagramRefs = diagramRefs.stream().sorted((r1, r2) -> getName(r1.getFile()).compareToIgnoreCase(getName(r2.getFile()))).toArray(InternalDiagramReference[]::new);

		// Prompt user to select a single dialog reference
		final ListDialog dialog = new ListDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.setAddCancelButton(true);
		dialog.setTitle("Select Diagram");
		dialog.setMessage("Select a Diagram to Open");
		dialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if(element instanceof InternalDiagramReference) {
					final InternalDiagramReference diagramRef = (InternalDiagramReference)element;
					return getName(diagramRef.getFile());
				}

				return super.getText(element);
			}
		});
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setInput(sortedDiagramRefs);
		dialog.setInitialElementSelections(Collections.singletonList(sortedDiagramRefs[0]));
		dialog.open();

		final Object[] result = dialog.getResult();
		return (result != null && result.length > 0) ? (InternalDiagramReference)result[0] : null;
	}

	/**
	 * Gets a new IFile for a new diagram.
	 * @param resourceSet the resource set that will contain the new resource.
	 * @param baseFilename the desired filename of the file that will store the resource. The method will adjust the filename to avoid returning an existing file resource
	 * @return the file resource for the new diagram. The file resource will be one which does not exist.
	 */
	private IFile getNewDiagramFile(final IProject project, final String baseFilename) {
		int nameCount = 1;
		IFile diagramFile;
		do
		{
			final IFolder diagramFolder = project.getFolder("diagrams/");
			final String suffix = nameCount == 1 ? "" : "(" + nameCount + ")";
			diagramFile = diagramFolder.getFile(baseFilename + suffix + AgeDiagramEditor.EXTENSION);
			nameCount++;
		} while(diagramFile.exists());

		return diagramFile;
	}

	@Override
	public IFile createDiagram(final Object contextBo) {
		// Create an AgeDiagram object. This object doesn't have to be completely valid. It just needs to be able to be written.
		final AgeDiagram diagram = new AgeDiagram(0);

		// Build diagram configuration
		final CanonicalBusinessObjectReference contextBoCanonicalRef = Objects.requireNonNull(referenceService.getCanonicalReference(contextBo), "Unable to build canonical reference for business object: " + contextBo);
		diagram.modify("Configure Diagram", m -> {
			m.setDiagramConfiguration(
					new DiagramConfigurationBuilder(new CustomDiagramType(), true)
					.setContextBoReference(contextBoCanonicalRef).build());
		});

		// Create a root diagram element for the context which will be set to manual.
		// This has the benefit that the root element will be checked when the user configures the diagram.
		final RelativeBusinessObjectReference contextBoRelRef = Objects.requireNonNull(referenceService.getRelativeReference(contextBo), "Unable to build relative reference for business object: " + contextBo);
		diagram.modify("Set Context as Manual", m -> {
			final DiagramElement contextElement = new DiagramElement(diagram, contextBo, null, contextBoRelRef);
			m.setManual(contextElement, true);
			m.addElement(contextElement);
		});

		final IProject project = Objects.requireNonNull(getProject(contextBo), "Unable to get project for business object: " + contextBo);

		// Determine the filename to use for the new diagram
		final String baseDiagramName = contextBo instanceof NamedElement ? ((NamedElement)contextBo).getQualifiedName().replaceAll("::|:|\\.", "_") : "untitled_diagram";
		final IFile newDiagramFile = getNewDiagramFile(project, baseDiagramName);

		final URI newDiagramUri = URI.createPlatformResourceURI(newDiagramFile.getFullPath().toString(), true);
		DiagramSerialization.write(diagram, newDiagramUri);

		try {
			newDiagramFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (final CoreException e) {
			throw new RuntimeException(e);
		}

		return newDiagramFile;
	}

	/**
	 * Returns all diagrams in the specified projects.
	 * @return
	 */
	@Override
	public List<InternalDiagramReference> findDiagrams(final Set<IProject> projects) {
		final Map<IFile, AgeDiagramEditor> fileToEditorMap = getOpenEditorsMap(projects);

		// Add saved diagram files if they are not open
		return savedDiagramIndex.getDiagramsByProject(projects.stream()).
				stream().map(e -> new InternalDiagramReference(e.diagramFile, fileToEditorMap.get(e.diagramFile)))
				.
				collect(Collectors.toList());
	}

	private static Map<IFile, AgeDiagramEditor> getOpenEditorsMap(final Collection<IProject> projects) {
		final Map<IFile, AgeDiagramEditor> fileToEditorMap = new HashMap<>();
		for(final IEditorReference editorRef : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()) {
			final IEditorPart editor = editorRef.getEditor(false); // If restore is true then the editor is automatically updated
			if(editor instanceof AgeDiagramEditor) {
				final AgeDiagramEditor diagramEditor = (AgeDiagramEditor)editor;
				final AgeDiagramBehavior behavior = (AgeDiagramBehavior)diagramEditor.getDiagramBehavior();

				if(behavior != null) {
					if(projects.contains(behavior.getProject())) {
						final IFile file = behavior.getFile();
						if(file != null) {
							fileToEditorMap.put(file, diagramEditor);
						}
					}
				}
			}
		}

		return fileToEditorMap;
	}

	@Override
	public String getName(final IFile diagramFile) {
		String name = null;
		if(diagramFile.exists()) {
			// Handle legacy diagram files which have names based on UUIDs
			if(uuidFilenamePattern.matcher(diagramFile.getName()).matches()) {
				// Attempt to use the name from the persistent property
				try {
					// Check modification time stamp
					final String modStampPropValue = diagramFile.getPersistentProperty(LEGACY_PROPERTY_NAME_MODIFICATION_TIMESTAMP);
					if(modStampPropValue != null && modStampPropValue.equals(Long.toString(diagramFile.getModificationStamp()))) {
						name = diagramFile.getPersistentProperty(LEGACY_PROPERTY_DIAGRAM_NAME);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

				if(name == null) {
					final ResourceSet resourceSet = new ResourceSetImpl();
					// Load the EMF Resource
					final URI uri = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
					try {
						final Resource resource = resourceSet.getResource(uri, true);
						if(resource.getContents().size() > 0 && resource.getContents().get(0) instanceof Diagram) {
							final Diagram diagram = (Diagram)resource.getContents().get(0);
							name = diagram.getName() + " (Legacy)";
						}
					} catch (final RuntimeException e) {
						// Ignore. Print to stderr
						e.printStackTrace();
					}

					// Use the diagram file's name if the name could not be determined for any reason
					if(name == null) {
						name = diagramFile.getName();
					}

					// Update persistent properties.
					try {
						diagramFile.setPersistentProperty(LEGACY_PROPERTY_DIAGRAM_NAME, name);
						diagramFile.setPersistentProperty(LEGACY_PROPERTY_NAME_MODIFICATION_TIMESTAMP, Long.toString(diagramFile.getModificationStamp()));
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}

			// Default to the diagram's filename
			if(name == null) {
				name = diagramFile.getName();
			}
		}

		return name;
	}

	@Override
	public void clearLegacyPersistentProperties(final IResource fileResource) {
		// Clear the persistent properties
		try {
			fileResource.setPersistentProperty(LEGACY_PROPERTY_DIAGRAM_NAME, null);
			fileResource.setPersistentProperty(LEGACY_PROPERTY_NAME_MODIFICATION_TIMESTAMP, null);
		} catch (final CoreException e) {
			// Ignore exceptions
		}
	}

	private IProject getProject(Object bo) {
		final Resource resource = getResource(bo);
		if(resource != null) {
			final URI uri = resource.getURI();
			if(uri != null) {
				return SelectionUtil.getProject(uri);
			}
		}

		return null;
	}

	private Resource getResource(Object bo) {
		final EObject eObject;

		// Handle EObject instances without delegating to specialized handlers
		if(bo instanceof EObject) {
			eObject = (EObject)bo;
		} else if(bo instanceof EmfContainerProvider) { // Use the EMF Object container if the business object is not an EMF Object
			final EObject container = ((EmfContainerProvider)bo).getEmfContainer();
			if(container == null) {
				return null;
			}

			eObject = container;
		} else {
			return null;
		}

		return eObject.eResource();
	}

	class InternalReferencesToUpdate implements ReferenceCollection {
		// Mapping from internal diagram references to a mapping from original diagram reference to lists of references to update
		// Key should be an IFile or and AgeDiagramEditor
		private final Map<Object, Map<CanonicalBusinessObjectReference, Collection<UpdateableReference>>> sourceToCanonicalReferenceToReferencesMap = new HashMap<>();

		@Override
		public void update(final UpdatedReferenceValueProvider updatedReferenceValues) {
			Display.getDefault().syncExec(() -> {
				for (final Entry<Object, Map<CanonicalBusinessObjectReference, Collection<UpdateableReference>>> sourceToCanonicalReferenceToReferencesEntry : sourceToCanonicalReferenceToReferencesMap
						.entrySet()) {
					final Object key = sourceToCanonicalReferenceToReferencesEntry.getKey();
					final Map<CanonicalBusinessObjectReference, Collection<UpdateableReference>> originalCanonicalRefToReferenceMap = sourceToCanonicalReferenceToReferencesEntry
							.getValue();

					if(key instanceof AgeDiagramEditor) {
						final AgeDiagramEditor editor = (AgeDiagramEditor)key;

						final AgeDiagramProvider diagramProvider = (AgeDiagramProvider)editor.getAdapter(AgeDiagramProvider.class);
						if(diagramProvider == null) {
							continue;
						}

						final AgeDiagram diagram = diagramProvider.getAgeDiagram();
						if(diagram == null) {
							continue;
						}

						// NonUndoableToolCommand
						editor.getEditingDomain().getCommandStack().execute(new NonUndoableToolCommand() {
							@Override
							public void execute() {
								diagram.modify("Update References", m -> updateReferences(updatedReferenceValues,
										originalCanonicalRefToReferenceMap, null, m));
							}
						});

						// Ensure that the editor is updated on the next model change
						editor.forceDiagramUpdateOnNextModelChange();
					} else if (key instanceof IFile) {
						final IFile diagramFile = (IFile) key;

						// Handle closed diagrams
						final ResourceSet rs = new ResourceSetImpl();
						final URI diagramUri = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
						final Resource diagramResource = rs.createResource(diagramUri);
						try {
							diagramResource.load(Collections.emptyMap());
							if (diagramResource.getContents().size() == 1
									&& diagramResource.getContents().get(0) instanceof org.osate.ge.diagram.Diagram) {
								updateReferences(updatedReferenceValues, originalCanonicalRefToReferenceMap,
										diagramResource, null);
							}
						} catch (IOException e) {
							// Ignore. Continue with next file
						} finally {
							// Save and unload the resource if it was loaded
							if (diagramResource.isLoaded()) {
								try {
									diagramResource.save(Collections.emptyMap());
								} catch (final IOException e) {
									// Ignore. Print stack trace so it will show in the console during development.
									e.printStackTrace();
								}
								diagramResource.unload();
							}
						}
					} else {
						throw new RuntimeException("Unexpected key: " + key);
					}
				}
			});
		}

		private void updateReferences(final UpdatedReferenceValueProvider newBoReferences,
				final Map<CanonicalBusinessObjectReference, Collection<UpdateableReference>> originalCanonicalRefToReferenceMap,
				final Resource diagramResource,
				final DiagramModification diagramModification) {
			try {
				referenceUpdateResource = diagramResource;
				referenceUpdateModification = diagramModification;
				for(final Entry<CanonicalBusinessObjectReference, Collection<UpdateableReference>> originalCanonicalRefToReferencesToUpdateEntry : originalCanonicalRefToReferenceMap.entrySet()) {
					final CanonicalBusinessObjectReference originalCanonicalReference = originalCanonicalRefToReferencesToUpdateEntry.getKey();
					final CanonicalBusinessObjectReference newCanonicalReference = newBoReferences.getNewCanonicalReference(originalCanonicalReference);
					final RelativeBusinessObjectReference newRelativeReference = newBoReferences.getNewRelativeReference(originalCanonicalReference);
					if(newCanonicalReference != null && newRelativeReference != null) {
						for(final UpdateableReference referenceToUpdate : originalCanonicalRefToReferencesToUpdateEntry.getValue()) {
							referenceToUpdate.update(newCanonicalReference, newRelativeReference);
						}
					}
				}
			} finally {
				referenceUpdateResource = null;
				referenceUpdateModification = null;
			}
		}

		public void addReference(
				final Object source,
				final CanonicalBusinessObjectReference originalCanonicalReference,
				final UpdateableReference reference) {
			if(!(source instanceof AgeDiagramEditor || source instanceof IFile)) {
				throw new RuntimeException("source must be a diagram editor or a file");
			}

			// Add Reference to the collection
			Map<CanonicalBusinessObjectReference, Collection<UpdateableReference>> canonicalReferenceToUpdateableReferenceMap = sourceToCanonicalReferenceToReferencesMap.get(source);
			if(canonicalReferenceToUpdateableReferenceMap == null) {
				canonicalReferenceToUpdateableReferenceMap = new HashMap<>();
				sourceToCanonicalReferenceToReferencesMap.put(source, canonicalReferenceToUpdateableReferenceMap);
			}

			Collection<UpdateableReference> updateableReferences = canonicalReferenceToUpdateableReferenceMap.get(originalCanonicalReference);
			if(updateableReferences == null) {
				updateableReferences = new ArrayList<>();
				canonicalReferenceToUpdateableReferenceMap.put(originalCanonicalReference, updateableReferences);
			}

			updateableReferences.add(reference);
		}

		public Collection<Entry<CanonicalBusinessObjectReference, Collection<UpdateableReference>>> getReferences(final InternalDiagramReference ref) {
			final Map<CanonicalBusinessObjectReference, Collection<UpdateableReference>> canonicalReferenceToUpdateableReferenceMap = sourceToCanonicalReferenceToReferencesMap
					.get(ref);
			if(canonicalReferenceToUpdateableReferenceMap == null) {
				return Collections.emptyList();
			}

			return canonicalReferenceToUpdateableReferenceMap.entrySet();
		}
	}

	// Variables used during the update process
	private Resource referenceUpdateResource;
	private DiagramModification referenceUpdateModification;

	interface UpdateableReference {
		void update(CanonicalBusinessObjectReference newCanonicalReference, RelativeBusinessObjectReference newRelativeReference);
	}

	//
	// Updateable Reference Implementations
	//

	// Reference to the context field in an open diagram's configuration
	class OpenDiagramContextReference implements UpdateableReference {
		private final AgeDiagram diagram;

		public OpenDiagramContextReference(final AgeDiagram diagram) {
			this.diagram = Objects.requireNonNull(diagram, "diagram must not be null");
		}

		@Override
		public void update(CanonicalBusinessObjectReference newCanonicalReference, RelativeBusinessObjectReference newRelativeReference) {
			diagram.modify("Configure Diagram", m -> {
				m.setDiagramConfiguration(new DiagramConfigurationBuilder(diagram.getConfiguration())
						.setContextBoReference(newCanonicalReference).build());
			});
		}
	}

	// Reference to the reference of an open diagram element
	class OpenDiagramElementReference implements UpdateableReference {
		private final DiagramElement diagramElement;

		public OpenDiagramElementReference(final DiagramElement diagramElement) {
			this.diagramElement = Objects.requireNonNull(diagramElement, "diagramElement must not be null");
		}

		@Override
		public void update(final CanonicalBusinessObjectReference newCanonicalReference, final RelativeBusinessObjectReference newRelativeReference) {
			// The diagram element's business object is not updated because it is not available at this point.
			// In the case of a rename refactoring, the object would be available during the rename but not during undo.
			referenceUpdateModification.updateBusinessObject(diagramElement, diagramElement.getBusinessObject(), newRelativeReference);
		}
	}

	// Reference to the context field in an saved diagram configuration
	class SavedDiagramContextReference implements UpdateableReference {
		@Override
		public void update(final CanonicalBusinessObjectReference newCanonicalReference, final RelativeBusinessObjectReference newRelativeReference) {
			final org.osate.ge.diagram.Diagram mmDiagram = (org.osate.ge.diagram.Diagram)referenceUpdateResource.getContents().get(0);

			// Get the Context Business Object
			final org.osate.ge.diagram.DiagramConfiguration config = mmDiagram.getConfig();
			if(config != null) {
				config.setContext(newCanonicalReference.toMetamodel());
			}
		}
	}

	// Reference to the context field in an saved diagram element
	class SavedDiagramElementReference implements UpdateableReference {
		private final URI diagramElementUri;

		public SavedDiagramElementReference(final URI diagramElementUri) {
			this.diagramElementUri = Objects.requireNonNull(diagramElementUri, "diagramElementUri must not be null");
		}

		@Override
		public void update(final CanonicalBusinessObjectReference newCanonicalReference, final RelativeBusinessObjectReference newRelativeReference) {
			final org.osate.ge.diagram.RelativeBusinessObjectReference mmRelRef = newRelativeReference.toMetamodel();
			if(mmRelRef != null) {
				final org.osate.ge.diagram.DiagramElement diagramElement = (org.osate.ge.diagram.DiagramElement)referenceUpdateResource.getEObject(diagramElementUri.fragment());
				if(diagramElement == null) {
					throw new RuntimeException("Unable to retrieve diagram element");
				}

				if(diagramElement.eIsProxy()) {
					throw new RuntimeException("Retrieved diagram element is proxy");
				}

				diagramElement.setBo(mmRelRef);
			}
		}
	}

	@Override
	public ReferenceCollection getReferences(final Set<IProject> relevantProjects,
			final Set<CanonicalBusinessObjectReference> originalCanonicalReferences) {
		final InternalReferencesToUpdate references = new InternalReferencesToUpdate();
		Display.getDefault().syncExec(() -> {
			try (final BusinessObjectProviderHelper bopHelper = new BusinessObjectProviderHelper(extRegistry)) {
				// Create updateable reference for open diagrams
				for (final AgeDiagramEditor editor : getOpenEditorsMap(relevantProjects).values()) {
					final AgeDiagramProvider diagramProvider = (AgeDiagramProvider) editor
							.getAdapter(AgeDiagramProvider.class);
					if (diagramProvider == null) {
						continue;
					}

					final AgeDiagram diagram = diagramProvider.getAgeDiagram();
					if (diagram == null) {
						continue;
					}

					// Update the diagram immediately. This is intended to ensure the diagram doesn't have any proxies
					editor.updateNowIfModelHasChanged();

					final CanonicalBusinessObjectReference diagramContextRef = diagram.getConfiguration()
							.getContextBoReference();
					if (diagramContextRef != null) {
						if (originalCanonicalReferences.contains(diagramContextRef)) {
							references.addReference(editor, diagramContextRef, new OpenDiagramContextReference(diagram));
						}
					}

					// Get references from the diagram elements
					getRuntimeReferencesFromChildren(editor, diagram, originalCanonicalReferences, references);
				}

				// Create updateable references for saved diagrams
				savedDiagramIndex.getDiagramsByContexts(relevantProjects.stream(), originalCanonicalReferences).
				forEach(e -> {
					references.addReference(e.diagramFile, e.reference, new SavedDiagramContextReference());
				});

				savedDiagramIndex.getElementUrisByReferences(relevantProjects.stream(), originalCanonicalReferences).
				forEach(e -> {
					references.addReference(e.diagramFile, e.reference, new SavedDiagramElementReference(e.elementUri));
				});
			}
		});
		return references;
	}


	/**
	 * Gets references from open editors.
	 * @param editor
	 * @param node
	 * @param originalCanonicalReferences
	 * @param references
	 */
	private void getRuntimeReferencesFromChildren(final AgeDiagramEditor editor,
			final DiagramNode node,
			final Collection<CanonicalBusinessObjectReference> originalCanonicalReferences,
			final InternalReferencesToUpdate references) {
		for(final DiagramElement child : node.getDiagramElements()) {
			final Object currentBo = child.getBusinessObject();
			final CanonicalBusinessObjectReference currentCanonicalRef = currentBo == null ? null : referenceService.getCanonicalReference(currentBo);
			if(currentCanonicalRef != null) {
				if(originalCanonicalReferences.contains(currentCanonicalRef)) {
					references.addReference(editor, currentCanonicalRef, new OpenDiagramElementReference(child));
				}
			}

			getRuntimeReferencesFromChildren(editor, child, originalCanonicalReferences, references);
		}
	}
}
