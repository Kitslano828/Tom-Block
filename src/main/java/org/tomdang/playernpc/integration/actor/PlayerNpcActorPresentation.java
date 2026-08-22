package org.tomdang.playernpc.integration.actor;

import org.bukkit.Location;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.ActorPresentation;
import org.tomdang.actorframework.presentation.ActorPresentationHandle;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.runtime.PlayerNPC;

import java.util.UUID;

public class PlayerNpcActorPresentation implements ActorPresentation {

	private final PlayerNpcLifecycleService playerNpcLifecycleService;

	public PlayerNpcActorPresentation(PlayerNpcLifecycleService playerNpcLifecycleService) {
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("playerNpcLifecycleService cannot be null");

		this.playerNpcLifecycleService = playerNpcLifecycleService;
	}

	@Override
	public ActorPresentationHandle spawnActorInstance(ActorInstance instance, Location location) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");

		PlayerNPC playerNPC = playerNpcLifecycleService.createNpc(location, instance.getActorDefinition().getActorID());
		return new ActorPresentationHandle(playerNPC.getProfileUUID(), instance.getInstanceID());
	}

	@Override
	public void removePresentationHandle(ActorPresentationHandle presentationHandle) {
		if (presentationHandle == null) throw new IllegalArgumentException("presentationHandle cannot be null");
		UUID npcProfileID = presentationHandle.presentationID();
		playerNpcLifecycleService.removeNpc(npcProfileID);
	}
}
