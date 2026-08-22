package org.tomdang.playernpc.integration.actor;

import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.presentation.ActiveActorPresentationRegistry;
import org.tomdang.actorframework.presentation.ActorPresentationHandle;
import org.tomdang.playernpc.runtime.PlayerNPC;
import org.tomdang.playernpc.runtime.PlayerNpcRegistry;

import java.util.UUID;

public class PlayerNpcActorResolver {

	private final PlayerNpcRegistry playerNpcRegistry;
	private final ActiveActorPresentationRegistry activeActorPresentationRegistry;
	private final ActorInstanceRegistry actorInstanceRegistry;

	public PlayerNpcActorResolver(PlayerNpcRegistry playerNpcRegistry, ActiveActorPresentationRegistry activeActorPresentationRegistry, ActorInstanceRegistry actorInstanceRegistry) {
		if (playerNpcRegistry == null) throw new IllegalArgumentException("Player NPC registry cannot be null");
		if (activeActorPresentationRegistry == null) throw new IllegalArgumentException("Active actor presentation registry cannot be null");
		if (actorInstanceRegistry == null) throw new IllegalArgumentException("Actor instance registry cannot be null");

		this.playerNpcRegistry = playerNpcRegistry;
		this.activeActorPresentationRegistry = activeActorPresentationRegistry;
		this.actorInstanceRegistry = actorInstanceRegistry;
	}

	public ActorInstance resolveByEntityID(int entityID) {

		PlayerNPC playerNPC = playerNpcRegistry.getByEntityID(entityID);
		if (playerNPC == null) return null;

		UUID playerNPCUUID = playerNPC.getProfileUUID();
		ActorPresentationHandle presentationHandle = activeActorPresentationRegistry.getByPresentationID(playerNPCUUID);
		if (presentationHandle == null) return null;

		UUID instanceID = presentationHandle.actorInstanceID();
		ActorInstance instance = actorInstanceRegistry.getInstance(instanceID);
		if (instance == null) throw new IllegalStateException("A registered presentation handle pointing at a nonexistent actor instance is corrupted framework state");

		return instance;
	}

}
