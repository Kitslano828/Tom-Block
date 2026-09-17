package org.tomdang.combat.damage;

import org.bukkit.entity.Player;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

/** Chooses which player stats contribute to a basic hit. Eligibility and readiness are separate. */
public final class BasicAttackCalculationService {
	private final PlayerStatsService stats;
	private final PlayerDamageCalculator combatCalculator;
	private final CriticalHitRoller criticalHitRoller;
	private final JellyfishHuntingDamageCalculator huntingCalculator;

	public BasicAttackCalculationService(PlayerStatsService stats, PlayerDamageCalculator combatCalculator,
	                                     CriticalHitRoller criticalHitRoller,
	                                     JellyfishHuntingDamageCalculator huntingCalculator) {
		if (stats == null || combatCalculator == null || criticalHitRoller == null || huntingCalculator == null) {
			throw new IllegalArgumentException("basic attack calculation dependencies are required");
		}
		this.stats = stats;
		this.combatCalculator = combatCalculator;
		this.criticalHitRoller = criticalHitRoller;
		this.huntingCalculator = huntingCalculator;
	}

	public PlayerAttackResult calculate(Player player, BasicAttackCalculationProfile profile) {
		if (player == null || profile == null) throw new IllegalArgumentException("player and profile are required");
		return switch (profile) {
			case COMBAT -> calculateCombat(player);
			case JELLYFISH_HUNTING -> new PlayerAttackResult(
					huntingCalculator.calculate(
							stats.getTotalStat(player, PlayerStatType.JELLYFISH_POWER),
							stats.getTotalStat(player, PlayerStatType.JELLYFISH_DAMAGE_BONUS)),
					false);
		};
	}

	private PlayerAttackResult calculateCombat(Player player) {
		double damage = stats.getTotalDamage(player);
		double strength = stats.getTotalStrength(player);
		double criticalChance = stats.getTotalCritChance(player);
		double criticalDamage = stats.getTotalCritDamage(player);
		boolean critical = criticalHitRoller.isCritical(criticalChance);
		return combatCalculator.calculateBasicAttack(damage, strength, criticalDamage, critical);
	}
}
