package org.tomdang.player.stats.rule;

import lombok.Getter;
import org.tomdang.player.stats.PlayerStatType;

import java.util.OptionalDouble;

public class PlayerStatRule {

	@Getter
	private final PlayerStatType statType;
	@Getter
	private final OptionalDouble cap;

	public PlayerStatRule(PlayerStatType statType, OptionalDouble cap) {
		if (statType == null) {
			throw new IllegalArgumentException("statType cannot be null");
		}g
		if (cap == null) {
			throw new IllegalArgumentException("cap OptionalDouble cannot be null");
		}

		if (cap.isPresent()) {
			double capValue = cap.getAsDouble();

			if (!Double.isFinite(capValue)) {
				throw new IllegalArgumentException("Cap value must be finite");
			}
			if (capValue < statType.getMinimumValue()) {
				throw new IllegalArgumentException(
						String.format("Cap (%f) cannot be less than stat minimum value (%f)",
								capValue, statType.getMinimumValue())
				);
			}
		}

		this.statType = statType;
		this.cap = cap;
	}
}
