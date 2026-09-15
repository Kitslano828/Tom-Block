package org.tomdang.player.stats.modifier;

import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;

public class PlayerStatModifier {

	private final PlayerStatType statType;
	private final String sourceId;
	private final PlayerStatContributionSource source;
	private final String displayName;
	private final double amount;

	public PlayerStatModifier(PlayerStatType statType, String sourceId, double amount) {
		this(statType, sourceId, PlayerStatContributionSource.OTHER, sourceId, amount);
	}

	public PlayerStatModifier(PlayerStatType statType, String sourceId,
	                          PlayerStatContributionSource source, String displayName, double amount) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (sourceId == null || sourceId.isBlank()) {
			throw new IllegalArgumentException("sourceId cannot be null or blank");
		}
		if (!Double.isFinite(amount)) throw new IllegalArgumentException("amount must be finite");
		if (source == null) throw new IllegalArgumentException("source cannot be null");
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("displayName cannot be null or blank");
		}

		this.statType = statType;
		this.sourceId = sourceId;
		this.source = source;
		this.displayName = displayName;
		this.amount = amount;
	}

	public PlayerStatType getStatType() {
		return statType;
	}

	public String getSourceId() {
		return sourceId;
	}

	public double getAmount() {
		return amount;
	}

	public PlayerStatContributionSource getSource() {
		return source;
	}

	public String getDisplayName() {
		return displayName;
	}

}
