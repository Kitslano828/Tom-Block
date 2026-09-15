package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;
import org.tomdang.player.stats.modifier.PlayerStatModifierCalculator;
import org.tomdang.player.stats.modifier.PlayerStatModifierProvider;

import java.util.Collection;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.tomdang.player.stats.evaluation.PlayerStatBreakdown;
import org.tomdang.player.stats.evaluation.PlayerStatContribution;
import org.tomdang.player.stats.evaluation.PlayerStatEvaluation;

public class PlayerStatsService {

	private final PlayerProfileService playerProfileService;
	private final PlayerStatModifierProvider statModifierProvider;
	private final PlayerStatModifierCalculator playerStatModifierCalculator;

	public PlayerStatsService(PlayerProfileService playerProfileService, PlayerStatModifierProvider statModifierProvider, PlayerStatModifierCalculator playerStatModifierCalculator) {
		if (playerProfileService == null) throw new IllegalArgumentException("playerProfileService cannot be null");
		if (statModifierProvider == null) throw new IllegalArgumentException("statModifierProvider cannot be null");
		if (playerStatModifierCalculator == null) throw new IllegalArgumentException("playerStatModifierCalculator cannot be null");
		this.playerProfileService = playerProfileService;
		this.statModifierProvider = statModifierProvider;
		this.playerStatModifierCalculator = playerStatModifierCalculator;
	}

	public double getTotalHealthStat(Player player) {
		return getTotalStat(player, PlayerStatType.MAX_HEALTH);
	}

	public double getTotalEnergy(Player player) {
		return getTotalStat(player, PlayerStatType.MAX_ENERGY);
	}

	public double getTotalDefense(Player player) {
		return getTotalStat(player, PlayerStatType.DEFENSE);
	}

	public double getTotalMiningFortune(Player player) {
		return getTotalStat(player, PlayerStatType.MINING_FORTUNE);
	}

	public double getTotalStrength(Player player) {
		return getTotalStat(player, PlayerStatType.STRENGTH);
	}

	public double getTotalDamage(Player player) {
		return getTotalStat(player, PlayerStatType.DAMAGE);
	}

	public double getTotalMiningSpeed(Player player) {
		return getTotalStat(player, PlayerStatType.MINING_SPEED);
	}

	public double getTotalStat(Player player, PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		PlayerProfile playerProfile = requireProfile(player);
		Collection<PlayerStatModifier> modifiers = statModifierProvider.getModifiers(player);
		return playerStatModifierCalculator.calculate(playerProfile.getStats(), statType, modifiers);
	}

	public PlayerStatEvaluation evaluate(Player player) {
		PlayerProfile playerProfile = requireProfile(player);
		Collection<PlayerStatModifier> modifiers = statModifierProvider.getModifiers(player);
		if (modifiers == null) throw new IllegalStateException("stat modifiers cannot be null");
		if (modifiers.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalStateException("stat modifiers cannot contain null elements");
		}

		EnumMap<PlayerStatType, PlayerStatBreakdown> breakdowns = new EnumMap<>(PlayerStatType.class);
		for (PlayerStatType statType : PlayerStatType.values()) {
			List<PlayerStatContribution> contributions = new ArrayList<>();
			for (PlayerStatModifier modifier : modifiers) {
				if (modifier.getStatType() != statType) continue;
				contributions.add(new PlayerStatContribution(
						statType,
						modifier.getSource(),
						modifier.getSourceId(),
						modifier.getDisplayName(),
						modifier.getAmount()
				));
			}

			breakdowns.put(statType, new PlayerStatBreakdown(
					statType,
					playerProfile.getStats().get(statType),
					contributions,
					playerStatModifierCalculator.calculate(playerProfile.getStats(), statType, modifiers)
			));
		}
		return new PlayerStatEvaluation(breakdowns);
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
