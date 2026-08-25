package org.tomdang.actorframework.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.ActorPresentationService;


public class ActorLookService {

	private final ActorPresentationService actorPresentationService;
	private final HorizontalFacingCalculator horizontalFacingCalculator;

	public ActorLookService(ActorPresentationService actorPresentationService, HorizontalFacingCalculator horizontalFacingCalculator) {
		if (actorPresentationService == null) throw new IllegalArgumentException("actorPresentationService cannot be null");
		if (horizontalFacingCalculator == null) throw new IllegalArgumentException("horizontalFacingCalculator cannot be null");

		this.actorPresentationService = actorPresentationService;
		this.horizontalFacingCalculator = horizontalFacingCalculator;
	}

	public void lookAt(ActorInstance instance, Location target) {
		if (instance == null) throw new IllegalArgumentException("Instance shouldn't be null");
		if (target == null) throw new IllegalArgumentException("target shouldn't be null");
		if (target.getWorld() == null) throw new IllegalArgumentException("target's world shouldn't be null");
		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Not on main thread");

		Location location = actorPresentationService.getPresentationLocation(instance);

		if (!target.getWorld().equals(location.getWorld())) throw new IllegalStateException("Worlds are not matching");

		Location finalLocation = horizontalFacingCalculator.rotateLocation(location, target);

		actorPresentationService.movePresentation(instance, finalLocation);

	}

}
