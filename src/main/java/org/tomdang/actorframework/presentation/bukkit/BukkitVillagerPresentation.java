package org.tomdang.actorframework.presentation.bukkit;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.*;

import java.util.UUID;

public class BukkitVillagerPresentation implements RestorableActorPresentation, MovableActorPresentation, LocatableActorPresentation {

	private final NamespacedKey actorInstanceIDKey;
	private final NamespacedKey actorDefinitionIDKey;
	private final NamespacedKey audienceScopeKey;
	private final NamespacedKey audienceIDKey;
	private final BukkitActorCollisionService bukkitActorCollisionService;
	private final NamespacedKey actorSpawnPointIDKey;


	public BukkitVillagerPresentation(NamespacedKey actorInstanceIDKey, NamespacedKey actorDefinitionIDKey,
									  NamespacedKey audienceScopeKey, NamespacedKey audienceIDKey,
									  BukkitActorCollisionService bukkitActorCollisionService, NamespacedKey actorSpawnPointIDKey
	) {
		if (actorInstanceIDKey == null) throw new IllegalArgumentException("Actor Instance Key cannot be null");
		if (actorDefinitionIDKey == null) throw new IllegalArgumentException("Actor Definition ID Key cannot be null");
		if (bukkitActorCollisionService == null) throw new IllegalArgumentException("Bukkit actor collision service cannot be null");
		if (audienceScopeKey == null) throw new IllegalArgumentException("audience scope key cannot be null");
		if (audienceIDKey == null) throw new IllegalArgumentException("audience id key cannot be null");
		if (actorSpawnPointIDKey == null) throw new IllegalArgumentException("actor spawn point ID key cannot be null");

		this.actorInstanceIDKey = actorInstanceIDKey;
		this.actorDefinitionIDKey = actorDefinitionIDKey;
		this.audienceScopeKey = audienceScopeKey;
		this.audienceIDKey = audienceIDKey;
		this.bukkitActorCollisionService = bukkitActorCollisionService;
		this.actorSpawnPointIDKey = actorSpawnPointIDKey;
	}

	@Override
	public ActorPresentationHandle spawnActorInstance(ActorInstance instance, Location location) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");

		World world = location.getWorld();
		if (world == null) throw new IllegalStateException("World cannot be null");

		Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
		configureVillager(villager, instance);


