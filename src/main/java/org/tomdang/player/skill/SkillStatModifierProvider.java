package org.tomdang.player.skill;

import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import org.tomdang.player.stats.modifier.PlayerStatModifier;
import org.tomdang.player.stats.modifier.PlayerStatModifierProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Earned skill rewards are effective-stat contributions, never editable base stats. */
public final class SkillStatModifierProvider implements PlayerStatModifierProvider {
	private final PlayerProfileService profiles;

	public SkillStatModifierProvider(PlayerProfileService profiles) {
		if (profiles == null) throw new IllegalArgumentException("profiles cannot be null");
		this.profiles = profiles;
	}

	@Override
	public Collection<PlayerStatModifier> getModifiers(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
		if (profile == null) throw new IllegalStateException("Player profile is not loaded");
		List<PlayerStatModifier> result = new ArrayList<>(2);
		if (profile.getMiningLVL() > 0) result.add(new PlayerStatModifier(
				SkillType.MINING.rewardStat(), "skill:mining:fortune",
				PlayerStatContributionSource.SKILL, "Mining Skill", SkillType.MINING.rewardAt(profile.getMiningLVL())));
		if (profile.getCombatLvl() > 0) result.add(new PlayerStatModifier(
				SkillType.COMBAT.rewardStat(), "skill:combat:strength",
				PlayerStatContributionSource.SKILL, "Combat Skill", SkillType.COMBAT.rewardAt(profile.getCombatLvl())));
		return List.copyOf(result);
	}
}
