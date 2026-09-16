package org.tomdang.player.stats.evaluation;

import org.tomdang.player.stats.PlayerStatType;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Optional;
import org.tomdang.player.stats.cap.LocationStatCap;

public record PlayerStatBreakdown(
		PlayerStatType statType,
		double baseValue,
		List<PlayerStatContribution> contributions,
		double rawValue,
		double effectiveValue,
		OptionalDouble configuredCap,
		OptionalDouble cap,
		double capModifierTotal,
		Optional<LocationStatCap> locationCap
) {
	public PlayerStatBreakdown(PlayerStatType statType, double baseValue, List<PlayerStatContribution> contributions,
			double rawValue, double effectiveValue, OptionalDouble configuredCap, OptionalDouble cap,
			double capModifierTotal) {
		this(statType, baseValue, contributions, rawValue, effectiveValue, configuredCap, cap,
				capModifierTotal, Optional.empty());
	}

	public PlayerStatBreakdown(PlayerStatType statType, double baseValue,
	                           List<PlayerStatContribution> contributions, double effectiveValue) {
		this(statType, baseValue, contributions, effectiveValue, effectiveValue,
				OptionalDouble.empty(), OptionalDouble.empty(), 0);
	}

	public PlayerStatBreakdown(PlayerStatType statType, double baseValue,
	                           List<PlayerStatContribution> contributions, double rawValue,
	                           double effectiveValue, OptionalDouble cap) {
		this(statType, baseValue, contributions, rawValue, effectiveValue, cap, cap, 0);
	}

	public PlayerStatBreakdown(PlayerStatType statType, double baseValue,
	                           List<PlayerStatContribution> contributions, PlayerStatCalculation calculation) {
		this(
				statType,
				baseValue,
				contributions,
				requireMatchingCalculation(statType, calculation).rawValue(),
				calculation.effectiveValue(),
				calculation.configuredCap(),
				calculation.cap(),
				calculation.capModifierTotal()
		);
	}

	public PlayerStatBreakdown(PlayerStatType statType, double baseValue,
			List<PlayerStatContribution> contributions, PlayerStatCalculation calculation,
			Optional<LocationStatCap> locationCap) {
		this(statType, baseValue, contributions, calculation.rawValue(), calculation.effectiveValue(),
				calculation.configuredCap(), calculation.cap(), calculation.capModifierTotal(), locationCap);
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
		if (configuredCap == null) throw new IllegalArgumentException("configuredCap cannot be null");
		if (cap == null) throw new IllegalArgumentException("cap cannot be null");
		if (configuredCap.isPresent() && !Double.isFinite(configuredCap.getAsDouble())) {
			throw new IllegalArgumentException("configuredCap must be finite when present");
		}
		if (cap.isPresent() && !Double.isFinite(cap.getAsDouble())) {
			throw new IllegalArgumentException("cap must be finite when present");
		}
		if (!Double.isFinite(capModifierTotal)) throw new IllegalArgumentException("capModifierTotal must be finite");
		if (locationCap == null) throw new IllegalArgumentException("locationCap cannot be null");
		locationCap.ifPresent(value -> {
			if (value.statType() != statType) throw new IllegalArgumentException("locationCap stat mismatch");
		});
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
