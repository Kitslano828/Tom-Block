package org.tomdang.customitemframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.EnumMap;
import java.util.Map;

public class CustomItemStatConfigurationLoader {

	public CustomItemStatModifiers load(ConfigurationSection itemSection, String itemId) {
		if (itemSection == null) throw new IllegalArgumentException("itemSection cannot be null");
		if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId cannot be null or blank");
		if (!itemSection.isSet("stats")) return CustomItemStatModifiers.empty();

		ConfigurationSection statsSection = itemSection.getConfigurationSection("stats");
		if (statsSection == null) {
			throw new IllegalArgumentException("Item " + itemId + " has an invalid stats section");
		}

		Map<PlayerStatType, Double> stats = new EnumMap<>(PlayerStatType.class);
		for (String key : statsSection.getKeys(false)) {
			PlayerStatType statType = findStatType(key, itemId);
			if (!statsSection.isDouble(key) && !statsSection.isInt(key) && !statsSection.isLong(key)) {
				throw new IllegalArgumentException("Item " + itemId + " has a non-numeric stat " + key);
			}

			double amount = statsSection.getDouble(key);
			if (!Double.isFinite(amount)) {
				throw new IllegalArgumentException("Item " + itemId + " has a non-finite stat " + key);
			}
			stats.put(statType, amount);
		}

		return new CustomItemStatModifiers(stats);
	}

	public double requireNonNegative(CustomItemStatModifiers stats, PlayerStatType statType, String itemId) {
		if (stats == null) throw new IllegalArgumentException("stats cannot be null");
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (!stats.asMap().containsKey(statType)) {
			throw new IllegalArgumentException("Item " + itemId + " is missing required stat " + statType.getStorageKey());
		}
		double amount = stats.get(statType);
		if (amount < 0) {
			throw new IllegalArgumentException("Item " + itemId + " has " + statType.getStorageKey() + " lesser than 0");
		}
		return amount;
	}

	private PlayerStatType findStatType(String storageKey, String itemId) {
		for (PlayerStatType statType : PlayerStatType.values()) {
			if (statType.getStorageKey().equalsIgnoreCase(storageKey)) return statType;
		}
		throw new IllegalArgumentException("Item " + itemId + " has unknown stat " + storageKey);
	}
}
