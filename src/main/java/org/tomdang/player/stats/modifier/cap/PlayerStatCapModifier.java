package org.tomdang.player.stats.modifier.cap;

import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;

public record PlayerStatCapModifier(
		PlayerStatType statType,
		String sourceId,
		PlayerStatContributionSource source,
		String displayName,
		double amount
) {
	public PlayerStatCapModifier(PlayerStatType statType, String sourceId, double amount) {
		this(statType, sourceId, PlayerStatContributionSource.OTHER, sourceId, amount);
	}

	public PlayerStatCapModifier {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (sourceId == null || sourceId.isBlank()) throw new IllegalArgumentException("sourceId cannot be null or blank");
		if (source == null) throw new IllegalArgumentException("source cannot be null");
		if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName cannot be null or blank");
		if (!Double.isFinite(amount)) throw new IllegalArgumentException("amount must be finite");
	}
}
