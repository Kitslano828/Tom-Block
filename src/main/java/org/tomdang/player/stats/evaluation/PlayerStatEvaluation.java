package org.tomdang.player.stats.evaluation;

import org.tomdang.player.stats.PlayerStatSnapshot;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class PlayerStatEvaluation {

	private final Map<PlayerStatType, PlayerStatBreakdown> breakdowns;
	private final PlayerStatSnapshot snapshot;

	public PlayerStatEvaluation(Map<PlayerStatType, PlayerStatBreakdown> breakdowns) {
		if (breakdowns == null) throw new IllegalArgumentException("breakdowns cannot be null");

		EnumMap<PlayerStatType, PlayerStatBreakdown> breakdownCopy = new EnumMap<>(PlayerStatType.class);
		EnumMap<PlayerStatType, Double> effectiveValues = new EnumMap<>(PlayerStatType.class);
		for (PlayerStatType statType : PlayerStatType.values()) {
			PlayerStatBreakdown breakdown = breakdowns.get(statType);
			if (breakdown == null) {
				throw new IllegalArgumentException("missing breakdown for " + statType.name());
			}
			if (breakdown.statType() != statType) {
				throw new IllegalArgumentException("breakdown key does not match its statType");
			}
			breakdownCopy.put(statType, breakdown);
			effectiveValues.put(statType, breakdown.effectiveValue());
		}
		if (breakdowns.size() != PlayerStatType.values().length) {
			throw new IllegalArgumentException("breakdowns contains unsupported stat keys");
		}

		this.breakdowns = Collections.unmodifiableMap(breakdownCopy);
		this.snapshot = new PlayerStatSnapshot(effectiveValues);
	}

	public PlayerStatBreakdown getBreakdown(PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		return breakdowns.get(statType);
	}

	public Map<PlayerStatType, PlayerStatBreakdown> getBreakdowns() {
		return breakdowns;
	}

	public PlayerStatSnapshot getSnapshot() {
		return snapshot;
	}
}
