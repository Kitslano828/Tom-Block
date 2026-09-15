package org.tomdang.player.stats;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class PlayerStatSnapshot {

	private final Map<PlayerStatType, Double> values;

	public PlayerStatSnapshot(Map<PlayerStatType, Double> values) {
		if (values == null) throw new IllegalArgumentException("values cannot be null");

		EnumMap<PlayerStatType, Double> copy = new EnumMap<>(PlayerStatType.class);
		for (Map.Entry<PlayerStatType, Double> entry : values.entrySet()) {
			if (entry.getKey() == null) throw new IllegalArgumentException("values cannot contain a null stat type");
			if (entry.getValue() == null) throw new IllegalArgumentException("values cannot contain a null value");
			if (!Double.isFinite(entry.getValue())) {
				throw new IllegalArgumentException("stat values must be finite");
			}
			copy.put(entry.getKey(), entry.getValue());
		}

		this.values = Collections.unmodifiableMap(copy);
	}

	public static PlayerStatSnapshot defaults() {
		return new PlayerStatSnapshot(Map.of());
	}

	public double get(PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		return values.getOrDefault(statType, statType.getDefaultValue());
	}

	public Map<PlayerStatType, Double> asMap() {
		return values;
	}
}
