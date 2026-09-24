package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;

public final class PlayerEnergyDisplayService {

    private final PlayerProfileService playerProfileService;
    private final PlayerStatsService playerStatsService;

    public PlayerEnergyDisplayService(PlayerProfileService playerProfileService, PlayerStatsService playerStatsService) {
        this.playerProfileService = playerProfileService;
        this.playerStatsService = playerStatsService;
    }

    public void displayEnergy(Player player) {
        PlayerProfile profile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
        double maximumEnergy = playerStatsService.getTotalEnergy(player);
        double maximumHealth = playerStatsService.getTotalHealthStat(player);
        float visibleHealth = (float)(20.0 * percentage(profile.getHealth().getCurrent(), maximumHealth));
        int visibleFoodLevel = (int)Math.round(20.0 * percentage(profile.getEnergy().getCurrent(), maximumEnergy));

        player.sendHealthUpdate(visibleHealth, visibleFoodLevel, 0.0f);
    }

    private double percentage(double current, double maximum) {
        return maximum <= 0 ? 0.0 : Math.clamp(current / maximum, 0.0, 1.0);
    }
}
