package org.tomdang.actorframework.lifecycle;

import org.bukkit.Location;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.instance.ActorInstanceService;
import org.tomdang.actorframework.movement.ActorMovementTaskRegistry;
import org.tomdang.actorframework.presentation.ActorPresentationService;

import java.util.UUID;

public class ActorLifecycleService {

	private final ActorInstanceService actorInstanceService;
	private final ActorPresentationService actorPresentationService;
	private final ActorMovementTaskRegistry actorMovementTaskRegistry;

	public ActorLifecycleService(ActorInstanceService actorInstanceService, ActorPresentationService actorPresentationService, ActorMovementTaskRegistry actorMovementTaskRegistry) {
		if (actorInstanceService == null) throw new IllegalArgumentException("Actor instance service cannot be null");
		if (actorPresentationService == null) throw new IllegalArgumentException("Actor presentation service cannot be null");
		if (actorMovementTaskRegistry == null) throw new IllegalArgumentException("actorMovementTaskRegistry cannot be null");

		this.actorInstanceService = actorInstanceService;
		this.actorPresentationService = actorPresentationService;
		this.actorMovementTaskRegistry = actorMovementTaskRegistry;
	}

	public ActorInstance createAndSpawnActor(String actorID, ActorAudienceKey audienceKey, Location location, String spawnPointID) {
		if (actorID == null) throw new IllegalArgumentException("actor ID cannot be null!");
		if (actorID.isBlank()) throw new IllegalArgumentException("actor ID cannot be blank!");
		ActorInstance instance = actorInstanceService.createInstance(actorID, audienceKey, spawnPointID);
		try {
			actorPresentationService.spawnPresentation(instance, location);
			return instance;
		} catch (RuntimeException exception) {
			actorInstanceService.removeInstance(instance.getInstanceID());
			throw exception;
		}
	}

	public ActorInstance restoreActor(UUID actorInstanceUUID, String actorID, ActorAudienceKey key, String spawnPointID, UUID existingPresentationUUID) {

		ActorInstance instance = actorInstanceService.restoreInstance(actorInstanceUUID, actorID, key, spawnPointID);

		try {
			actorPresentationService.restorePresentation(instance, existingPresentationUUID);
		} catch (RuntimeException e) {
			actorInstanceService.removeInstance(instance.getInstanceID());
			throw e;
		}

		return  instance;
	}

	public ActorInstance removeActor(ActorInstance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");

		UUID instanceID = instance.getInstanceID();
		actorMovementTaskRegistry.cancelMovement(instanceID);

		actorPresentationService.removePresentation(instance);
		return actorInstanceService.removeInstance(instance.getInstanceID());
	}

}
