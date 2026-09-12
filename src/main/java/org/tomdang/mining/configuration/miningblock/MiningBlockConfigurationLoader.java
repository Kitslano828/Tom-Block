package org.tomdang.mining.configuration.miningblock;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiningBlockConfigurationLoader {

	public List<MiningBlockDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration data = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection blocksSection = data.getConfigurationSection("mining-blocks");
		if (blocksSection == null) {
			throw new IllegalArgumentException("mining-blocks.yml does not contain the required mining-blocks root section");
		}

		List<MiningBlockDefinition> definitions = new ArrayList<>();
		for (String materialName : blocksSection.getKeys(false)) {
			ConfigurationSection section = blocksSection.getConfigurationSection(materialName);
			if (section == null) {
				throw new IllegalArgumentException("Mining block " + materialName + " must be a configuration section");
			}

			Material material = parseMaterial(materialName);
			int blockStrength = requireNonNegativeInt(section, materialName, "block-strength");
			int breakingPower = requireNonNegativeInt(section, materialName, "breaking-power");
			int xp = requireNonNegativeInt(section, materialName, "xp");
			long regenerationTime = requireNonNegativeLong(section, materialName, "regeneration-time-seconds");
			List<MiningDropDefinition> drops = loadDrops(section, materialName);

			definitions.add(new MiningBlockDefinition(
					material,
					blockStrength,
					breakingPower,
					xp,
					regenerationTime,
					drops
			));
		}

		return definitions;
	}

	private List<MiningDropDefinition> loadDrops(ConfigurationSection section, String blockId) {
		if (!section.isList("drops")) {
			throw new IllegalArgumentException("Mining block " + blockId + " has a missing or invalid drops list");
		}

		List<?> rawDrops = section.getList("drops");
		if (rawDrops == null || rawDrops.isEmpty()) {
			throw new IllegalArgumentException("Mining block " + blockId + " must contain at least one drop");
		}

		List<MiningDropDefinition> drops = new ArrayList<>();
		for (int index = 0; index < rawDrops.size(); index++) {
			Object rawDrop = rawDrops.get(index);
			if (!(rawDrop instanceof Map<?, ?> dropValues)) {
				throw new IllegalArgumentException("Mining block " + blockId + " drop " + index + " must be a section");
			}

			String customItemId = requireString(dropValues, blockId, index, "item");
			int amount = requirePositiveInt(dropValues, blockId, index, "amount");
			double chance = requireChance(dropValues, blockId, index);
			boolean affectedByFortune = requireBoolean(dropValues, blockId, index, "affected-by-fortune");
			drops.add(new MiningDropDefinition(customItemId, amount, chance, affectedByFortune));
		}

		return drops;
	}

	private Material parseMaterial(String value) {
		try {
			return Material.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid mining block material: " + value, exception);
		}
	}

	private int requireNonNegativeInt(ConfigurationSection section, String blockId, String field) {
		if (!section.isInt(field)) {
			throw new IllegalArgumentException("Mining block " + blockId + " has a missing or invalid " + field);
		}
		int value = section.getInt(field);
		if (value < 0) {
			throw new IllegalArgumentException("Mining block " + blockId + " cannot have negative " + field);
		}
		return value;
	}

	private long requireNonNegativeLong(ConfigurationSection section, String blockId, String field) {
		if (!section.isInt(field) && !section.isLong(field)) {
			throw new IllegalArgumentException("Mining block " + blockId + " has a missing or invalid " + field);
		}
		long value = section.getLong(field);
		if (value < 0) {
			throw new IllegalArgumentException("Mining block " + blockId + " cannot have negative " + field);
		}
		return value;
	}

	private String requireString(Map<?, ?> values, String blockId, int index, String field) {
		Object value = values.get(field);
		if (!(value instanceof String text) || text.isBlank()) {
			throw invalidDropField(blockId, index, field);
		}
		return text;
	}

	private int requirePositiveInt(Map<?, ?> values, String blockId, int index, String field) {
		Object value = values.get(field);
		if (!(value instanceof Integer number) || number < 1) {
			throw invalidDropField(blockId, index, field);
		}
		return number;
	}

	private double requireChance(Map<?, ?> values, String blockId, int index) {
		Object value = values.get("chance");
		if (!(value instanceof Number number)) {
			throw invalidDropField(blockId, index, "chance");
		}
		double chance = number.doubleValue();
		if (!Double.isFinite(chance) || chance < 0.0 || chance > 100.0) {
			throw invalidDropField(blockId, index, "chance");
		}
		return chance;
	}

	private boolean requireBoolean(Map<?, ?> values, String blockId, int index, String field) {
		Object value = values.get(field);
		if (!(value instanceof Boolean booleanValue)) {
			throw invalidDropField(blockId, index, field);
		}
		return booleanValue;
	}

	private IllegalArgumentException invalidDropField(String blockId, int index, String field) {
		return new IllegalArgumentException("Mining block " + blockId + " drop " + index + " has an invalid " + field);
	}
}
