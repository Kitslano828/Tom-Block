package org.tomdang.playernpc.integration.actor;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.audience.ActorAudienceResolver;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentation;
import org.tomdang.actorframework.presentation.ActorPresentation;
import org.tomdang.actorframework.presentation.ActorPresentationHandle;
import org.tomdang.actorframework.presentation.LocatableActorPresentation;
import org.tomdang.actorframework.presentation.MovableActorPresentation;
import org.tomdang.actorframework.presentation.bukkit.BukkitActorCollisionService;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.runtime.PlayerNPC;

import java.util.Collection;
import java.util.UUID;

public class PlayerNpcActorPresentation implements ActorPresentation, MovableActorPresentation, LocatableActorPresentation {

	private final PlayerNpcLifecycleService playerNpcLifecycleService;
	private final ActorAudienceResolver actorAudienceResolver;
	private final BukkitActorCollisionService bukkitActorCollisionService;
	private final ActorNameplatePresentation actorNameplatePresentation;
	private final PlayerNpcProfileNameFactory playerNpcProfileNameFactory;

	public PlayerNpcActorPresentation(PlayerNpcLifecycleService playerNpcLifecycleService, ActorAudienceResolver actorAudienceResolver, BukkitActorCollisionService bukkitActorCollisionService, ActorNameplatePresentation actorNameplatePresentation, PlayerNpcProfileNameFactory playerNpcProfileNameFactory) {
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("playerNpcLifecycleService cannot be null");
		if (actorAudienceResolver == null) throw new IllegalArgumentException("actorAudienceResolver cannot be null");
		if (bukkitActorCollisionService == null) throw new IllegalArgumentException("bukkitActorCollisionService cannot be null");
		if (actorNameplatePresentation == null) throw new IllegalArgumentException("actorNameplatePresentation cannot be null");
		if (playerNpcProfileNameFactory == null) throw new IllegalArgumentException("playerNpcProfileNameFactory cannot be null");

		this.playerNpcLifecycleService = playerNpcLifecycleService;
		this.actorAudienceResolver = actorAudienceResolver;
		this.bukkitActorCollisionService = bukkitActorCollisionService;
		this.actorNameplatePresentation = actorNameplatePresentation;
		this.playerNpcProfileNameFactory = playerNpcProfileNameFactory;
	}

	@Override
	public ActorPresentationHandle spawnActorInstance(ActorInstance instance, Location location) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");

		ActorAudienceKey key = instance.getAudienceKey();
		Collection<Player> players = actorAudienceResolver.resolvePlayers(key);

		String requestedProfileName = playerNpcProfileNameFactory.create(instance.getInstanceID());
		PlayerNPC playerNPC = playerNpcLifecycleService.createNpc(location, requestedProfileName);
		UUID npcUUID = playerNPC.getProfileUUID();
		String profileName = playerNPC.getProfileName();
		ActorCollisionPolicy collisionPolicy = instance.getActorDefinition().getActorCollisionPolicy();

		try {
			bukkitActorCollisionService.applyCollisionPolicyToEntry(profileName, collisionPolicy);
			for (Player player : players) {
				playerNpcLifecycleService.showToViewer(player, npcUUID);
				actorNameplatePresentation.showToViewer(player, instance, location, false);
			}
		} catch (RuntimeException spawnException) {
			try {
				actorNameplatePresentation.removeNameplate(instance.getInstanceID());
			} catch (RuntimeException cleanupException) {
				spawnException.addSuppressed(cleanupException);
			}

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
		actorNameplatePresentation.removeNameplate(presentationHandle.actorInstanceID());
		UUID npcProfileID = presentationHandle.presentationID();
		PlayerNPC removedNpc = playerNpcLifecycleService.removeNpc(npcProfileID);
		if (removedNpc == null) return;
		bukkitActorCollisionService.removeCollisionEntry(removedNpc.getProfileName());
	}

	@Override
	public void movePresentationHandle(ActorInstance instance, ActorPresentationHandle handle, Location location, boolean isMoving) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (handle == null) throw new IllegalArgumentException("Handle cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");
		if (location.getWorld() == null) throw new IllegalArgumentException("world cannot be null");

		UUID presentationID = handle.presentationID();
		playerNpcLifecycleService.moveNpc(presentationID, location);
		actorNameplatePresentation.updateNameplate(instance, location, isMoving);
	}

	@Override
	public Location getPresentationLocation(ActorPresentationHandle handle) {
		if (handle == null) throw new IllegalArgumentException("Handle cannot be null");
		UUID presentationID = handle.presentationID();
		return playerNpcLifecycleService.getNpcLocation(presentationID);
	}
}
