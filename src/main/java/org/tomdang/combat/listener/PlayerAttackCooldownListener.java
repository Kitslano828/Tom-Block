package org.tomdang.combat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.combat.attackspeed.PlayerAttackCooldownService;

public class PlayerAttackCooldownListener implements Listener {

	private final PlayerAttackCooldownService cooldownService;

	public PlayerAttackCooldownListener(PlayerAttackCooldownService cooldownService) {
		if (cooldownService == null) throw new IllegalArgumentException("cooldownService cannot be null");
		this.cooldownService = cooldownService;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		cooldownService.clear(event.getPlayer().getUniqueId());
	}
}
