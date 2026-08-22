package org.tomdang.actorframework.combat;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.resolver.ActorResolver;

public class ActorDamageService {

	private final ActorResolver actorResolver;

	public ActorDamageService(ActorResolver actorResolver) {
		if (actorResolver == null) throw new IllegalArgumentException("Actor Resolver cannot be null");

		this.actorResolver = actorResolver;
	}

	public boolean handleDamage(EntityDamageEvent event) {
		Entity entityAttacked = event.getEntity();
		ActorDefinition definition = actorResolver.resolveDefinition(entityAttacked);

		if (definition == null) return false;

		if (definition.getDamagePolicy() == ActorDamagePolicy.PROTECTED) {
			event.setCancelled(true);
			return true;
		}

		return true;
	}

}
