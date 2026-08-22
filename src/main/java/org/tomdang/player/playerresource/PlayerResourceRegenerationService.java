package org.tomdang.player.playerresource;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tomdang.TomBlock;


public class PlayerResourceRegenerationService {

	private final TomBlock instance;
	private final PlayerStatsService playerStatsService;
	private final PlayerResourceService playerResourceService;

	public PlayerResourceRegenerationService(TomBlock instance, PlayerStatsService playerStatsService, PlayerResourceService playerResourceService) {
		this.instance = instance;
		this.playerStatsService = playerStatsService;
		this.playerResourceService = playerResourceService;
	}

	public void start() {
		Bukkit.getScheduler().runTaskTimer(instance, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {

				double amountOfHealthToRegenerate = playerStatsService.getTotalHealthStat(player) / 100;
				double amountOfEnergyToRegenerate = playerStatsService.getTotalEnergy(player) / 100;

				playerResourceService.heal(player, amountOfHealthToRegenerate);
				playerResourceService.restoreEnergy(player, amountOfEnergyToRegenerate);
			}
		}, 1L, 25L);
	}

}
