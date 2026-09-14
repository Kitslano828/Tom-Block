package org.tomdang.player.stats;

import java.util.EnumMap;
import java.util.Map;

public class PlayerStatBlock {

	private final Map<PlayerStatType, Double> stats;

	public PlayerStatBlock() {
		this.stats = new EnumMap<>(PlayerStatType.class);
		for (PlayerStatType statType : PlayerStatType.values()) {
			this.stats.put(statType, statType.getDefaultValue());
		}
	}

	public double get(PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("StatType cannot be null");
		Double value = stats.get(statType);
		return (value != null) ? value : statType.getDefaultValue();
	}

	public void set(PlayerStatType statType, double amount) {
		if (statType == null) throw new IllegalArgumentException("StatType cannot be null");
		if (!Double.isFinite(amount)) throw new IllegalArgumentException("amount has to be finite");

		if (amount < statType.getMinimumValue()) {
			stats.put(statType, statType.getMinimumValue());
		} else {
			stats.put(statType, amount);
		}
	}

	public void add(PlayerStatType statType, double amount) {
		if (statType == null) throw new IllegalArgumentException("StatType cannot be null");
		if (!Double.isFinite(amount)) throw new IllegalArgumentException("amount has to be finite");

		double currentAmount = get(statType);
		set(statType, currentAmount + amount);
	}

	public void reset(PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("StatType cannot be null");
		set(statType, statType.getDefaultValue());
	}

	public void resetAll() {
		for (PlayerStatType statType : PlayerStatType.values()) {
			set(statType, statType.getDefaultValue());
		}
	}
}