		return new ActorPresentationHandle(villager.getUniqueId(), instance.getInstanceID());
	}

	@Override
	public void removePresentationHandle(ActorPresentationHandle presentationHandle) {
		if (presentationHandle == null) throw new IllegalArgumentException("presentation handle cannot be null");

		UUID entityUUID = presentationHandle.presentationID();

		Entity entity = Bukkit.getEntity(entityUUID);

		// 3. SAFE RETURN: If the entity itself no longer exists in the world, return normally.
		if (entity == null) return;

		// 4. CRITICAL THROW: The UUID exists but belongs to a completely wrong entity type.
		if (!(entity instanceof Villager)) {
			throw new IllegalStateException("Entity target match failure: Tracked UUID belongs to a "
					+ entity.getType().name() + " instead of a VILLAGER.");
		}

		// 5. Read the stored string using the actor-instance key
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		String storedInstanceID = pdc.get(actorInstanceIDKey, PersistentDataType.STRING);
		String storedDefinitionID = pdc.get(actorDefinitionIDKey, PersistentDataType.STRING);


		// 6. DANGEROUS CONDITION CRASH: Entity exists but is completely missing its tracking tag.
		if (storedInstanceID == null) {
			throw new IllegalStateException("Corrupt Entity State: The Villager entity exists at "
					+ entity.getLocation().toVector() + " but is completely missing its actor-instance tag!");
		}

		if (storedDefinitionID == null) {
			throw new IllegalStateException("Corrupt Entity State: The Villager entity exists at "
					+ entity.getLocation().toVector() + " but is completely missing its actor-definition tag!");
		}

		String handleInstanceID = presentationHandle.actorInstanceID().toString();
		if (!storedInstanceID.equalsIgnoreCase(handleInstanceID)) {
			throw new IllegalStateException("Security Abort: Presentation handle instance ID (" + handleInstanceID
					+ ") does not match the persistent data token found on the entity (" + storedInstanceID + ")!");
		}
		bukkitActorCollisionService.removeCollisionState(entity);

		entity.remove();
	}

	@Override
	public ActorPresentationHandle restoreActorInstance(ActorInstance instance, UUID presentationEntityID) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (presentationEntityID == null) throw new IllegalArgumentException("Entity Presentation ID cannot be null");

		Entity entity = Bukkit.getEntity(presentationEntityID);
		if (entity == null || !entity.isValid()) {
			throw new IllegalStateException("Entity with ID " + presentationEntityID + " no longer exists or is invalid");
		}

		if (!(entity instanceof Villager villager)) {
			throw new IllegalArgumentException("Entity with ID " + presentationEntityID + " is not a Villager");
		}

		PersistentDataContainer entityPDC = villager.getPersistentDataContainer();

		String storedInstanceIDStr = entityPDC.get(actorInstanceIDKey, PersistentDataType.STRING);
		String storedDefinitionIDStr = entityPDC.get(actorDefinitionIDKey, PersistentDataType.STRING);

		// Validate that the keys actually exist in the PDC
		if (storedInstanceIDStr == null || storedInstanceIDStr.isBlank()) {
			throw new IllegalStateException("Entity is missing a stored instance ID");
		}
		if (storedDefinitionIDStr == null || storedDefinitionIDStr.isBlank()) {
			throw new IllegalStateException("Entity is missing a stored definition ID");
		}

		// 4. Confirm the stored instance ID matches the supplied ActorInstance
		String suppliedInstanceID = instance.getInstanceID().toString();
		if (!storedInstanceIDStr.equalsIgnoreCase(suppliedInstanceID)) {
			throw new IllegalStateException("Stored instance ID (" + storedInstanceIDStr
					+ ") does not match supplied ActorInstance ID (" + suppliedInstanceID + ")");
		}

		// 5. Confirm the stored definition ID matches the instance’s definition
		String suppliedDefinitionID = instance.getActorDefinition().getActorID();
		if (!storedDefinitionIDStr.equals(suppliedDefinitionID)) {
			throw new IllegalStateException("Stored definition ID (" + storedDefinitionIDStr
					+ ") does not match instance definition ID (" + suppliedDefinitionID + ")");
		}

		configureVillager(villager, instance);

		return new ActorPresentationHandle(villager.getUniqueId(), instance.getInstanceID());

	}

	@Override
	public void movePresentationHandle(ActorPresentationHandle handle, Location location) {
		if (handle == null) throw new IllegalArgumentException("Handle cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");
		if (location.getWorld() == null) throw new IllegalArgumentException("world cannot be null");

		UUID presentationEntityID = handle.presentationID();
		Entity entity = Bukkit.getEntity(presentationEntityID);

		if (entity == null || !entity.isValid()) {
			throw new IllegalStateException("Entity with ID " + presentationEntityID + " no longer exists or is invalid");
		}

		if (!(entity instanceof Villager villager)) {
			throw new IllegalArgumentException("Entity with ID " + presentationEntityID + " is not a Villager");
		}

		// 5. Read the stored string using the actor-instance key
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		String storedInstanceID = pdc.get(actorInstanceIDKey, PersistentDataType.STRING);
		String storedDefinitionID = pdc.get(actorDefinitionIDKey, PersistentDataType.STRING);


		// 6. DANGEROUS CONDITION CRASH: Entity exists but is completely missing its tracking tag.
		if (storedInstanceID == null) {
			throw new IllegalStateException("Corrupt Entity State: The Villager entity exists at "
					+ entity.getLocation().toVector() + " but is completely missing its actor-instance tag!");
		}

		if (storedDefinitionID == null) {
			throw new IllegalStateException("Corrupt Entity State: The Villager entity exists at "
					+ entity.getLocation().toVector() + " but is completely missing its actor-definition tag!");
		}

		String handleInstanceID = handle.actorInstanceID().toString();
		if (!storedInstanceID.equalsIgnoreCase(handleInstanceID)) {
			throw new IllegalStateException("Security Abort: Presentation handle instance ID (" + handleInstanceID
					+ ") does not match the persistent data token found on the entity (" + storedInstanceID + ")!");
		}

		if (!entity.teleport(location)) throw new IllegalStateException("Entity was not teleported");
	}

	@Override
	public Location getPresentationLocation(ActorPresentationHandle handle) {
		if (handle == null) throw new IllegalArgumentException("Handle should not be null");

		UUID presentationID = handle.presentationID();
		Entity entity = Bukkit.getEntity(presentationID);

		if (entity == null || !entity.isValid()) {
			throw new IllegalStateException("Entity with ID " + presentationID + " no longer exists or is invalid");
		}

		if (!(entity instanceof Villager villager)) {
			throw new IllegalArgumentException("Entity with ID " + presentationID + " is not a Villager");
		}

		// 5. Read the stored string using the actor-instance key
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		String storedInstanceID = pdc.get(actorInstanceIDKey, PersistentDataType.STRING);
		String storedDefinitionID = pdc.get(actorDefinitionIDKey, PersistentDataType.STRING);


		// 6. DANGEROUS CONDITION CRASH: Entity exists but is completely missing its tracking tag.
		if (storedInstanceID == null) {
			throw new IllegalStateException("Corrupt Entity State: The Villager entity exists at "
					+ entity.getLocation().toVector() + " but is completely missing its actor-instance tag!");
		}

		if (storedDefinitionID == null) {
			throw new IllegalStateException("Corrupt Entity State: The Villager entity exists at "
					+ entity.getLocation().toVector() + " but is completely missing its actor-definition tag!");
		}

		String handleInstanceID = handle.actorInstanceID().toString();
		if (!storedInstanceID.equalsIgnoreCase(handleInstanceID)) {
			throw new IllegalStateException("Security Abort: Presentation handle instance ID (" + handleInstanceID
					+ ") does not match the persistent data token found on the entity (" + storedInstanceID + ")!");
		}

		return entity.getLocation();
	}

	private void configureVillager(Villager villager, ActorInstance instance) {
		villager.setAI(false);
		villager.setInvulnerable(instance.getActorDefinition().getDamagePolicy() == ActorDamagePolicy.PROTECTED);
		villager.setSilent(true);
		villager.setRemoveWhenFarAway(false);
		villager.setPersistent(true);

		bukkitActorCollisionService.applyCollisionPolicy(villager, instance.getActorDefinition().getActorCollisionPolicy());

		villager.customName(Component.text(instance.getActorDefinition().getDisplayName()));
		villager.setCustomNameVisible(true);
		PersistentDataContainer villagerPDC = villager.getPersistentDataContainer();

		if (instance.getSpawnPointID() == null) {
			villagerPDC.remove(actorSpawnPointIDKey);
		} else {
			villagerPDC.set(actorSpawnPointIDKey, PersistentDataType.STRING, instance.getSpawnPointID());
		}

		ActorAudienceKey audienceKey = instance.getAudienceKey();

		if (audienceKey.audienceID() == null) {
			villagerPDC.remove(audienceIDKey);
		} else {
			villagerPDC.set(audienceIDKey, PersistentDataType.STRING, audienceKey.audienceID().toString());
		}
		villagerPDC.set(audienceScopeKey, PersistentDataType.STRING, audienceKey.scope().toString());
		villagerPDC.set(actorInstanceIDKey, PersistentDataType.STRING, instance.getInstanceID().toString());
		villagerPDC.set(actorDefinitionIDKey, PersistentDataType.STRING, instance.getActorDefinition().getActorID());
	}


}
