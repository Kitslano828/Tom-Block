package org.tomdang.customitemframework.configuration;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CustomItemConfigurationLoader {
	private final CustomItemStatConfigurationLoader statLoader = new CustomItemStatConfigurationLoader();
	private final CustomItemStatCapConfigurationLoader capLoader = new CustomItemStatCapConfigurationLoader();
	private final CustomItemCombatConfigurationLoader combatLoader = new CustomItemCombatConfigurationLoader();

	public List<CustomItemDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration data = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection itemSection = data.getConfigurationSection("items");
		if (itemSection == null) {
			throw new IllegalArgumentException("items.yml does not contain the required items root section");
		}

		List<CustomItemDefinition> definitions = new ArrayList<>();
		for (String id : itemSection.getKeys(false)) {
			if (id == null || id.isBlank()) {
				throw new IllegalArgumentException("item ID cannot be null or blank");
			}

			ConfigurationSection section = itemSection.getConfigurationSection(id);
			if (section == null) {
				throw new IllegalArgumentException("Item " + id + " must be a configuration section");
			}

			String materialValue = requireText(section, id, "material");
			String displayName = requireText(section, id, "display-name");
			String rarityValue = requireText(section, id, "rarity");
			String categoryValue = requireText(section, id, "category");

			Material material = parseEnum(Material.class, materialValue, id, "material");
			Rarity rarity = parseEnum(Rarity.class, rarityValue, id, "rarity");
			ItemCategory category = parseEnum(ItemCategory.class, categoryValue, id, "category");

			definitions.add(new CustomItemDefinition(
					id, material, displayName, rarity, category,
					statLoader.load(section, id), capLoader.load(section, id), combatLoader.load(section, id)
			));
		}

		return definitions;
	}

	private String requireText(ConfigurationSection section, String itemId, String field) {
		String value = section.getString(field);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Item " + itemId + " has a missing or blank " + field);
		}
		return value;
	}

	private <T extends Enum<T>> T parseEnum(Class<T> enumType, String value, String itemId, String field) {
		try {
			return Enum.valueOf(enumType, value.toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Item " + itemId + " has an invalid " + field + ": " + value, exception);
		}
	}
}
