package org.tomdang.combat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.tomdang.combat.CombatService;

public class MobHitListener implements Listener {
	private final CombatService combatService;

	public MobHitListener(CombatService combatService) {
		this.combatService = combatService;
	}

	@EventHandler
	public void onMobHit(EntityDamageByEntityEvent event) {
		combatService.onMobHit(event);
	}
}
