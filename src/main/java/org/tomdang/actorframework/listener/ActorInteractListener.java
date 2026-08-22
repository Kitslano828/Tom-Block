package org.tomdang.actorframework.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.interaction.ActorInteractionService;
import org.tomdang.actorframework.resolver.ActorResolver;

public class ActorInteractListener implements Listener {

	private final ActorResolver actorResolver;
	private final ActorInteractionService actorInteractionService;

	public ActorInteractListener(ActorResolver actorResolver, ActorInteractionService actorInteractionService) {
		if (actorResolver == null) throw new IllegalArgumentException("Actor Resolver cannot be null");
		if (actorInteractionService == null) throw new IllegalArgumentException("Actor Interaction Service cannot be null");

		this.actorResolver = actorResolver;
		this.actorInteractionService = actorInteractionService;
	}

	@EventHandler
	public void playerInteractActor(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity clickedEntity = event.getRightClicked();

		if (event.getHand() != EquipmentSlot.HAND) return;

		ActorInstance instance = actorResolver.resolveInstance(clickedEntity);
		if (instance == null) return;
		event.setCancelled(true);
		actorInteractionService.interact(player, instance);
	}

}
