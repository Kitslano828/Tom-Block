package org.tomdang.custommobframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.tomdang.custommobframework.MobType;
import org.tomdang.combat.eligibility.AttackCapability;
import org.tomdang.combat.eligibility.AttackEligibilityRule;
import org.tomdang.custommobframework.behavior.MobBehaviorType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

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
					loadAllowedSpawnRegions(mob, id),
					loadPopulation(mob, id),
					loadAttackEligibility(mob, id),
					loadDrops(mob, id),
					parseEnum(MobBehaviorType.class, mob.getString("behavior", "VANILLA"), id, "behavior")
			));
		}
		return List.copyOf(definitions);
	}

	private AttackEligibilityRule loadAttackEligibility(ConfigurationSection mob, String mobId) {
		String field = "accepted-attack-capabilities";
		if (!mob.contains(field)) return new AttackEligibilityRule(Set.of());
		if (!mob.isList(field)) throw new IllegalArgumentException("Mob " + mobId + " has a non-list " + field);
		List<?> values = mob.getList(field);
		if (values == null) throw new IllegalArgumentException("Mob " + mobId + " has an invalid " + field);
		Set<AttackCapability> accepted = EnumSet.noneOf(AttackCapability.class);
		for (Object value : values) {
			if (!(value instanceof String name) || name.isBlank()) {
				throw new IllegalArgumentException("Mob " + mobId + " has a non-text " + field + " entry");
			}
			AttackCapability capability;
			try {
				capability = AttackCapability.valueOf(name.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Mob " + mobId + " has unknown attack capability " + name, exception);
			}
			if (!accepted.add(capability)) throw new IllegalArgumentException("Mob " + mobId + " has duplicate attack capability " + name);
		}
		return new AttackEligibilityRule(accepted);
	}

	private MobPopulationRule loadPopulation(ConfigurationSection mob, String mobId) {
		if (!mob.contains("population")) return null;
		ConfigurationSection section = requireSection(mob, "population", "Population for mob " + mobId);
		return new MobPopulationRule(mobId,
				requireText(section, mobId, "region"),
				requireInt(section, mobId, "max-alive"),
				requireLong(section, mobId, "interval-ticks"),
				requireInt(section, mobId, "activation-radius"),
				requireInt(section, mobId, "despawn-radius"),
				requireLong(section, mobId, "despawn-grace-ticks"),
				requireInt(section, mobId, "minimum-spawn-distance"),
				requireInt(section, mobId, "maximum-spawn-distance"),
				parseEnum(MobSpawnPlacement.class, section.getString("placement", "GROUND"), mobId, "placement"),
				optionalInt(section, mobId, "minimum-y"),
				optionalInt(section, mobId, "maximum-y"),
				section.contains("max-near-player") ? requireInt(section, mobId, "max-near-player")
						: requireInt(section, mobId, "max-alive"));
	}

	private Integer optionalInt(ConfigurationSection section, String id, String field) {
		if (!section.contains(field)) return null;
		return requireInt(section, id, field);
	}

	private long requireLong(ConfigurationSection section, String id, String field) {
		if (!section.isInt(field) && !section.isLong(field)) {
			throw new IllegalArgumentException("Mob " + id + " has a non-integer " + field);
		}
		return section.getLong(field);
	}

	private List<String> loadAllowedSpawnRegions(ConfigurationSection mob, String mobId) {
		if (!mob.contains("allowed-spawn-regions")) return List.of();
		if (!mob.isList("allowed-spawn-regions")) {
			throw new IllegalArgumentException("Mob " + mobId + " has a non-list allowed-spawn-regions");
		}
		List<?> values = mob.getList("allowed-spawn-regions");
		if (values == null || values.stream().anyMatch(value -> !(value instanceof String))) {
			throw new IllegalArgumentException("Mob " + mobId + " has a non-text allowed-spawn-regions entry");
		}
		return values.stream().map(value -> ((String) value).trim()).toList();
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
