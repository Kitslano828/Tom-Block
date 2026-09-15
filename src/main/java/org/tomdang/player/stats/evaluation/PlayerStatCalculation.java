package org.tomdang.player.stats.evaluation;

import org.tomdang.player.stats.PlayerStatType;

import java.util.OptionalDouble;

public record PlayerStatCalculation(
		PlayerStatType statType,
		double rawValue,
		double effectiveValue,
		OptionalDouble configuredCap,
		OptionalDouble cap,
		double capModifierTotal
) {
	public PlayerStatCalculation(PlayerStatType statType, double rawValue, double effectiveValue, OptionalDouble cap) {
		this(statType, rawValue, effectiveValue, cap, cap, 0);
	}

	public PlayerStatCalculation {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (!Double.isFinite(rawValue)) throw new IllegalArgumentException("rawValue must be finite");
		if (!Double.isFinite(effectiveValue)) throw new IllegalArgumentException("effectiveValue must be finite");
		if (configuredCap == null) throw new IllegalArgumentException("configuredCap cannot be null");
		if (cap == null) throw new IllegalArgumentException("cap cannot be null");
		if (configuredCap.isPresent() && !Double.isFinite(configuredCap.getAsDouble())) {
			throw new IllegalArgumentException("configuredCap must be finite when present");
		}
		if (cap.isPresent() && !Double.isFinite(cap.getAsDouble())) {
			throw new IllegalArgumentException("cap must be finite when present");
		}
		if (!Double.isFinite(capModifierTotal)) throw new IllegalArgumentException("capModifierTotal must be finite");
	}

	public boolean capped() {
		return cap.isPresent() && effectiveValue >= cap.getAsDouble() && rawValue > effectiveValue;
	}
}
