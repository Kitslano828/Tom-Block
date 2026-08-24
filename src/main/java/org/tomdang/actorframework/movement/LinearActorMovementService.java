package org.tomdang.actorframework.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.ActorPresentationService;

import java.util.UUID;

public class LinearActorMovementService {

	private final Plugin plugin;
	private final ActorPresentationService actorPresentationService;
	private final LinearMovementStepCalculator linearMovementStepCalculator;
	private final ActorMovementTaskRegistry actorMovementTaskRegistry;

	public LinearActorMovementService(Plugin plugin, ActorPresentationService actorPresentationService, LinearMovementStepCalculator linearMovementStepCalculator, ActorMovementTaskRegistry actorMovementTaskRegistry) {

		if (plugin == null) throw new IllegalArgumentException("Plugin shouldn't be null");
		if (actorPresentationService == null) throw new IllegalArgumentException("actorPresentationService shouldn't be null");
		if (linearMovementStepCalculator == null) throw new IllegalArgumentException("linearMovementStepCalculator shouldn't be null");
		if (actorMovementTaskRegistry == null) throw new IllegalArgumentException("actorMovementTaskRegistry shouldn't be null");

		this.plugin = plugin;
		this.actorPresentationService = actorPresentationService;
		this.linearMovementStepCalculator = linearMovementStepCalculator;
		this.actorMovementTaskRegistry = actorMovementTaskRegistry;
	}

	public void moveTo(ActorInstance instance, Location destination, double distancePerTick) {
		if (instance == null) throw new IllegalArgumentException("Instance shouldn't be null");
		if (destination == null) throw new IllegalArgumentException("destination shouldn't be null");
		if (destination.getWorld() == null) throw new IllegalArgumentException("destination's world shouldn't be null");
		if (!Double.isFinite(distancePerTick)) throw new IllegalArgumentException("Max distance cannot be nan or infinite");
		if (distancePerTick <= 0) throw new IllegalArgumentException("Max distance must be greater than zero");

		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Thread is not on primary");

		Location destinationLocation = destination.clone();
		Location currentLocation = actorPresentationService.getPresentationLocation(instance);
		if (!destinationLocation.getWorld().equals(currentLocation.getWorld())) throw new IllegalStateException("Different worlds are yet to be supported");

		UUID instanceID = instance.getInstanceID();
		actorMovementTaskRegistry.cancelMovement(instanceID);

		if (linearMovementStepCalculator.hasReachedDestination(currentLocation, destinationLocation)) {
			actorPresentationService.movePresentation(instance, destinationLocation);
			return;
		}

		BukkitTask[] taskHolder = new BukkitTask[1];

		BukkitTask scheduledTask = plugin.getServer().getScheduler().runTaskTimer(
				plugin,
				() -> {
					BukkitTask currentTask = taskHolder[0];
					try {
						Location current = actorPresentationService.getPresentationLocation(instance);
						Location nextLocation = linearMovementStepCalculator.calculateNextLocation(current, destinationLocation, distancePerTick);
						actorPresentationService.movePresentation(instance, nextLocation);
						if (linearMovementStepCalculator.hasReachedDestination(nextLocation, destinationLocation)) {
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
}
