package org.tomdang.player.stats.evaluation;

import org.tomdang.player.stats.PlayerStatType;

public record PlayerStatContribution(
		PlayerStatType statType,
		PlayerStatContributionSource source,
		String sourceId,
		String displayName,
		double amount
) {
	public PlayerStatContribution {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (source == null) throw new IllegalArgumentException("source cannot be null");
		if (sourceId == null || sourceId.isBlank()) {
			throw new IllegalArgumentException("sourceId cannot be null or blank");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("displayName cannot be null or blank");
		}
		if (!Double.isFinite(amount)) throw new IllegalArgumentException("amount must be finite");
	}
}
