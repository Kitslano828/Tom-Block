package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerhealthdisplay.PlayerHealthDisplayService;

public class PlayerResourceService {

	private final PlayerProfileService playerProfileService;
	private final PlayerStatsService playerStatsService;
	private final PlayerHealthDisplayService playerHealthDisplayService;

	public PlayerResourceService(PlayerProfileService playerProfileService,
								 PlayerStatsService playerStatsService,
								 PlayerHealthDisplayService playerHealthDisplayService
	) {
		this.playerProfileService = playerProfileService;
		this.playerStatsService = playerStatsService;
		this.playerHealthDisplayService = playerHealthDisplayService;
	}

	public void reconcilePlayerHealth(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectMaxHealth = playerStatsService.getTotalHealthStat(player);
		reconcileResource(playerProfile.getHealth(), effectMaxHealth);
		playerHealthDisplayService.displayHealth(player);
	}

	public void restoreHealthToMaximum(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectMaxHealth = playerStatsService.getTotalHealthStat(player);
		restoreResourceToMax(playerProfile.getHealth(), effectMaxHealth);
		playerHealthDisplayService.displayHealth(player);
	}

	public void heal(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectMaxHealth = playerStatsService.getTotalHealthStat(player);
		regenerateResource(playerProfile.getHealth(), amount, effectMaxHealth);
		playerHealthDisplayService.displayHealth(player);
	}

	public void damagePlayer(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		reduceResource(playerProfile.getHealth(), amount);
		playerHealthDisplayService.displayHealth(player);
	}

	public void restoreEnergy(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectiveMaxEnergy = playerStatsService.getTotalEnergy(player);
		regenerateResource(playerProfile.getEnergy(), amount, effectiveMaxEnergy);
	}

	public void restoreMaxEnergy(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double effectiveMaxEnergy = playerStatsService.getTotalEnergy(player);
		restoreResourceToMax(playerProfile.getEnergy(), effectiveMaxEnergy);
	}

	public boolean spendEnergy(Player player, double amount) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		if (playerProfile.getEnergy().getCurrent() < amount) {
			return false;
		} else {
			reduceResource(playerProfile.getEnergy(), amount);
			return true;
		}
	}

	private void reduceResource(PlayerResource resource, double amount) {
		double resourceRemaining = resource.getCurrent() - amount;
		resource.setCurrent(resourceRemaining);
	}

	private void regenerateResource(PlayerResource resource, double amount, double effectiveMaximum) {
		resource.addCurrent(amount, effectiveMaximum);
	}

	private void reconcileResource(PlayerResource resource, double effectMaximum) {
		if (resource.getCurrent() > effectMaximum) {
			resource.setCurrent(effectMaximum);
		}
	}

	private void restoreResourceToMax(PlayerResource resource, double effectiveMaximum) {
		resource.restoreFull(effectiveMaximum);
	}
}
