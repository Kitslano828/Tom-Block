package org.tomdang.player.playerhealthdisplay;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerStatsService;

public class PlayerHealthDisplayService {
	private static final float STABLE_SATURATION = 20.0f;

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
		double currentEnergy = playerProfile.getEnergy().getCurrent();
		double effectiveMaxEnergy = playerStatsService.getTotalEnergy(player);

		float visibleHealth = (float)(20.0 * percentage(currentHealth, effectiveMaxHealth));
		int visibleFood = (int)Math.round(20.0 * percentage(currentEnergy, effectiveMaxEnergy));
		// Keep the native food renderer stable when a health refresh also carries
		// TomBlock's energy-backed food value.
		player.sendHealthUpdate(visibleHealth, visibleFood, STABLE_SATURATION);
	}

	private double percentage(double current, double maximum) {
		return maximum <= 0 ? 0.0 : Math.clamp(current / maximum, 0.0, 1.0);
	}

}
