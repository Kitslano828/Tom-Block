package org.tomdang.actorframework.instance;

import org.tomdang.actorframework.audience.ActorAudienceKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActorInstanceRegistry {

	private final Map<UUID, ActorInstance> actorInstances = new HashMap<>();
	private final Map<ActorInstanceKey, UUID> instanceKeyUUIDMap = new HashMap<>();

	public void registerInstance(ActorInstance instance) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		ActorInstanceKey instanceKey = new ActorInstanceKey(instance.getActorDefinition().getActorID(), instance.getAudienceKey(), instance.getSpawnPointID());
		UUID instanceID = instance.getInstanceID();
		if (instanceID == null) throw new IllegalArgumentException("Instance ID cannot be null");

		if (actorInstances.containsKey(instanceID)) throw new IllegalStateException("Actor instance already exists");
		if (instanceKeyUUIDMap.containsKey(instanceKey)) throw new IllegalStateException("An instance already exists for this actor and audience");

		actorInstances.put(instanceID, instance);
		instanceKeyUUIDMap.put(instanceKey, instanceID);
	}

	public ActorInstance getInstance(UUID uuid) {
		if (uuid == null) throw new IllegalArgumentException("Instance ID cannot be null");
		if (!actorInstances.containsKey(uuid)) return null;
		return actorInstances.get(uuid);
	}

	public ActorInstance getInstanceForPlacement(String actorID, ActorAudienceKey audienceKey, String spawnPointID) {
		ActorInstanceKey instanceKey = new ActorInstanceKey(actorID, audienceKey, spawnPointID);
		UUID instanceID = instanceKeyUUIDMap.get(instanceKey);
		if (instanceID == null) return null;
		return actorInstances.get(instanceID);
	}

	public boolean isInstanceRegistered(UUID uuid) {
		if (uuid == null) throw new IllegalArgumentException("Instance ID cannot be null");
		return actorInstances.containsKey(uuid);
	}

	public ActorInstance removeInstance(UUID uuid) {
		if (uuid == null) throw new IllegalArgumentException("Instance ID cannot be null");
		ActorInstance removedInstance = actorInstances.get(uuid);
		if (removedInstance == null) return null;
		actorInstances.remove(uuid);

		String instanceActorID = removedInstance.getActorDefinition().getActorID();
		ActorAudienceKey audienceKey = removedInstance.getAudienceKey();
		ActorInstanceKey instanceKey = new ActorInstanceKey(instanceActorID, audienceKey, removedInstance.getSpawnPointID());
		instanceKeyUUIDMap.remove(instanceKey);

		return removedInstance;
	}

}
