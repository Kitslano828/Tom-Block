package org.tomdang.mining.configuration.miningtool;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.configuration.CustomItemStatConfigurationLoader;
import org.tomdang.customitemframework.configuration.CustomItemStatCapConfigurationLoader;
import org.tomdang.customitemframework.configuration.CustomItemCombatConfigurationLoader;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class MiningToolConfigurationLoader {
	private final CustomItemStatConfigurationLoader statLoader = new CustomItemStatConfigurationLoader();
	private final CustomItemStatCapConfigurationLoader capLoader = new CustomItemStatCapConfigurationLoader();
	private final CustomItemCombatConfigurationLoader combatLoader = new CustomItemCombatConfigurationLoader();

	public List<MiningToolDefinition> loadDefinitions(File file) {
		if (file == null) throw new IllegalArgumentException("file cannot be null");
		return loadDefinitions(YamlConfiguration.loadConfiguration(file));
	}

	public List<MiningToolDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		return loadDefinitions(YamlConfiguration.loadConfiguration(reader));
	}

	private List<MiningToolDefinition> loadDefinitions(YamlConfiguration data) {
		List<MiningToolDefinition> definitions = new ArrayList<>();

		ConfigurationSection miningToolSection = data.getConfigurationSection("mining-tools");
		if (miningToolSection == null) {
			throw new IllegalArgumentException("mining-tools.yml does not contain the required root section.");
		}

		for (String id : miningToolSection.getKeys(false)) {
			ConfigurationSection section = miningToolSection.getConfigurationSection(id);
			if (section == null) {
				throw new IllegalArgumentException("Section should not be null");
			}

			String materialStr = section.getString("material");
			if (materialStr == null || materialStr.isBlank()) {
				throw new IllegalArgumentException(id + " cannot have material be null or blank");
			}

			String displayName = section.getString("display-name");
			if (displayName == null || displayName.isBlank()) {
				throw new IllegalArgumentException(id + " cannot have a display name be null or blank");
			}

			String rarityStr = section.getString("rarity");
			if (rarityStr == null || rarityStr.isBlank()) {
				throw new IllegalArgumentException(id + " cannot have a rarity be null or blank");
			}

			if (!section.isSet("breaking-power") || (!section.isInt("breaking-power"))) {
				throw new IllegalArgumentException(id + " has Missing breaking-power value, or invalid value");
			}
			int breakingPower = section.getInt("breaking-power");
			if (breakingPower < 0) throw new IllegalArgumentException(id + " has breaking-power lesser than 0");

			CustomItemStatModifiers statModifiers = section.isSet("stats")
					? statLoader.load(section, id)
					: loadLegacyStats(section, id);
			statLoader.requireNonNegative(statModifiers, PlayerStatType.MINING_SPEED, id);
			statLoader.requireNonNegative(statModifiers, PlayerStatType.MINING_FORTUNE, id);

			List<String> abilities = new ArrayList<>();
			if (section.isSet("abilities") && section.isList("abilities")) {
				List<String> rawAbilities = section.getStringList("abilities");

				// Loop through raw abilities, rejecting null or blank entries
				for (String abilityId : rawAbilities) {
					if (abilityId != null && !abilityId.isBlank()) {
						abilities.add(abilityId);
					} else {
						throw new IllegalArgumentException(id + "'s " + abilityId + " is invalid or blank");
					}
				}
			} else {
				throw new IllegalArgumentException(id + " has an invalid ability list");
			}

			Material material;
			try {
				material = Material.valueOf(materialStr.toUpperCase());
			} catch (IllegalArgumentException ignored) {
				throw new IllegalArgumentException("Item " + id + " has an invalid material " + materialStr);
			}

			Rarity rarity;
			try {
				rarity = Rarity.valueOf(rarityStr.toUpperCase());
			} catch (IllegalArgumentException ignored) {
				throw new IllegalArgumentException("Item " + id + " has an invalid rarity " + rarityStr);
			}

			MiningToolDefinition definition = new MiningToolDefinition(
					id,
					material,
					displayName,
					rarity,
					breakingPower,
					statModifiers,
					capLoader.load(section, id),
					combatLoader.load(section, id),
					abilities
			);

			definitions.add(definition);
		}

		return definitions;
	}

	private CustomItemStatModifiers loadLegacyStats(ConfigurationSection section, String id) {
		EnumMap<PlayerStatType, Double> stats = new EnumMap<>(PlayerStatType.class);
		stats.put(PlayerStatType.MINING_SPEED, requireLegacyNumber(section, id, "mining-speed"));
		stats.put(PlayerStatType.MINING_FORTUNE, requireLegacyNumber(section, id, "fortune"));
		return new CustomItemStatModifiers(stats);
	}

	private double requireLegacyNumber(ConfigurationSection section, String id, String field) {
		if (!section.isSet(field) || (!section.isDouble(field) && !section.isInt(field) && !section.isLong(field))) {
			throw new IllegalArgumentException(id + " has missing " + field + " value, or invalid value");
		}
		double amount = section.getDouble(field);
		if (amount < 0) throw new IllegalArgumentException(id + " has " + field + " lesser than 0");
		return amount;
	}

}
