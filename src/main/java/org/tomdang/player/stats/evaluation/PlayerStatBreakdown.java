package org.tomdang.player.stats.evaluation;

import org.tomdang.player.stats.PlayerStatType;

import java.util.List;
import java.util.Objects;

public record PlayerStatBreakdown(
		PlayerStatType statType,
		double baseValue,
		List<PlayerStatContribution> contributions,
		double effectiveValue
) {
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
		if (!Double.isFinite(effectiveValue)) throw new IllegalArgumentException("effectiveValue must be finite");
		contributions = List.copyOf(contributions);
	}
}
