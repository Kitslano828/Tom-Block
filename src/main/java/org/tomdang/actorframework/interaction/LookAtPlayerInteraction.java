package org.tomdang.actorframework.interaction;

import org.bukkit.entity.Player;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.movement.ActorLookService;

public class LookAtPlayerInteraction implements ActorInteraction{

	private final ActorLookService actorLookService;
	private final ActorInteraction delegate;

	public LookAtPlayerInteraction(ActorLookService actorLookService, ActorInteraction delegate) {
		if (actorLookService == null) throw new IllegalArgumentException("actorLookService should not be null");
		if (delegate == null) throw new IllegalArgumentException("delegate should not be null");

		this.actorLookService = actorLookService;
		this.delegate = delegate;
	}

	@Override
	public void interact(ActorInteractionContext context) {
		if (context == null) throw new IllegalArgumentException("Context should not be null");

		Player player = context.player();
		ActorInstance instance = context.instance();

		actorLookService.lookAt(instance, player.getLocation());
		delegate.interact(context);
	}
}
