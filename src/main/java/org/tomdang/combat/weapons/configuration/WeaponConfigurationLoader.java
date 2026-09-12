package org.tomdang.combat.weapons.configuration;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.customitemframework.Rarity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WeaponConfigurationLoader {


	public List<WeaponDefinition> loadDefinitions(File file) {


		List<WeaponDefinition> definitions = new ArrayList<>();
		YamlConfiguration data = YamlConfiguration.loadConfiguration(file);

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

			if (!section.isSet("damage") || (!section.isDouble("damage") && !section.isInt("damage"))) {
				throw new IllegalArgumentException(id + " has Missing damage value, or invalid value");
			}
			double damage = section.getDouble("damage");
			if (damage < 0) throw new IllegalArgumentException(id + " has damage lesser than 0");


			if (!section.isSet("strength") || (!section.isDouble("strength") && !section.isInt("strength"))) {
				throw new IllegalArgumentException(id + " has Missing strength value, or invalid value");
			}
			double strength = section.getDouble("strength");
			if (strength < 0) throw new IllegalArgumentException(id + " has strength lesser than 0");


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
					damage,
					strength,
					abilities
			);

			// Add it to the result list
			definitions.add(definition);
		}

		return definitions;
	}

}
