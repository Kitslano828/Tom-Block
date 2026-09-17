package org.tomdang.player.skill;

import org.tomdang.player.PlayerProfile;
import org.tomdang.player.stats.PlayerStatType;

public enum SkillType {
	MINING(PlayerStatType.MINING_FORTUNE, 4),
	COMBAT(PlayerStatType.STRENGTH, 2);

	private final PlayerStatType rewardStat;
	private final double rewardPerLevel;

	SkillType(PlayerStatType rewardStat, double rewardPerLevel) {
		this.rewardStat = rewardStat;
		this.rewardPerLevel = rewardPerLevel;
	}

	public SkillProgress progress(PlayerProfile profile) {
		if (profile == null) throw new IllegalArgumentException("profile cannot be null");
		return switch (this) {
			case MINING -> profile.getMiningProgress();
			case COMBAT -> profile.getCombatProgress();
		};
	}

	public void setTotalXp(PlayerProfile profile, long totalXp) {
		if (profile == null) throw new IllegalArgumentException("profile cannot be null");
		switch (this) {
			case MINING -> profile.setMiningXP(totalXp);
			case COMBAT -> profile.setCombatXP(totalXp);
		}
	}

	public PlayerStatType rewardStat() { return rewardStat; }
	public double rewardAt(int level) { return level * rewardPerLevel; }
	public double rewardPerLevel() { return rewardPerLevel; }
}
