package org.tomdang.actorframework.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.tomdang.actorframework.combat.ActorDamageService;

public class ActorDamageListener implements Listener {

	private final ActorDamageService actorDamageService;

	public ActorDamageListener(ActorDamageService actorDamageService) {
		if (actorDamageService == null) throw new IllegalArgumentException("Actor Damage Service cannot be null");

		this.actorDamageService = actorDamageService;
	}

	@EventHandler
	public void onEntityHit(EntityDamageEvent event) {
		actorDamageService.handleDamage(event);
	}

}
