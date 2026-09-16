package org.tomdang.player.stats.cap;

import org.tomdang.player.stats.PlayerStatType;

public record LocationStatCap(String locationId, PlayerStatType statType, double value) {
	public LocationStatCap {
		if (locationId == null || locationId.isBlank()) throw new IllegalArgumentException("locationId is required");
		if (statType == null) throw new IllegalArgumentException("statType is required");
		if (!Double.isFinite(value) || value < statType.getMinimumValue())
			throw new IllegalArgumentException("Invalid location cap for " + statType);
	}
}
