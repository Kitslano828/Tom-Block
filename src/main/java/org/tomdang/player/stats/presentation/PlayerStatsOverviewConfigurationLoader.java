package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.stats.PlayerStatCategory;

import java.io.Reader;
import java.util.*;

public class PlayerStatsOverviewConfigurationLoader {
	public PlayerStatsOverviewConfiguration load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		ConfigurationSection root = section(YamlConfiguration.loadConfiguration(reader), "stats-overview", "Root");
		String title = text(root, "title", "Stats overview");
		int size = integer(root, "size", "Stats overview");
		ConfigurationSection close = section(root, "close-button", "Stats overview");
		Material closeMaterial = material(text(close, "material", "Close button"), "Close button");
		String closeName = text(close, "name", "Close button");
		TextColor closeColor = color(text(close, "color", "Close button"), "Close button");
		int closeSlot = integer(close, "slot", "Close button");

		ConfigurationSection configuredCategories = section(root, "categories", "Stats overview");
		List<PlayerStatCategoryPresentation> categories = new ArrayList<>();
		Set<PlayerStatCategory> seen = EnumSet.noneOf(PlayerStatCategory.class);
		Set<Integer> occupiedSlots = new HashSet<>();
		occupiedSlots.add(closeSlot);
		for (String id : configuredCategories.getKeys(false)) {
			PlayerStatCategory category = enumValue(id, PlayerStatCategory.class, "category");
			if (!seen.add(category)) throw new IllegalArgumentException("Duplicate category: " + category);
			String context = "Category " + id;
			ConfigurationSection entry = section(configuredCategories, id, context);
			int slot = integer(entry, "slot", context);
			if (slot < 0 || slot >= size) throw new IllegalArgumentException(context + " slot is outside the inventory");
			if (!occupiedSlots.add(slot)) throw new IllegalArgumentException("Menu slot " + slot + " is used more than once");
			categories.add(new PlayerStatCategoryPresentation(category, text(entry, "name", context),
					color(text(entry, "color", context), context), text(entry, "description", context),
					material(text(entry, "material", context), context), slot, optionalBoolean(entry, "visible", true, context)));
		}
		Set<PlayerStatCategory> missing = EnumSet.allOf(PlayerStatCategory.class);
		missing.removeAll(seen);
		if (!missing.isEmpty()) throw new IllegalArgumentException("Missing category presentations: " + missing);
		return new PlayerStatsOverviewConfiguration(title, size, closeMaterial, closeName, closeColor, closeSlot, categories);
	}

	private ConfigurationSection section(ConfigurationSection parent, String key, String context) {
		if (!parent.isConfigurationSection(key)) throw new IllegalArgumentException(context + " has a missing or invalid section: " + key);
		return parent.getConfigurationSection(key);
	}
	private String text(ConfigurationSection section, String key, String context) {
		String value = section.getString(key);
		if (value == null || value.isBlank()) throw new IllegalArgumentException(context + " has a missing or blank " + key);
		return value;
	}
	private int integer(ConfigurationSection section, String key, String context) {
		if (!section.isInt(key)) throw new IllegalArgumentException(context + " has an invalid integer: " + key);
		return section.getInt(key);
	}
	private boolean optionalBoolean(ConfigurationSection section, String key, boolean fallback, String context) {
		if (!section.contains(key)) return fallback;
		if (!section.isBoolean(key)) throw new IllegalArgumentException(context + " has an invalid boolean: " + key);
		return section.getBoolean(key);
	}
	private TextColor color(String value, String context) {
		if (!value.matches("^#[0-9a-fA-F]{6}$")) throw new IllegalArgumentException(context + " has an invalid color: " + value);
		TextColor parsed = TextColor.fromHexString(value);
		if (parsed == null) throw new IllegalArgumentException(context + " has an invalid color: " + value);
		return parsed;
	}
	private Material material(String value, String context) {
		Material parsed = Material.matchMaterial(value.toUpperCase(Locale.ROOT));
		if (parsed == null || parsed == Material.AIR || parsed == Material.CAVE_AIR || parsed == Material.VOID_AIR)
			throw new IllegalArgumentException(context + " has an invalid material: " + value);
		return parsed;
	}
	private <E extends Enum<E>> E enumValue(String value, Class<E> type, String context) {
		try { return Enum.valueOf(type, value.toUpperCase(Locale.ROOT).replace('-', '_')); }
		catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unknown " + context + ": " + value, exception); }
	}
}
