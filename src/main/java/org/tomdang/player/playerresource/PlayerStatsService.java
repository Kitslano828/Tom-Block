package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.tomdang.combat.weapons.Weapon;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;

public class PlayerStatsService {

	private final PlayerProfileService playerProfileService;
	private final CustomArmorService customArmorService;

	public PlayerStatsService(PlayerProfileService playerProfileService, CustomArmorService customArmorService) {
		if (playerProfileService == null) throw new IllegalArgumentException("playerProfileService cannot be null");
		if (customArmorService == null) throw new IllegalArgumentException("customArmorService cannot be null");
		this.playerProfileService = playerProfileService;
		this.customArmorService = customArmorService;
	}

	public double getTotalHealthStat(Player player) {
		PlayerProfile playerProfile = requireProfile(player);

		double totalHealthStat;

		double playerHealthStat = playerProfile.getMaximumHealth();
		double armorHealthStat = customArmorService.calculateBonusStats(player).getHealthBonus();
		// Add more for accessories and other stuff

		totalHealthStat = playerHealthStat + armorHealthStat;

		return totalHealthStat;
	}

	public double getTotalEnergy(Player player) {
		PlayerProfile playerProfile = requireProfile(player);

		double totalEnergyStat;

		double playerEnergyStat = playerProfile.getMaximumEnergy();

		totalEnergyStat = playerEnergyStat;

		return totalEnergyStat;
	}

	public double getTotalDefense(Player player) {

		PlayerProfile playerProfile = requireProfile(player);

		double totalDefenseStat;

		double playerDefenseStat = playerProfile.getDefense();
		double armorDefenseStat = customArmorService.calculateBonusStats(player).getDefenseBonus();
		// Add more for accessories

		totalDefenseStat = playerDefenseStat + armorDefenseStat;

		return totalDefenseStat;
	}

	public double getTotalMiningFortune(Player player, MiningTool miningtool) {
		PlayerProfile playerProfile = requireProfile(player);
		double totalMiningFortuneStat;

		double playerMiningFortune = playerProfile.getMiningFortune();
		double toolMiningFortune = miningtool == null ? 0 : miningtool.getFortune();

		totalMiningFortuneStat = playerMiningFortune + toolMiningFortune;

		return totalMiningFortuneStat;
	}

	public double getTotalStrength(Player player, Weapon weapon) {
		PlayerProfile playerProfile = requireProfile(player);
		double totalStrength;

		double weaponStrength;

		double playerStrength = playerProfile.getStrength();
		if (weapon == null) {
			weaponStrength = 0;
		} else {
			weaponStrength = weapon.getStrength();
		}

		totalStrength = playerStrength + weaponStrength;

		return totalStrength;
	}

	private PlayerProfile requireProfile(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		if (playerProfile == null) {
			throw new IllegalStateException("No player profile is loaded for " + player.getUniqueId());
		}
		return playerProfile;
	}

}
