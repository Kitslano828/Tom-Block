package org.tomdang.customitemframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.EnumMap;
import java.util.Map;

public class CustomItemStatCapConfigurationLoader {
	public CustomItemStatCapModifiers load(ConfigurationSection itemSection, String itemId) {
		if (itemSection == null) throw new IllegalArgumentException("itemSection cannot be null");
		if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId cannot be null or blank");
		if (!itemSection.isSet("stat-cap-modifiers")) return CustomItemStatCapModifiers.empty();

		ConfigurationSection section = itemSection.getConfigurationSection("stat-cap-modifiers");
		if (section == null) throw new IllegalArgumentException("Item " + itemId + " has an invalid stat-cap-modifiers section");
		Map<PlayerStatType, Double> modifiers = new EnumMap<>(PlayerStatType.class);
		for (String key : section.getKeys(false)) {
			PlayerStatType statType = findStatType(key, itemId);
			if (!section.isDouble(key) && !section.isInt(key) && !section.isLong(key)) {
				throw new IllegalArgumentException("Item " + itemId + " has a non-numeric cap modifier " + key);
			}
			double amount = section.getDouble(key);
			if (!Double.isFinite(amount)) throw new IllegalArgumentException("Item " + itemId + " has a non-finite cap modifier " + key);
			modifiers.put(statType, amount);
		}
		return new CustomItemStatCapModifiers(modifiers);
	}

	private PlayerStatType findStatType(String storageKey, String itemId) {
		for (PlayerStatType statType : PlayerStatType.values()) {
			if (statType.getStorageKey().equalsIgnoreCase(storageKey)) return statType;
		}
		throw new IllegalArgumentException("Item " + itemId + " has unknown cap-modified stat " + storageKey);
	}
}
