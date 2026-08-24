package org.tomdang.actorframework.movement;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class ActorMovementTaskService {

	private final Plugin plugin;
	private final ActorMovementTaskRegistry actorMovementTaskRegistry;

	public ActorMovementTaskService(Plugin plugin, ActorMovementTaskRegistry actorMovementTaskRegistry) {
		if (plugin == null) throw new IllegalArgumentException("plugin should not be null");
		if (actorMovementTaskRegistry == null) throw new IllegalArgumentException("actorMovementTaskRegistry should not be null");

		this.plugin = plugin;
		this.actorMovementTaskRegistry = actorMovementTaskRegistry;
	}

	public void start(UUID instanceID, ActorMovementTick movementTick) {
		if (instanceID == null) throw new IllegalArgumentException("instanceID should not be null");
		if (movementTick == null) throw new IllegalArgumentException("movementTick should not be null");

		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("not on main thread");

		actorMovementTaskRegistry.cancelMovement(instanceID);

		final BukkitTask[] taskHolder = new BukkitTask[1];

		BukkitTask scheduledTask = plugin.getServer().getScheduler().runTaskTimer(
				plugin,
				() -> {

					BukkitTask currentTask = taskHolder[0];

					try {
						boolean reached = movementTick.tick();

						if (reached) {
							actorMovementTaskRegistry.completeMovement(instanceID, currentTask);
							currentTask.cancel();
						}
					} catch (RuntimeException e) {
						actorMovementTaskRegistry.completeMovement(instanceID, currentTask);
						currentTask.cancel();
						throw e;
					}
				},
				1L,
				1L
		);

		taskHolder[0] = scheduledTask;

		try {
			actorMovementTaskRegistry.registerTask(instanceID, scheduledTask);
		} catch (RuntimeException e) {
			scheduledTask.cancel();
			throw e;
		}

	}


	public boolean cancel(UUID instanceID) {
		if (instanceID == null) throw new IllegalArgumentException("Instance ID should not be null");
		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Thread is not on primary");

		return actorMovementTaskRegistry.cancelMovement(instanceID);
	}

	public void cancelAll() {
		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Thread is not on primary");

		actorMovementTaskRegistry.cancelAll();
	}

}
