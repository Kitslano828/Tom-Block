package org.tomdang.player;



import lombok.Getter;
import lombok.Setter;
import org.tomdang.player.playerresource.PlayerResource;
import org.tomdang.player.stats.PlayerStatBlock;
import org.tomdang.player.stats.PlayerStatType;

import java.util.UUID;

public class PlayerProfile {

	@Getter
	private final UUID uuid;

	@Getter @Setter
	private int miningXP = 0;
	@Getter @Setter
	private int miningLVL = 1;
	@Getter @Setter
	private int combatXP = 0;
	@Getter @Setter
	private int combatLvl = 1;

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
		this.miningXP += amount;
	}

	public void increaseCombatXP(int amount) {
		this.combatXP += amount;
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
		this.miningLVL += amount;
	}

	public boolean isDead() {
		return health.getCurrent() <= 0.0;
	}

	public void reduceDefense(double amount) {
		stats.add(PlayerStatType.DEFENSE, -amount);
	}
}

