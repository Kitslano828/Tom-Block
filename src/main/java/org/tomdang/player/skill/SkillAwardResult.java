package org.tomdang.player.skill;

/** Progress before and after one earned-XP award. */
public record SkillAwardResult(SkillType skill, long awardedXp, SkillProgress before, SkillProgress after) {
	public int levelsGained() { return after.level() - before.level(); }
	public boolean leveledUp() { return levelsGained() > 0; }
}
