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

	public int getMiningLVL() { return SkillXpCurve.levelForXp(miningXP); }
	public int getCombatLvl() { return SkillXpCurve.levelForXp(combatXP); }
	public SkillProgress getMiningProgress() { return SkillProgress.fromTotalXp(miningXP); }
	public SkillProgress getCombatProgress() { return SkillProgress.fromTotalXp(combatXP); }

	public void setMiningXP(long totalXp) {
		int previous = getMiningLVL();
		int next = SkillXpCurve.levelForXp(totalXp);
		miningXP = Math.min(totalXp, SkillXpCurve.totalXpForLevel(SkillXpCurve.MAX_LEVEL));
		stats.add(PlayerStatType.MINING_FORTUNE, 4.0 * (next - previous));
	}

	public void setCombatXP(long totalXp) {
		int previous = getCombatLvl();
		int next = SkillXpCurve.levelForXp(totalXp);
		combatXP = Math.min(totalXp, SkillXpCurve.totalXpForLevel(SkillXpCurve.MAX_LEVEL));
		stats.add(PlayerStatType.STRENGTH, 2.0 * (next - previous));
	}

	public void setMiningLVL(int level) { setMiningXP(SkillXpCurve.totalXpForLevel(level)); }
	public void setCombatLvl(int level) { setCombatXP(SkillXpCurve.totalXpForLevel(level)); }

	/** Load an old profile whose skill bonuses are already baked into its saved base stats. */
	public void restoreLegacySkillXp(long miningXp, int oldMiningLevel, long combatXp, int oldCombatLevel) {
		SkillXpCurve.levelForXp(miningXp);
		SkillXpCurve.levelForXp(combatXp);
		int oldMining = Math.clamp(oldMiningLevel, 0, 100);
		int oldCombat = Math.clamp(oldCombatLevel, 0, 100);
		this.miningXP = Math.min(Math.max(miningXp, SkillXpCurve.totalXpForLevel(oldMining)), SkillXpCurve.totalXpForLevel(100));
		this.combatXP = Math.min(Math.max(combatXp, SkillXpCurve.totalXpForLevel(oldCombat)), SkillXpCurve.totalXpForLevel(100));
		stats.add(PlayerStatType.MINING_FORTUNE, 4.0 * (getMiningLVL() - oldMining));
		stats.add(PlayerStatType.STRENGTH, 2.0 * (getCombatLvl() - oldCombat));
	}

	/** Load the new XP-only format without replaying saved rewards. */
	public void restoreSkillXp(long miningXp, long combatXp) {
		SkillXpCurve.levelForXp(miningXp);
		SkillXpCurve.levelForXp(combatXp);
		this.miningXP = Math.min(miningXp, SkillXpCurve.totalXpForLevel(100));
		this.combatXP = Math.min(combatXp, SkillXpCurve.totalXpForLevel(100));
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
		// Skill rewards are earned progression, not removable equipment bonuses.
		stats.add(PlayerStatType.MINING_FORTUNE, 4.0 * getMiningLVL());
		stats.add(PlayerStatType.STRENGTH, 2.0 * getCombatLvl());
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

