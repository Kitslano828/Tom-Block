package org.tomdang.actorframework.presentation;

import org.bukkit.Location;
import org.tomdang.actorframework.instance.ActorInstance;

import java.util.UUID;

public class ActorPresentationService {

	private final ActiveActorPresentationRegistry activeActorPresentationRegistry;
	private final ActorPresentationTypeRegistry actorPresentationTypeRegistry;

	public ActorPresentationService(ActiveActorPresentationRegistry activeActorPresentationRegistry,
									ActorPresentationTypeRegistry actorPresentationTypeRegistry) {
		if (activeActorPresentationRegistry == null) throw new IllegalArgumentException("Active actor presentation registry cannot be null");
		if (actorPresentationTypeRegistry == null) throw new IllegalArgumentException("Actor presentation type registry cannot be null");

		this.activeActorPresentationRegistry = activeActorPresentationRegistry;
		this.actorPresentationTypeRegistry = actorPresentationTypeRegistry;
	}

	public ActorPresentationHandle 	spawnPresentation(ActorInstance instance, Location location) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");

		UUID instanceUUID = instance.getInstanceID();
		if (activeActorPresentationRegistry.hasPresentation(instanceUUID)) throw new IllegalStateException("instance already exists");
		String presentationType = instance.getActorDefinition().getPresentationTypeID();
		if (!actorPresentationTypeRegistry.isRegistered(presentationType)) throw new IllegalStateException(presentationType + " is not registered!");

		ActorPresentation presentation = actorPresentationTypeRegistry.lookupPresentation(presentationType);
		if (presentation == null) throw new IllegalStateException("Presentation was not registered!");

		ActorPresentationHandle presentationHandle = presentation.spawnActorInstance(instance, location);
		if (presentationHandle == null) throw new IllegalStateException("Presentation Handle is null");

		if (!presentationHandle.actorInstanceID().equals(instanceUUID)) throw new IllegalStateException("Actor IDs do not match");
		activeActorPresentationRegistry.registerPresentation(presentationHandle);
		return presentationHandle;
	}

	public ActorPresentationHandle removePresentation(ActorInstance instance) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");

		UUID instanceUUID = instance.getInstanceID();
		if (!activeActorPresentationRegistry.hasPresentation(instanceUUID)) return null;
		String presentationType = instance.getActorDefinition().getPresentationTypeID();
		if (!actorPresentationTypeRegistry.isRegistered(presentationType)) throw new IllegalStateException(presentationType + " is not registered!");

		ActorPresentation presentation = actorPresentationTypeRegistry.lookupPresentation(presentationType);
		if (presentation == null) throw new IllegalStateException("Presentation was not registered!");

		ActorPresentationHandle presentationHandle =
				activeActorPresentationRegistry.getPresentation(instanceUUID);

		presentation.removePresentationHandle(presentationHandle);

		return activeActorPresentationRegistry.removePresentation(instanceUUID);

	}

	public ActorPresentationHandle restorePresentation(ActorInstance instance, UUID presentationID) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (presentationID == null) throw new IllegalArgumentException("Presentation ID cannot be null");

		UUID instanceUUID = instance.getInstanceID();
		if (activeActorPresentationRegistry.hasPresentation(instanceUUID)) throw new IllegalStateException("instance already exists");
		String presentationType = instance.getActorDefinition().getPresentationTypeID();
		if (!actorPresentationTypeRegistry.isRegistered(presentationType)) throw new IllegalStateException(presentationType + " is not registered!");

		ActorPresentation presentation = actorPresentationTypeRegistry.lookupPresentation(presentationType);
		if (presentation == null) throw new IllegalStateException("Presentation shouldn't be null");
		if (!(presentation instanceof RestorableActorPresentation restorablePresentation)) {
			throw new IllegalStateException("Presentation type " + presentationType + " does not support restoration");
		}

		ActorPresentationHandle presentationHandle = restorablePresentation.restoreActorInstance(instance, presentationID);
		if (presentationHandle == null) throw new IllegalStateException("Presentation handle cannot be null");

		if (!presentationHandle.actorInstanceID().equals(instanceUUID)) throw new IllegalStateException("Actor IDs do not match");
		if (!presentationHandle.presentationID().equals(presentationID)) throw new IllegalStateException("Presentation IDs do not match");
		activeActorPresentationRegistry.registerPresentation(presentationHandle);
		return presentationHandle;
	}


}
