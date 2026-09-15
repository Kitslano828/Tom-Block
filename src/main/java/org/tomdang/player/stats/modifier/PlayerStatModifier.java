package org.tomdang.player.stats.modifier;

import org.tomdang.player.stats.PlayerStatType;

public class PlayerStatModifier {

	private final PlayerStatType statType;
	private final String sourceId;
	private final double amount;

	public PlayerStatModifier(PlayerStatType statType, String sourceId, double amount) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (sourceId == null || sourceId.isBlank()) {
			throw new IllegalArgumentException("sourceId cannot be null or blank");
		}
		if (!Double.isFinite(amount)) throw new IllegalArgumentException("amount must be finite");

		this.statType = statType;
		this.sourceId = sourceId;
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

}
