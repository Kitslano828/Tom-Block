package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;
import java.util.Locale;

public class PlayerStatsCategoryMenuConfigurationLoader {
	public PlayerStatsCategoryMenuConfiguration load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		ConfigurationSection root = requireSection(YamlConfiguration.loadConfiguration(reader), "category-menu", "Root");
		ConfigurationSection back = requireSection(root, "back-button", "Category menu");
		ConfigurationSection close = requireSection(root, "close-button", "Category menu");
		return new PlayerStatsCategoryMenuConfiguration(
				requireText(root, "title-format", "Category menu"), requireInt(root, "size", "Category menu"),
				material(back, "material", "Back button"), requireText(back, "name", "Back button"), color(back, "color", "Back button"), requireInt(back, "slot", "Back button"),
				material(close, "material", "Close button"), requireText(close, "name", "Close button"), color(close, "color", "Close button"), requireInt(close, "slot", "Close button")
		);
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String key, String context) {
		if (!parent.isConfigurationSection(key)) throw new IllegalArgumentException(context + " has a missing or invalid section: " + key);
		return parent.getConfigurationSection(key);
	}
	private String requireText(ConfigurationSection section, String key, String context) {
		String value = section.getString(key);
		if (value == null || value.isBlank()) throw new IllegalArgumentException(context + " has a missing or blank " + key);
		return value;
	}
	private int requireInt(ConfigurationSection section, String key, String context) {
		if (!section.isInt(key)) throw new IllegalArgumentException(context + " has an invalid integer: " + key);
		return section.getInt(key);
	}
	private TextColor color(ConfigurationSection section, String key, String context) {
		String value = requireText(section, key, context);
		if (!value.matches("^#[0-9a-fA-F]{6}$")) throw new IllegalArgumentException(context + " has an invalid color: " + value);
		return TextColor.fromHexString(value);
	}
	private Material material(ConfigurationSection section, String key, String context) {
		String value = requireText(section, key, context);
		Material result = Material.matchMaterial(value.toUpperCase(Locale.ROOT));
		if (result == null || result == Material.AIR || result == Material.CAVE_AIR || result == Material.VOID_AIR) throw new IllegalArgumentException(context + " has an invalid material: " + value);
		return result;
	}
}
