package org.tomdang.actorframework.movement;

import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActorMovementTaskRegistry {

	private final Map<UUID, BukkitTask>  movementTasks = new HashMap<>();

	public void registerTask(UUID instanceID, BukkitTask task) {
		if (instanceID == null) throw new IllegalArgumentException("InstanceID cannot be null");
		if (task == null) throw new IllegalArgumentException("task cannot be null");

		if (movementTasks.containsKey(instanceID)) throw new IllegalStateException("Bukkit task for this instance already exist");

		movementTasks.put(instanceID, task);
	}

	public boolean hasActiveMovement(UUID instanceID) {
		if (instanceID == null) throw new IllegalArgumentException("InstanceID cannot be null");
		return movementTasks.containsKey(instanceID);
	}

	public BukkitTask getMovement(UUID instanceID) {
		if (instanceID == null) throw new IllegalArgumentException("InstanceID cannot be null");
		return movementTasks.get(instanceID);
	}

	public boolean cancelMovement(UUID instanceID) {
		if (instanceID == null) throw new IllegalArgumentException("InstanceID cannot be null");

		BukkitTask removedTask = movementTasks.remove(instanceID);
		if (removedTask == null) return false;
		removedTask.cancel();
		return true;
	}


	public boolean completeMovement(UUID instanceID, BukkitTask completedTask) {
		if (instanceID == null) throw new IllegalArgumentException("InstanceID cannot be null");
		if (completedTask == null) throw new IllegalArgumentException("completed Task cannot be null");

		BukkitTask task = movementTasks.get(instanceID);

		if (completedTask != task) return false;

		movementTasks.remove(instanceID);
		return true;
	}

	public void cancelAll() {

		for (BukkitTask task : movementTasks.values()) {
			task.cancel();
		}

		movementTasks.clear();
	}
}
