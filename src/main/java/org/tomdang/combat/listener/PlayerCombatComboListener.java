package org.tomdang.combat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.combat.combo.ConsecutiveChargedHitTracker;

public class PlayerCombatComboListener implements Listener {
	private final ConsecutiveChargedHitTracker tracker;

	public PlayerCombatComboListener(ConsecutiveChargedHitTracker tracker) {
		if (tracker == null) throw new IllegalArgumentException("tracker cannot be null");
		this.tracker = tracker;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		tracker.clear(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		tracker.clear(event.getEntity().getUniqueId());
	}
}
