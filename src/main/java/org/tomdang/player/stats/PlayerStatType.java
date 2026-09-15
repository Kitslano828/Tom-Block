package org.tomdang.player.stats;

import lombok.Getter;

public enum PlayerStatType {

	// Combat Stats
	MAX_HEALTH("maxHealth", "Maximum Health", PlayerStatCategory.COMBAT, 100.0, 1.0),
	MAX_ENERGY("maxEnergy", "Maximum Energy", PlayerStatCategory.UTILITY, 100.0, 0.0),
	DEFENSE("defense", "Defense", PlayerStatCategory.COMBAT, 0.0, 0.0),
	STRENGTH("strength", "Strength", PlayerStatCategory.COMBAT, 0.0, 0.0),
	DAMAGE("damage", "Damage", PlayerStatCategory.COMBAT, 0.0, 0.0),

	// Mining Stats
	MINING_FORTUNE("mining-fortune", "Mining Fortune", PlayerStatCategory.MINING, 0.0, 0.0),
	MINING_SPEED("mining-speed", "Mining Speed", PlayerStatCategory.MINING, 0.0, 0.0);

	@Getter
	private final String storageKey;
	@Getter
	private final String displayName;
	@Getter
	private final PlayerStatCategory category;
	@Getter
	private final double defaultValue;
	@Getter
	private final double minimumValue;

	PlayerStatType(String storageKey, String displayName, PlayerStatCategory category, double defaultValue, double minimumValue) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new IllegalArgumentException("storageKey cannot be null or blank");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("displayName cannot be null or blank");
		}
		if (category == null) {
			throw new IllegalArgumentException("category cannot be null");
		}
		if (!Double.isFinite(defaultValue)) {
			throw new IllegalArgumentException("defaultValue must be finite");
		}
		if (!Double.isFinite(minimumValue)) {
			throw new IllegalArgumentException("minimumValue must be finite");
		}
		if (defaultValue < minimumValue) {
			throw new IllegalArgumentException("defaultValue (" + defaultValue + ") cannot be less than minimumValue (" + minimumValue + ")");
		}

		this.storageKey = storageKey;
		this.displayName = displayName;
		this.category = category;
		this.defaultValue = defaultValue;
		this.minimumValue = minimumValue;
	}

}
