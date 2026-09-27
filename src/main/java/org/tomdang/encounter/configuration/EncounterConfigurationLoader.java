package org.tomdang.encounter.configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.encounter.definition.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import org.tomdang.activity.*;
public final class EncounterConfigurationLoader {
	public List<EncounterDefinition> load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("Encounter input cannot be null");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection root = yaml.getConfigurationSection("encounter");
		if (root == null) throw new IllegalArgumentException("Encounter file requires encounter");
		String id = required(root, "id");
		Map<String,String> parameters = new LinkedHashMap<>();
		ConfigurationSection parameterSection = root.getConfigurationSection("parameters");
		if (parameterSection != null) for (String key : parameterSection.getKeys(false)) parameters.put(key, String.valueOf(parameterSection.get(key)));
		ConfigurationSection activity = root.getConfigurationSection("activity");
		ActivityPolicy policy = activity == null ? ActivityPolicy.PRIVATE_SOLO : new ActivityPolicy(
				ActivityVisibility.valueOf(activity.getString("visibility", "OWNER").toUpperCase(Locale.ROOT)),
				ActivityAudience.valueOf(activity.getString("interaction", "OWNER").toUpperCase(Locale.ROOT)),
				ActivityContributionPolicy.valueOf(activity.getString("contribution", "BLOCK").toUpperCase(Locale.ROOT)),
				ActivityAudience.valueOf(activity.getString("rewards", "OWNER").toUpperCase(Locale.ROOT)));
		return List.of(new EncounterDefinition(id, required(root, "behavior"),
				EncounterMode.valueOf(root.getString("mode", "PLAYER").toUpperCase(Locale.ROOT)),
				Duration.ofSeconds(root.getLong("timeout-seconds", 300)),
				Duration.ofSeconds(root.getLong("disconnect-grace-seconds", 60)), parameters, policy));
	}
	private String required(ConfigurationSection section, String path) {
		String value = section.getString(path); if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing " + path); return value;
	}
}
