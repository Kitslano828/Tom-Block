package org.tomdang.custommobframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.tomdang.custommobframework.MobType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CustomMobConfigurationLoader {
	public List<CustomMobDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection mobs = configuration.getConfigurationSection("mobs");
		if (mobs == null) throw new IllegalArgumentException("mobs.yml does not contain the required mobs section");

		List<CustomMobDefinition> definitions = new ArrayList<>();
		for (String id : mobs.getKeys(false)) {
			ConfigurationSection mob = requireSection(mobs, id, "Mob " + id);
			definitions.add(new CustomMobDefinition(
					id,
					parseEnum(EntityType.class, requireText(mob, id, "entity-type"), id, "entity-type"),
					requireText(mob, id, "display-name"),
					requireDouble(mob, id, "max-health"),
					requireDouble(mob, id, "damage"),
					parseEnum(MobType.class, requireText(mob, id, "mob-type"), id, "mob-type"),
					requireInt(mob, id, "xp"),
					requireBoolean(mob, id, "burns-in-daylight"),
					loadDrops(mob, id)
			));
		}
		return List.copyOf(definitions);
	}

	private List<CustomMobDropDefinition> loadDrops(ConfigurationSection mob, String mobId) {
		ConfigurationSection drops = mob.getConfigurationSection("drops");
		if (drops == null) return List.of();
		List<CustomMobDropDefinition> definitions = new ArrayList<>();
		for (String itemId : drops.getKeys(false)) {
			ConfigurationSection drop = requireSection(drops, itemId, "Drop " + itemId + " for mob " + mobId);
			definitions.add(new CustomMobDropDefinition(
					itemId,
					requireInt(drop, mobId + " drop " + itemId, "amount"),
					requireDouble(drop, mobId + " drop " + itemId, "chance")
			));
		}
		return definitions;
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String path, String description) {
		ConfigurationSection section = parent.getConfigurationSection(path);
		if (section == null) throw new IllegalArgumentException(description + " must be a configuration section");
		return section;
	}

	private String requireText(ConfigurationSection section, String id, String field) {
		String value = section.getString(field);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Mob " + id + " has a missing or blank " + field);
		return value;
	}

	private double requireDouble(ConfigurationSection section, String id, String field) {
		if (!section.isDouble(field) && !section.isInt(field) && !section.isLong(field)) {
			throw new IllegalArgumentException("Mob " + id + " has a non-numeric " + field);
		}
		return section.getDouble(field);
	}

	private int requireInt(ConfigurationSection section, String id, String field) {
		if (!section.isInt(field)) throw new IllegalArgumentException("Mob " + id + " has a non-integer " + field);
		return section.getInt(field);
	}

	private boolean requireBoolean(ConfigurationSection section, String id, String field) {
		if (!section.isBoolean(field)) throw new IllegalArgumentException("Mob " + id + " has a non-boolean " + field);
		return section.getBoolean(field);
	}

	private <T extends Enum<T>> T parseEnum(Class<T> type, String value, String id, String field) {
		try {
			return Enum.valueOf(type, value.toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Mob " + id + " has an invalid " + field + ": " + value, exception);
		}
	}
}
