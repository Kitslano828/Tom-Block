package org.tomdang.player.skill;

/** A view of cumulative XP, without storing a second independent level value. */
public record SkillProgress(int level, long totalXp, long xpIntoLevel, long xpNeededForNextLevel) {
	public static SkillProgress fromTotalXp(long totalXp) {
		int level = SkillXpCurve.levelForXp(totalXp);
		long into = totalXp - SkillXpCurve.totalXpForLevel(level);
		long needed = level == SkillXpCurve.MAX_LEVEL ? 0 : SkillXpCurve.xpForLevel(level + 1);
		return new SkillProgress(level, totalXp, into, needed);
	}

	public boolean maxLevel() { return level == SkillXpCurve.MAX_LEVEL; }
	public double fraction() { return maxLevel() ? 1.0 : (double) xpIntoLevel / xpNeededForNextLevel; }
}
