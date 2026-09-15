package org.tomdang.combat.weapons.configuration;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.configuration.CustomItemStatConfigurationLoader;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class WeaponConfigurationLoader {

	private final CustomItemStatConfigurationLoader statLoader = new CustomItemStatConfigurationLoader();

	public List<WeaponDefinition> loadDefinitions(File file) {
		if (file == null) throw new IllegalArgumentException("file cannot be null");
		return loadDefinitions(YamlConfiguration.loadConfiguration(file));
	}

	public List<WeaponDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		return loadDefinitions(YamlConfiguration.loadConfiguration(reader));
	}

	private List<WeaponDefinition> loadDefinitions(YamlConfiguration data) {
		List<WeaponDefinition> definitions = new ArrayList<>();

		ConfigurationSection weaponSection = data.getConfigurationSection("weapons");
		if (weaponSection == null) {
			throw new IllegalArgumentException("weapons.yml does not contain the required root section.");
		}

		for (String id : weaponSection.getKeys(false)) {
			ConfigurationSection section = weaponSection.getConfigurationSection(id);
			if (section == null) {
				throw new IllegalArgumentException("section should not be null");
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

			CustomItemStatModifiers statModifiers = section.isSet("stats")
					? statLoader.load(section, id)
					: loadLegacyStats(section, id);
			statLoader.requireNonNegative(statModifiers, PlayerStatType.DAMAGE, id);


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

			// Convert the rarity string into Rarity
			Rarity rarity;
			try {
				rarity = Rarity.valueOf(rarityStr.toUpperCase());
			} catch (IllegalArgumentException ignored) {
				throw new IllegalArgumentException("Item " + id + " has an invalid rarity " + rarityStr);
			}

			// Create a WeaponDefinition
			WeaponDefinition definition = new WeaponDefinition(
					id,
					material,
					displayName,
					rarity,
					statModifiers,
					abilities
			);

			// Add it to the result list
			definitions.add(definition);
		}

		return definitions;
	}

	private CustomItemStatModifiers loadLegacyStats(ConfigurationSection section, String id) {
		EnumMap<PlayerStatType, Double> stats = new EnumMap<>(PlayerStatType.class);
		stats.put(PlayerStatType.DAMAGE, requireLegacyNumber(section, id, "damage"));
		stats.put(PlayerStatType.STRENGTH, requireLegacyNumber(section, id, "strength"));
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
