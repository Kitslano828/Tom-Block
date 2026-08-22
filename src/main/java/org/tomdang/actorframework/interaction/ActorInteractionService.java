package org.tomdang.actorframework.interaction;

import org.bukkit.entity.Player;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.instance.ActorInstance;

public class ActorInteractionService {

	private final ActorInteractionRegistry actorInteractionRegistry;

	public ActorInteractionService(ActorInteractionRegistry actorInteractionRegistry) {
		if (actorInteractionRegistry == null) throw new IllegalArgumentException("Actor interaction registry cannot be null");

		this.actorInteractionRegistry = actorInteractionRegistry;
	}

	public boolean interact(Player player, ActorInstance instance) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");

		ActorDefinition definition = instance.getActorDefinition();

		if (!definition.hasInteraction()) return false;

		String interactionID = definition.getInteractionID();
		if (!actorInteractionRegistry.isInteractionRegistered(interactionID))
			throw new IllegalStateException(definition.getActorID() + " references unregistered interaction " + interactionID);

		ActorInteractionContext context = new ActorInteractionContext(player, instance);

		ActorInteraction interaction = actorInteractionRegistry.lookupInteraction(interactionID);

		if (interaction == null) throw new IllegalStateException("Interaction is null");

		interaction.interact(context);

		return true;
	}

}
