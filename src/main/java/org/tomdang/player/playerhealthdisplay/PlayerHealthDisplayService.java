package org.tomdang.player.playerhealthdisplay;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerStatsService;

public class PlayerHealthDisplayService {

	private final PlayerProfileService playerProfileService;
	private final PlayerStatsService playerStatsService;

	public PlayerHealthDisplayService(PlayerProfileService playerProfileService, PlayerStatsService playerStatsService) {
		this.playerProfileService = playerProfileService;
		this.playerStatsService = playerStatsService;
	}

	public void displayHealth(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		double currentHealth = playerProfile.getHealth().getCurrent();
		double effectiveMaxHealth = playerStatsService.getTotalHealthStat(player);

		if (effectiveMaxHealth <= 0) return;

		double percent = Math.clamp(currentHealth / effectiveMaxHealth, 0.0, 1.0);

		double vanillaHearts = percent * 20;
		player.setHealth(vanillaHearts);
	}

}
