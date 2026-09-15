package org.tomdang.player.stats.evaluation;

import org.tomdang.player.stats.PlayerStatType;

import java.util.OptionalDouble;

public record PlayerStatCalculation(
		PlayerStatType statType,
		double rawValue,
		double effectiveValue,
		OptionalDouble cap
) {
	public PlayerStatCalculation {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (!Double.isFinite(rawValue)) throw new IllegalArgumentException("rawValue must be finite");
		if (!Double.isFinite(effectiveValue)) throw new IllegalArgumentException("effectiveValue must be finite");
		if (cap == null) throw new IllegalArgumentException("cap cannot be null");
		if (cap.isPresent() && !Double.isFinite(cap.getAsDouble())) {
			throw new IllegalArgumentException("cap must be finite when present");
		}
	}

	public boolean capped() {
		return cap.isPresent() && effectiveValue >= cap.getAsDouble() && rawValue > effectiveValue;
	}
}
