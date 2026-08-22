package org.tomdang.combat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.tomdang.combat.CombatService;

public class PlayerRespawnListener implements Listener {
	private final CombatService combatService;

	public PlayerRespawnListener(CombatService combatService) {
		this.combatService = combatService;
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		combatService.respawnPlayer(event.getPlayer());
	}
}
