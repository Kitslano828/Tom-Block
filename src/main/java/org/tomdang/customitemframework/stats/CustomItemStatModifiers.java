package org.tomdang.customitemframework.stats;

import org.tomdang.player.stats.PlayerStatType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class CustomItemStatModifiers {

	private final Map<PlayerStatType, Double> values;

	public CustomItemStatModifiers(Map<PlayerStatType, Double> values) {
		if (values == null) throw new IllegalArgumentException("values cannot be null");

		EnumMap<PlayerStatType, Double> copy = new EnumMap<>(PlayerStatType.class);
		for (Map.Entry<PlayerStatType, Double> entry : values.entrySet()) {
			PlayerStatType statType = entry.getKey();
			Double amount = entry.getValue();
			if (statType == null) throw new IllegalArgumentException("values cannot contain a null stat type");
			if (amount == null) throw new IllegalArgumentException("values cannot contain a null amount");
			if (!Double.isFinite(amount)) throw new IllegalArgumentException("stat modifier amount must be finite");
			copy.put(statType, amount);
		}

		this.values = Collections.unmodifiableMap(copy);
	}

	public static CustomItemStatModifiers empty() {
		return new CustomItemStatModifiers(Map.of());
	}

	public double get(PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		return values.getOrDefault(statType, 0.0);
	}

	public Map<PlayerStatType, Double> asMap() {
		return values;
	}
}
