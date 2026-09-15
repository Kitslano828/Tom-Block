package org.tomdang.combat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.combat.attackspeed.PlayerAttackReadinessService;
import org.tomdang.combat.attackspeed.PlayerAttackIndicatorService;

public class PlayerAttackReadinessListener implements Listener {
	private final PlayerAttackReadinessService readinessService;
	private final PlayerAttackIndicatorService indicatorService;

	public PlayerAttackReadinessListener(PlayerAttackReadinessService readinessService,
	                                     PlayerAttackIndicatorService indicatorService) {
		if (readinessService == null) throw new IllegalArgumentException("readinessService cannot be null");
		if (indicatorService == null) throw new IllegalArgumentException("indicatorService cannot be null");
		this.readinessService = readinessService;
		this.indicatorService = indicatorService;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		readinessService.clear(event.getPlayer().getUniqueId());
		indicatorService.restore(event.getPlayer());
	}
}
