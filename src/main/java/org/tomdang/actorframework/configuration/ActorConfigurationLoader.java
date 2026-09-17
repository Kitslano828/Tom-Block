package org.tomdang.actorframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActorConfigurationLoader {

	public List<ActorConfigurationDefinition> loadDefinitions(Reader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("reader cannot be null");
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);

		ConfigurationSection actorsSection = requireSection(config, "actors", "Root configuration");
		if (actorsSection.getKeys(false).isEmpty()) {
			throw new IllegalArgumentException("actors section cannot be empty");
		}

		List<ActorConfigurationDefinition> definitions = new ArrayList<>();

		for (String actorId : actorsSection.getKeys(false)) {
			ConfigurationSection actorSection = requireSection(actorsSection, actorId, "Actor " + actorId);

			String displayName = requireString(actorSection, "display-name", actorId);
			ActorAudienceScope audienceScope = parseEnum(actorSection, "audience-scope", actorId, ActorAudienceScope.class);
			String presentationTypeId = requireString(actorSection, "presentation-type", actorId);
			String skinId = optionalString(actorSection, "skin", "Actor " + actorId);
			String interactionId = optionalString(actorSection, "interaction-id", "Actor " + actorId);
			ActorDamagePolicy damagePolicy = parseEnum(actorSection, "damage-policy", actorId, ActorDamagePolicy.class);
			ActorCollisionPolicy collisionPolicy = parseEnum(actorSection, "collision-policy", actorId, ActorCollisionPolicy.class);

			ConfigurationSection nameplateSection = requireSection(actorSection, "nameplate", "Actor " + actorId);
			ConfigurationSection linesSection = requireSection(nameplateSection, "lines", "Actor " + actorId + " nameplate");

			List<ActorNameplateLineConfigurationDefinition> nameplateLines = new ArrayList<>();

			for (String lineKey : linesSection.getKeys(false)) {
				ConfigurationSection lineSection = requireSection(linesSection, lineKey, "Actor " + actorId + " nameplate line " + lineKey);

				ActorNameplateLineRole role = parseEnum(lineSection, "role", actorId + " nameplate line " + lineKey, ActorNameplateLineRole.class);
				String text = requireString(lineSection, "text", actorId + " nameplate line " + lineKey);
				String color = optionalString(lineSection, "color", "Actor " + actorId + " nameplate line " + lineKey);

				boolean bold = optionalBoolean(lineSection, "bold", false, "Actor " + actorId + " nameplate line " + lineKey);
				boolean italic = optionalBoolean(lineSection, "italic", false, "Actor " + actorId + " nameplate line " + lineKey);
				boolean visibleWhileMoving = optionalBoolean(lineSection, "visible-while-moving", false, "Actor " + actorId + " nameplate line " + lineKey);

				nameplateLines.add(new ActorNameplateLineConfigurationDefinition(
						role,
						text,
						color,
						bold,
						italic,
						visibleWhileMoving
				));
			}

			if (nameplateLines.isEmpty()) {
				throw new IllegalArgumentException("Actor " + actorId + " nameplate lines section cannot be empty");
			}

			definitions.add(new ActorConfigurationDefinition(
					actorId,
					displayName,
					audienceScope,
					presentationTypeId,
					interactionId,
					damagePolicy,
					collisionPolicy,
					nameplateLines,
					skinId
			));
		}

		return List.copyOf(definitions);
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String path, String context) {
		if (!parent.isConfigurationSection(path)) {
			throw new IllegalArgumentException(context + " has a missing or invalid section path: " + path);
		}
		return parent.getConfigurationSection(path);
	}

	private String requireString(ConfigurationSection section, String path, String actorContext) {
		String value = section.getString(path);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Actor " + actorContext + " has a missing or blank " + path);
		}
		return value;
	}

	private String optionalString(ConfigurationSection section, String path, String context) {
		if (!section.contains(path)) {
			return null;
		}
		String value = section.getString(path);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(context + " has an invalid " + path);
		}
		return value;
	}

	private boolean optionalBoolean(ConfigurationSection section, String path, boolean defaultValue, String context) {
		if (!section.contains(path)) return defaultValue;
		if (!section.isBoolean(path)) {
			throw new IllegalArgumentException(context + " has an invalid " + path + ": expected true or false");
		}
		return section.getBoolean(path);
	}

	private <E extends Enum<E>> E parseEnum(ConfigurationSection section, String path, String actorContext, Class<E> enumClass) {
		String rawValue = section.getString(path);
		if (rawValue == null || rawValue.isBlank()) {
			throw new IllegalArgumentException("Actor " + actorContext + " has a missing or blank " + path);
		}
		try {
			return Enum.valueOf(enumClass, rawValue.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Actor " + actorContext + " has an invalid " + path + ": " + rawValue, e);
		}
	}
}
