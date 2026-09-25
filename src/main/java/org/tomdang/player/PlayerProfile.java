package org.tomdang.player;



import lombok.Getter;
import lombok.Setter;
import org.tomdang.player.playerresource.PlayerResource;
import org.tomdang.player.stats.PlayerStatBlock;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.skill.SkillProgress;
import org.tomdang.player.skill.SkillXpCurve;

import java.util.UUID;

public class PlayerProfile {

	@Getter
	private final UUID uuid;

	@Getter
	private long miningXP = 0;
	@Getter
	private long combatXP = 0;
	@Getter
	private long foragingXP = 0;
	@Getter
	private long huntingXP = 0;

	public int getMiningLVL() { return SkillXpCurve.levelForXp(miningXP); }
	public int getCombatLvl() { return SkillXpCurve.levelForXp(combatXP); }
	public SkillProgress getMiningProgress() { return SkillProgress.fromTotalXp(miningXP); }
	public SkillProgress getCombatProgress() { return SkillProgress.fromTotalXp(combatXP); }
	public SkillProgress getForagingProgress() { return SkillProgress.fromTotalXp(foragingXP); }
	public SkillProgress getHuntingProgress() { return SkillProgress.fromTotalXp(huntingXP); }

	public void setMiningXP(long totalXp) {
		SkillXpCurve.levelForXp(totalXp);
		miningXP = Math.min(totalXp, SkillXpCurve.totalXpForLevel(SkillXpCurve.MAX_LEVEL));
	}

	public void setCombatXP(long totalXp) {
		SkillXpCurve.levelForXp(totalXp);
		combatXP = Math.min(totalXp, SkillXpCurve.totalXpForLevel(SkillXpCurve.MAX_LEVEL));
	}
	public void setForagingXP(long totalXp) {
		SkillXpCurve.levelForXp(totalXp);
		foragingXP = Math.min(totalXp, SkillXpCurve.totalXpForLevel(SkillXpCurve.MAX_LEVEL));
	}
	public void setHuntingXP(long totalXp) {
		SkillXpCurve.levelForXp(totalXp);
		huntingXP = Math.min(totalXp, SkillXpCurve.totalXpForLevel(SkillXpCurve.MAX_LEVEL));
	}

	public void setMiningLVL(int level) { setMiningXP(SkillXpCurve.totalXpForLevel(level)); }
	public void setCombatLvl(int level) { setCombatXP(SkillXpCurve.totalXpForLevel(level)); }

	/** Restore the current XP-only format without applying stat rewards to base stats. */
	public void restoreSkillXp(long miningXp, long combatXp) { restoreSkillXp(miningXp, combatXp, 0); }
	public void restoreSkillXp(long miningXp, long combatXp, long foragingXp) { restoreSkillXp(miningXp, combatXp, foragingXp, 0); }
	public void restoreSkillXp(long miningXp, long combatXp, long foragingXp, long huntingXp) {
		SkillXpCurve.levelForXp(miningXp);
		SkillXpCurve.levelForXp(combatXp);
		this.miningXP = Math.min(miningXp, SkillXpCurve.totalXpForLevel(100));
		this.combatXP = Math.min(combatXp, SkillXpCurve.totalXpForLevel(100));
		this.foragingXP = Math.min(foragingXp, SkillXpCurve.totalXpForLevel(100));
		this.huntingXP = Math.min(huntingXp, SkillXpCurve.totalXpForLevel(100));
	}

	@Getter
	private final PlayerStatBlock stats = new PlayerStatBlock();

	@Getter @Setter
	private double prosperity = 0;

	@Getter
	private final PlayerResource health = new PlayerResource();
	@Getter
	private final PlayerResource energy = new PlayerResource();

	public PlayerProfile (UUID uuid) {
		this.uuid = uuid;
	}

	public void increaseMiningXP(int amount) {
		if (amount < 0) throw new IllegalArgumentException("XP award cannot be negative");
		setMiningXP(Math.addExact(miningXP, amount));
	}

	public void increaseCombatXP(int amount) {
		if (amount < 0) throw new IllegalArgumentException("XP award cannot be negative");
		setCombatXP(Math.addExact(combatXP, amount));
	}

	public void increaseMiningFortune(double amount) {
		stats.add(PlayerStatType.MINING_FORTUNE, amount);
	}

	public double getMiningFortune() {
		return stats.get(PlayerStatType.MINING_FORTUNE);
	}

	public void setMiningFortune(double amount) {
		stats.set(PlayerStatType.MINING_FORTUNE, amount);
	}

	public double getStrength() {
		return stats.get(PlayerStatType.STRENGTH);
	}

	public void setStrength(double amount) {
		stats.set(PlayerStatType.STRENGTH, amount);
	}

	public double getDefense() {
		return stats.get(PlayerStatType.DEFENSE);
	}

	public void setDefense(double amount) {
		stats.set(PlayerStatType.DEFENSE, amount);
	}

	public double getMaximumHealth() {
		return stats.get(PlayerStatType.MAX_HEALTH);
	}

	public void setMaximumHealth(double amount) {
		stats.set(PlayerStatType.MAX_HEALTH, amount);
		if (health.getCurrent() > getMaximumHealth()) {
			health.setCurrent(getMaximumHealth());
		}
	}

	public double getMaximumEnergy() {
		return stats.get(PlayerStatType.MAX_ENERGY);
	}

	public void setMaximumEnergy(double amount) {
		stats.set(PlayerStatType.MAX_ENERGY, amount);
		if (energy.getCurrent() > getMaximumEnergy()) {
			energy.setCurrent(getMaximumEnergy());
		}
	}

	public void setStat(PlayerStatType statType, double amount) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		switch (statType) {
			case MAX_HEALTH -> setMaximumHealth(amount);
			case MAX_ENERGY -> setMaximumEnergy(amount);
			default -> stats.set(statType, amount);
		}
	}

	public void resetAllStats() {
		stats.resetAll();
		if (health.getCurrent() > getMaximumHealth()) health.setCurrent(getMaximumHealth());
		if (energy.getCurrent() > getMaximumEnergy()) energy.setCurrent(getMaximumEnergy());
	}

	public void increaseMiningLevel(int amount) {
		setMiningLVL(Math.addExact(getMiningLVL(), amount));
	}

	public boolean isDead() {
		return health.getCurrent() <= 0.0;
	}

	public void reduceDefense(double amount) {
		stats.add(PlayerStatType.DEFENSE, -amount);
	}
}

