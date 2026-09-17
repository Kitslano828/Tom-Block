package org.tomdang.player.skill;

import org.tomdang.player.PlayerProfile;

/** The shared mutation boundary for XP earned through gameplay. */
public final class SkillProgressionService {
	public SkillAwardResult awardXp(PlayerProfile profile, SkillType skill, long amount) {
		if (profile == null || skill == null) throw new IllegalArgumentException("Profile and skill are required");
		if (amount < 0) throw new IllegalArgumentException("XP award cannot be negative");
		SkillProgress before = skill.progress(profile);
		long cap = SkillXpCurve.totalXpForLevel(SkillXpCurve.MAX_LEVEL);
		long requestedTotal = before.totalXp() + Math.min(amount, cap - before.totalXp());
		skill.setTotalXp(profile, requestedTotal);
		SkillProgress after = skill.progress(profile);
		return new SkillAwardResult(skill, after.totalXp() - before.totalXp(), before, after);
	}
}
