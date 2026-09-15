package org.tomdang.player.stats.evaluation;

import org.tomdang.player.stats.PlayerStatType;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

public record PlayerStatBreakdown(
		PlayerStatType statType,
		double baseValue,
		List<PlayerStatContribution> contributions,
		double rawValue,
		double effectiveValue,
		OptionalDouble cap
) {
	public PlayerStatBreakdown(PlayerStatType statType, double baseValue,
	                           List<PlayerStatContribution> contributions, double effectiveValue) {
		this(statType, baseValue, contributions, effectiveValue, effectiveValue, OptionalDouble.empty());
	}

	public PlayerStatBreakdown(PlayerStatType statType, double baseValue,
	                           List<PlayerStatContribution> contributions, PlayerStatCalculation calculation) {
		this(
				statType,
				baseValue,
				contributions,
				requireMatchingCalculation(statType, calculation).rawValue(),
				calculation.effectiveValue(),
				calculation.cap()
		);
	}

	public PlayerStatBreakdown {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (!Double.isFinite(baseValue)) throw new IllegalArgumentException("baseValue must be finite");
		if (contributions == null) throw new IllegalArgumentException("contributions cannot be null");
		if (contributions.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("contributions cannot contain null elements");
		}
		if (contributions.stream().anyMatch(contribution -> contribution.statType() != statType)) {
			throw new IllegalArgumentException("every contribution must match the breakdown statType");
		}
		if (!Double.isFinite(rawValue)) throw new IllegalArgumentException("rawValue must be finite");
		if (!Double.isFinite(effectiveValue)) throw new IllegalArgumentException("effectiveValue must be finite");
		if (cap == null) throw new IllegalArgumentException("cap cannot be null");
		if (cap.isPresent() && !Double.isFinite(cap.getAsDouble())) {
			throw new IllegalArgumentException("cap must be finite when present");
		}
		contributions = List.copyOf(contributions);
	}

	public boolean capped() {
		return cap.isPresent() && effectiveValue >= cap.getAsDouble() && rawValue > effectiveValue;
	}

	private static PlayerStatCalculation requireMatchingCalculation(
			PlayerStatType statType, PlayerStatCalculation calculation) {
		if (calculation == null) throw new IllegalArgumentException("calculation cannot be null");
		if (calculation.statType() != statType) {
			throw new IllegalArgumentException("calculation statType must match the breakdown statType");
		}
		return calculation;
	}
}
