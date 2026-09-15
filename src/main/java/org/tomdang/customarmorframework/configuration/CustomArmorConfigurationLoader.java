package org.tomdang.customarmorframework.configuration;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.customarmorframework.ArmorSlot;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.configuration.CustomItemStatConfigurationLoader;
import org.tomdang.customitemframework.configuration.CustomItemStatCapConfigurationLoader;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CustomArmorConfigurationLoader {

	private final CustomItemStatConfigurationLoader statLoader = new CustomItemStatConfigurationLoader();
	private final CustomItemStatCapConfigurationLoader capLoader = new CustomItemStatCapConfigurationLoader();

	public List<CustomArmorDefinition> loadDefinitions(File file) {
		if (file == null) throw new IllegalArgumentException("file cannot be null");
		return loadDefinitions(YamlConfiguration.loadConfiguration(file));
	}

	public List<CustomArmorDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		return loadDefinitions(YamlConfiguration.loadConfiguration(reader));
	}

	private List<CustomArmorDefinition> loadDefinitions(YamlConfiguration data) {
		ConfigurationSection armorSection = data.getConfigurationSection("armor");
		if (armorSection == null) {
			throw new IllegalArgumentException("armor.yml does not contain the required armor root section");
		}

		List<CustomArmorDefinition> definitions = new ArrayList<>();
		for (String id : armorSection.getKeys(false)) {
			if (id == null || id.isBlank()) throw new IllegalArgumentException("armor ID cannot be null or blank");
			ConfigurationSection section = armorSection.getConfigurationSection(id);
			if (section == null) throw new IllegalArgumentException("Armor " + id + " must be a configuration section");

			Material material = parseEnum(Material.class, requireText(section, id, "material"), id, "material");
			String displayName = requireText(section, id, "display-name");
			Rarity rarity = parseEnum(Rarity.class, requireText(section, id, "rarity"), id, "rarity");
			ArmorSlot slot = parseEnum(ArmorSlot.class, requireText(section, id, "slot"), id, "slot");
			Color color = parseColor(requireText(section, id, "color"), id);
			List<String> abilityIDs = requireAbilityIDs(section, id);
			String armorSetId = optionalId(section, id, "set-id");

			definitions.add(new CustomArmorDefinition(
					id, material, displayName, rarity, slot, color,
					statLoader.load(section, id), capLoader.load(section, id), abilityIDs, armorSetId
			));
		}
		return List.copyOf(definitions);
	}

	private List<String> requireAbilityIDs(ConfigurationSection section, String id) {
		if (!section.isList("abilities")) {
			throw new IllegalArgumentException("Armor " + id + " has an invalid abilities list");
		}
		List<?> configuredAbilities = section.getList("abilities");
		if (configuredAbilities == null) {
			throw new IllegalArgumentException("Armor " + id + " has an invalid abilities list");
		}
		List<String> abilityIDs = new ArrayList<>();
		for (Object configuredAbility : configuredAbilities) {
			if (!(configuredAbility instanceof String abilityID) || abilityID.isBlank()) {
				throw new IllegalArgumentException("Armor " + id + " has a blank ability ID");
			}
			abilityIDs.add(abilityID);
		}
		return List.copyOf(abilityIDs);
	}

	private String optionalId(ConfigurationSection section, String armorId, String field) {
		if (!section.isSet(field)) return null;
		String value = section.getString(field);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Armor " + armorId + " has a blank " + field);
		}
		return value.trim();
	}

	private Color parseColor(String value, String id) {
		String hex = value.startsWith("#") ? value.substring(1) : value;
		if (!hex.matches("[0-9a-fA-F]{6}")) {
			throw new IllegalArgumentException("Armor " + id + " has an invalid color: " + value);
		}
		return Color.fromRGB(Integer.parseInt(hex, 16));
	}

	private String requireText(ConfigurationSection section, String id, String field) {
		String value = section.getString(field);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Armor " + id + " has a missing or blank " + field);
		}
		return value;
	}

	private <T extends Enum<T>> T parseEnum(Class<T> type, String value, String id, String field) {
		try {
			return Enum.valueOf(type, value.toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Armor " + id + " has an invalid " + field + ": " + value, exception);
		}
	}
}
