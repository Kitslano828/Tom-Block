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
		this.playerProfileService = playerProfileService;
		this.customArmorService = customArmorService;
	}

	public double getTotalHealthStat(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		double totalHealthStat;

		double playerHealthStat = playerProfile.getHealth().getMaximum();
		double armorHealthStat = customArmorService.calculateBonusStats(player).getHealthBonus();
		// Add more for accessories and other stuff

		totalHealthStat = playerHealthStat + armorHealthStat;

		return totalHealthStat;
	}

	public double getTotalEnergy(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		double totalEnergyStat;

		double playerEnergyStat = playerProfile.getEnergy().getMaximum();

		totalEnergyStat = playerEnergyStat;

		return totalEnergyStat;
	}

	public double getTotalDefense(Player player) {

		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		double totalDefenseStat;

		double playerDefenseStat = playerProfile.getDefense();
		double armorDefenseStat = customArmorService.calculateBonusStats(player).getDefenseBonus();
		// Add more for accessories

		totalDefenseStat = playerDefenseStat + armorDefenseStat;

		return totalDefenseStat;
	}

	public double getTotalMiningFortune(Player player, MiningTool miningtool) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		double totalMiningFortuneStat;

		double playerMiningFortune = playerProfile.getMiningFortune();
		double toolMiningFortune = miningtool.getFortune();

		totalMiningFortuneStat = playerMiningFortune + toolMiningFortune;

		return totalMiningFortuneStat;
	}

	public double getTotalStrength(Player player, Weapon weapon) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
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

}
