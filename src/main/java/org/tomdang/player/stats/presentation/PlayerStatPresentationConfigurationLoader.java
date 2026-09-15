package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.stats.PlayerStatType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlayerStatPresentationConfigurationLoader {

	public List<PlayerStatPresentation> load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection root = requireSection(configuration, "stat-presentations", "Root configuration");
		if (root.getKeys(false).isEmpty()) {
			throw new IllegalArgumentException("stat-presentations section cannot be empty");
		}

		List<PlayerStatPresentation> presentations = new ArrayList<>();
		Set<PlayerStatType> encountered = EnumSet.noneOf(PlayerStatType.class);
		for (String configuredId : root.getKeys(false)) {
			PlayerStatType statType = parseStatType(configuredId);
			if (!encountered.add(statType)) {
				throw new IllegalArgumentException("Duplicate stat presentation for " + statType.name());
			}

			String context = "Stat presentation " + configuredId;
			ConfigurationSection section = requireSection(root, configuredId, context);
			String symbol = requireString(section, "symbol", context);
			TextColor color = parseColor(requireString(section, "color", context), context);
			String description = requireString(section, "description", context);
			Material material = parseMaterial(requireString(section, "material", context), context);
			boolean visible = optionalBoolean(section, "visible", true, context);

			presentations.add(new PlayerStatPresentation(
					statType, symbol, color, description, material, visible
			));
		}

		Set<PlayerStatType> missing = EnumSet.allOf(PlayerStatType.class);
		missing.removeAll(encountered);
		if (!missing.isEmpty()) {
			throw new IllegalArgumentException("Missing stat presentations: " + missing);
		}
		return List.copyOf(presentations);
	}

	private PlayerStatType parseStatType(String configuredId) {
		try {
			return PlayerStatType.valueOf(configuredId.toUpperCase(Locale.ROOT).replace('-', '_'));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Unknown stat presentation ID: " + configuredId, exception);
		}
	}

	private TextColor parseColor(String value, String context) {
		if (!value.matches("^#[0-9a-fA-F]{6}$")) {
			throw new IllegalArgumentException(context + " has an invalid color: " + value + " (expected #RRGGBB)");
		}
		TextColor color = TextColor.fromHexString(value);
		if (color == null) throw new IllegalArgumentException(context + " has an invalid color: " + value);
		return color;
	}

	private Material parseMaterial(String value, String context) {
		Material material = Material.matchMaterial(value.toUpperCase(Locale.ROOT));
		if (material == null || material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR) {
			throw new IllegalArgumentException(context + " has an invalid material: " + value);
		}
		return material;
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String path, String context) {
		if (!parent.isConfigurationSection(path)) {
			throw new IllegalArgumentException(context + " has a missing or invalid section path: " + path);
		}
		return parent.getConfigurationSection(path);
	}

	private String requireString(ConfigurationSection section, String path, String context) {
		String value = section.getString(path);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(context + " has a missing or blank " + path);
		}
		return value;
	}

	private boolean optionalBoolean(ConfigurationSection section, String path, boolean defaultValue, String context) {
		if (!section.contains(path)) return defaultValue;
		if (!section.isBoolean(path)) {
			throw new IllegalArgumentException(context + " has an invalid " + path + ": expected true or false");
		}
		return section.getBoolean(path);
	}
}
