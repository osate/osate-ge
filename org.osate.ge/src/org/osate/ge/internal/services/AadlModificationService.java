package org.osate.ge.internal.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.collect.LinkedListMultimap;

/**
 * Service providing a mechanism for making changes to the model
 *
 */
public interface AadlModificationService {
	// TODO: Rename
	// TODO: Document that mapper is guaranteed to be called after any locking
	// TODO: SHould this be moved out of this service?
	static class Modification<I, E extends EObject> {
		private final I obj; // TODO: Rename
		private final Function<I, E> objToBoToModifyMapper;
		private final NewModifier<I, E> modifier;

		// TODO: Rename
		// TODO: Anyway to make this API private?
		public I getObject() {
			return obj;
		}

		public Function<I, E> getObjectToBoToModifyMapper() {
			return objToBoToModifyMapper;
		}

		public NewModifier<I, E> getModifier() {
			return modifier;
		}

		// TODO
		private Modification(final I obj, final Function<I, E> objToBoToModifyMapper,
				final NewModifier<I, E> modifier) {
			this.obj = obj;
			this.objToBoToModifyMapper = objToBoToModifyMapper;
			this.modifier = modifier;
		}

		public static <I, E extends EObject> Modification<I, E> create(final I obj,
				final Function<I, E> objToBoToModifyMapper,
				final NewModifier<I, E> modifier) {
			return new Modification<>(obj, objToBoToModifyMapper, modifier);
		}

		public static <E extends EObject> Modification<E, E> create(final E boToModify,
				final NewModifier<E, E> modifier) {
			return new Modification<>(boToModify, bo -> bo, modifier);
		}
	}

	// TODO: Rename interface and argument.
	static interface NewModifier<I, E extends EObject> {
		void modify(final E boToModify, final I obj);
	}

	static interface ModificationPostprocessor {
		void modificationCompleted(boolean allSuccessful);
	}

	// TODO: Need to notify caller if modification was successful
	// TODO: Need a function to be called after it is finished
	// TODO: Rename arguments
	// Postprocessor argument is whether all modification were successful.
	// TODO: Consider an interface for that
	<I, E extends EObject> void modify(List<Modification<I, E>> modifierArgs,
			ModificationPostprocessor postProcessor);

	// TODO: Remove all other modify functions

	/**
	 * Calls the specified modifier for each business object provided by applying objToBoToModifyMapper for each object object in the specified object list.
	 * @param modifier
	 * @param objToBoToModifyMapper
	 */
	default <I, E extends EObject, R> List<R> modify(List<I> objs, Function<I, E> objToBoToModifyMapper,
			MappedObjectModifier<E, R> modifier) {
		Objects.requireNonNull(objs, "objs must not be null");
		Objects.requireNonNull(objToBoToModifyMapper, "objToBoToModifyMapper must not be null");
		Objects.requireNonNull(modifier, "modifier must not be null");

		// Create a map containing a mapping from each specified object to the specified modifier so that a common modify() implementation can be used.
		final LinkedListMultimap<I, MappedObjectModifier<E, R>> objectsToModifierMap = LinkedListMultimap.create(objs.size());
		for (final I obj : objs) {
			objectsToModifierMap.put(obj, modifier);
		}

		return modify(objectsToModifierMap, objToBoToModifyMapper, results -> {
		});
	}

	/**
	 * For each key in the specified map, calls the value modifier for each business object provided by applying objToBoToModifyMapper.
	 * @param objectsToModifierMap
	 * @param objToBoToModifyMapper
	 * @param resultConsumer is a consumer which is called with the results before the modification is completed and the change notifier is unlocked.
	 */
	default <I, E extends EObject, R> List<R> modify(
			LinkedListMultimap<I, MappedObjectModifier<E, R>> objectsToModifierMap,
			Function<I, E> objToBoToModifyMapper, final Consumer<List<R>> resultConsumer) {
		// TODO
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	// TODO: Remove

	/**
	 * Modifies an AADL model. Performs any necessary work to ensure it is done safely and appropriately regardless of the current state.
	 * The modification is considered to have failed if the model that results from the modification contains validation errors.
	 * @param element a named element that is contained in the model to be modified
	 * @param modifier the modifier that will perform the actual modification
	 * @returns the result of the modification or null if the modification failed
	 */
	default <E extends EObject, R> R modify(E bo, Modifier<E, R> modifier) {
		if (bo == null) {
			return null;
		}

		return modify(Collections.singletonList(bo), Function.identity(),
				(Resource resource, E boToModify, Object obj) -> modifier.modify(resource, boToModify)).get(0);
	}

	public static interface Modifier<E, R> {
		R modify(Resource resource, final E bo);
	}

	/**
	 * Version of Modifier which provides the object that was mapped to be business object.
	 */
	public static interface MappedObjectModifier<E, R> {
		R modify(Resource resource, final E bo, final Object obj);
	}
}
