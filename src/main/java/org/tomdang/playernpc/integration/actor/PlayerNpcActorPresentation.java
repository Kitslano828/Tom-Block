package org.tomdang.playernpc.integration.actor;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.audience.ActorAudienceResolver;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.ActorPresentation;
import org.tomdang.actorframework.presentation.ActorPresentationHandle;
import org.tomdang.actorframework.presentation.MovableActorPresentation;
import org.tomdang.actorframework.presentation.bukkit.BukkitActorCollisionService;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.runtime.PlayerNPC;

import java.util.Collection;
import java.util.UUID;

public class PlayerNpcActorPresentation implements ActorPresentation, MovableActorPresentation {

	private final PlayerNpcLifecycleService playerNpcLifecycleService;
	private final ActorAudienceResolver actorAudienceResolver;
	private final BukkitActorCollisionService bukkitActorCollisionService;

	public PlayerNpcActorPresentation(PlayerNpcLifecycleService playerNpcLifecycleService, ActorAudienceResolver actorAudienceResolver, BukkitActorCollisionService bukkitActorCollisionService) {
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("playerNpcLifecycleService cannot be null");
		if (actorAudienceResolver == null) throw new IllegalArgumentException("actorAudienceResolver cannot be null");
		if (bukkitActorCollisionService == null) throw new IllegalArgumentException("bukkitActorCollisionService cannot be null");

		this.playerNpcLifecycleService = playerNpcLifecycleService;
		this.actorAudienceResolver = actorAudienceResolver;
		this.bukkitActorCollisionService = bukkitActorCollisionService;
	}

	@Override
	public ActorPresentationHandle spawnActorInstance(ActorInstance instance, Location location) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");

		ActorAudienceKey key = instance.getAudienceKey();
		Collection<Player> players = actorAudienceResolver.resolvePlayers(key);

		PlayerNPC playerNPC = playerNpcLifecycleService.createNpc(location, instance.getActorDefinition().getDisplayName());
		UUID npcUUID = playerNPC.getProfileUUID();
		String profileName = playerNPC.getProfileName();
		ActorCollisionPolicy collisionPolicy = instance.getActorDefinition().getActorCollisionPolicy();

		try {
			bukkitActorCollisionService.applyCollisionPolicyToEntry(profileName, collisionPolicy);
			for (Player player : players) {
				playerNpcLifecycleService.showToViewer(player, npcUUID);
			}
		} catch (RuntimeException spawnException) {
			try {
				playerNpcLifecycleService.removeNpc(npcUUID);
			} catch (RuntimeException cleanupException) {
				spawnException.addSuppressed(cleanupException);
			}

			try {
				bukkitActorCollisionService.removeCollisionEntry(profileName);
			} catch (RuntimeException cleanupException) {
				spawnException.addSuppressed(cleanupException);
			}

			throw spawnException;
		}

		return new ActorPresentationHandle(npcUUID, instance.getInstanceID());
	}

	@Override
	public void removePresentationHandle(ActorPresentationHandle presentationHandle) {
		if (presentationHandle == null) throw new IllegalArgumentException("presentationHandle cannot be null");
		UUID npcProfileID = presentationHandle.presentationID();
		PlayerNPC removedNpc = playerNpcLifecycleService.removeNpc(npcProfileID);
		if (removedNpc == null) return;
		bukkitActorCollisionService.removeCollisionEntry(removedNpc.getProfileName());
	}

	@Override
	public void movePresentationHandle(ActorPresentationHandle handle, Location location) {
		if (handle == null) throw new IllegalArgumentException("Handle cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");
		if (location.getWorld() == null) throw new IllegalArgumentException("world cannot be null");

		UUID presentationID = handle.presentationID();
		playerNpcLifecycleService.moveNpc(presentationID, location);
	}
}
