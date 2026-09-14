package org.tomdang.actorframework.spawn.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.actorframework.audience.ActorAudienceScope;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ActorSpawnPointConfigurationLoader {

	public List<ActorSpawnPointConfigurationDefinition> loadDefinitions(Reader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("reader cannot be null");
		}

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection spawnPointsSection = requireSection(configuration, "spawn-points", "Root configuration");

		List<ActorSpawnPointConfigurationDefinition> definitions = new ArrayList<>();

		for (String spawnPointId : spawnPointsSection.getKeys(false)) {
			if (spawnPointId.isBlank()) {
				throw new IllegalArgumentException("Spawn point key cannot be blank");
			}

			ConfigurationSection entrySection = requireSection(spawnPointsSection, spawnPointId, "Spawn point " + spawnPointId);

			String actorId = requireString(entrySection, "actor-id", spawnPointId);

			// Audience parsing
			ConfigurationSection audienceSection = requireSection(entrySection, "audience", "Spawn point " + spawnPointId);
			ActorAudienceScope audienceScope = parseEnum(audienceSection, "scope", spawnPointId + " audience", ActorAudienceScope.class);
			UUID audienceId = parseOptionalUUID(audienceSection, "id", spawnPointId);

			// Location parsing
			ConfigurationSection locationSection = requireSection(entrySection, "location", "Spawn point " + spawnPointId);
			String worldName = requireString(locationSection, "world", spawnPointId + " location");

			double x = requireDouble(locationSection, "x", spawnPointId + " location");
			double y = requireDouble(locationSection, "y", spawnPointId + " location");
			double z = requireDouble(locationSection, "z", spawnPointId + " location");

			float yaw = optionalFloat(locationSection, "yaw", spawnPointId + " location");
			float pitch = optionalFloat(locationSection, "pitch", spawnPointId + " location");

			definitions.add(new ActorSpawnPointConfigurationDefinition(
					spawnPointId,
					actorId,
					audienceScope,
					audienceId,
					worldName,
					x,
					y,
					z,
					yaw,
					pitch
			));
		}

		if (definitions.isEmpty()) {
			throw new IllegalArgumentException("spawn-points configuration cannot be empty");
		}

		return List.copyOf(definitions);
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String path, String context) {
		if (!parent.isConfigurationSection(path)) {
			throw new IllegalArgumentException(context + " has a missing or invalid section path: " + path);
		}
		return parent.getConfigurationSection(path);
	}

	private String requireString(ConfigurationSection section, String path, String context) {
		if (!section.isString(path)) {
			throw new IllegalArgumentException("Spawn point " + context + " has a missing or invalid string value for " + path);
		}
		String value = section.getString(path);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Spawn point " + context + " has a missing or blank " + path);
		}
		return value;
	}

	private double requireDouble(ConfigurationSection section, String path, String context) {
		if (!section.contains(path)) {
			throw new IllegalArgumentException("Spawn point " + context + " is missing required numeric coordinate: " + path);
		}
		Object value = section.get(path);
		if (!(value instanceof Number number)) {
			throw new IllegalArgumentException("Spawn point " + context + " has an invalid non-numeric value for " + path + ": " + section.get(path));
		}
		return number.doubleValue();
	}

	private float optionalFloat(ConfigurationSection section, String path, String context) {
		if (!section.contains(path)) {
			return (float) 0.0;
		}
		Object value = section.get(path);
		if (!(value instanceof Number number)) {
			throw new IllegalArgumentException("Spawn point " + context + " has an invalid non-numeric value for " + path + ": " + section.get(path));
		}
		return number.floatValue();
	}

	private <E extends Enum<E>> E parseEnum(ConfigurationSection section, String path, String context, Class<E> enumClass) {
		String rawValue = section.getString(path);
		if (rawValue == null || rawValue.isBlank()) {
			throw new IllegalArgumentException("Spawn point " + context + " has a missing or blank " + path);
		}
		try {
			return Enum.valueOf(enumClass, rawValue.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Spawn point " + context + " has an invalid " + path + ": " + rawValue, e);
		}
	}

	private UUID parseOptionalUUID(ConfigurationSection section, String path, String spawnPointContext) {
		if (!section.contains(path)) {
			return null;
		}
		String rawUUID = section.getString(path);
		if (rawUUID == null || rawUUID.isBlank()) {
			throw new IllegalArgumentException("Spawn point " + spawnPointContext + " has a blank or invalid audience UUID");
		}
		try {
			return UUID.fromString(rawUUID);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Spawn point " + spawnPointContext + " has a malformed audience UUID: " + rawUUID, e);
		}
	}

}
