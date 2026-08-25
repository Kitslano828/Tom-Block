package org.tomdang.actorframework.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.ActorPresentationService;

import java.util.UUID;

public class ActorFollowService {

	private final ActorPresentationService actorPresentationService;
	private final LinearMovementStepCalculator linearMovementStepCalculator;
	private final ActorMovementTaskService actorMovementTaskService;

	public ActorFollowService(ActorPresentationService actorPresentationService, LinearMovementStepCalculator linearMovementStepCalculator, ActorMovementTaskService actorMovementTaskService) {
		if (actorPresentationService == null) throw new IllegalArgumentException("actorPresentationService should not be null");
		if (linearMovementStepCalculator == null) throw new IllegalArgumentException("linearMovementStepCalculator should not be null");
		if (actorMovementTaskService == null) throw new IllegalArgumentException("actorMovementTaskService should not be null");

		this.actorPresentationService = actorPresentationService;
		this.linearMovementStepCalculator = linearMovementStepCalculator;
		this.actorMovementTaskService = actorMovementTaskService;
	}

	public void follow(ActorInstance instance, UUID playerUUID, double maxMovableSpeed, double distanceFromPlayer) {
		if (instance == null) throw new IllegalArgumentException("Instance should not be null");
		if (playerUUID == null) throw new IllegalArgumentException("playerUUID should not be null");
		if (!Double.isFinite(maxMovableSpeed) || maxMovableSpeed <= 0)
			throw new IllegalArgumentException("Move speed needs to be greater than 0 and finite");
		if (!Double.isFinite(distanceFromPlayer) || distanceFromPlayer < 0)
			throw new IllegalArgumentException("distanceFromPlayer needs to be 0 or greater and finite");
		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Not on main thread");

		Player player = Bukkit.getPlayer(playerUUID);
		if (player == null) throw new IllegalStateException("Player is null");
		if (!player.isOnline()) throw new IllegalStateException("Player is not online");

		actorMovementTaskService.start(instance.getInstanceID(), () -> {
					Player playerFollowing = Bukkit.getPlayer(playerUUID);
					if (playerFollowing == null) return true;
					if (!playerFollowing.isOnline()) return true;

					Location currentLocation = actorPresentationService.getPresentationLocation(instance);
					Location currentPlayerLocation = playerFollowing.getLocation();

					if (!currentLocation.getWorld().equals(currentPlayerLocation.getWorld())) return true;

					double distance = currentLocation.distance(currentPlayerLocation);

					if (distance <= distanceFromPlayer) {
						// The actor does not move, but following remains active so it can resume when the player moves away.
						return false;
					}

					double distanceAway = distance - distanceFromPlayer;

					double stepDistance = Math.min(maxMovableSpeed, distanceAway);

					Location next = linearMovementStepCalculator.calculateNextLocation(currentLocation, currentPlayerLocation, stepDistance);
					actorPresentationService.movePresentation(instance, next);

					return false;
				}
		);
	}

}
