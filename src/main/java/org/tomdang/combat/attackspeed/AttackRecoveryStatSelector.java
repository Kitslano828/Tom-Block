package org.tomdang.combat.attackspeed;

import org.bukkit.entity.Player;
import org.tomdang.combat.damage.BasicAttackCalculationProfile;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

/** Keeps combat Attack Speed out of fishing recovery and the matching vanilla indicator. */
public final class AttackRecoveryStatSelector {
	public double select(Player player, CustomItem heldItem, PlayerStatsService stats) {
		if (player == null || stats == null) throw new IllegalArgumentException("player and stats are required");
		BasicAttackCalculationProfile profile = heldItem == null
				? BasicAttackCalculationProfile.COMBAT
				: heldItem.getCombatProfile().basicAttackCalculationProfile();
		return switch (profile) {
			case COMBAT -> stats.getTotalStat(player, PlayerStatType.ATTACK_SPEED);
			case JELLYFISH_HUNTING -> 0.0;
		};
	}
}
