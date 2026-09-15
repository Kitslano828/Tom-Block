package org.tomdang.customitemframework.stats;

import org.tomdang.player.stats.PlayerStatType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class CustomItemStatCapModifiers {
	private final Map<PlayerStatType, Double> values;

	public CustomItemStatCapModifiers(Map<PlayerStatType, Double> values) {
		if (values == null) throw new IllegalArgumentException("values cannot be null");
		EnumMap<PlayerStatType, Double> copy = new EnumMap<>(PlayerStatType.class);
		for (Map.Entry<PlayerStatType, Double> entry : values.entrySet()) {
			if (entry.getKey() == null) throw new IllegalArgumentException("values cannot contain a null stat type");
			if (entry.getValue() == null || !Double.isFinite(entry.getValue())) {
				throw new IllegalArgumentException("cap modifier amounts must be finite and non-null");
			}
			copy.put(entry.getKey(), entry.getValue());
		}
		this.values = Collections.unmodifiableMap(copy);
	}

	public static CustomItemStatCapModifiers empty() {
		return new CustomItemStatCapModifiers(Map.of());
	}

	public Map<PlayerStatType, Double> asMap() {
		return values;
	}
}
