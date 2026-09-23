package org.tomdang.foraging.encounter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ForagingEncounterConfigurationLoader {
	public ForagingEncounterRegistry load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("foraging/encounters.yml is missing");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection root = yaml.getConfigurationSection("encounters");
		if (root == null) throw new IllegalArgumentException("Missing encounters section");
		ForagingEncounterRegistry registry = new ForagingEncounterRegistry();
		for (String id : root.getKeys(false)) {
			ConfigurationSection value = root.getConfigurationSection(id);
			if (value == null) throw new IllegalArgumentException("Encounter must be a section: " + id);
			List<EncounterNode> nodes = new ArrayList<>();
			for (Map<?, ?> node : value.getMapList("nodes")) nodes.add(new EncounterNode(number(node, "x"), number(node, "y"), number(node, "z")));
			registry.register(new ForagingEncounterDefinition(id, required(value, "display-name"), required(value, "world"),
					value.getDouble("toughness"), value.getInt("cooldown-seconds"), value.getLong("xp"),
					required(value, "collection"), nodes));
		}
		return registry;
	}
	private int number(Map<?, ?> values, String key) {
		Object value = values.get(key);
		if (!(value instanceof Number number)) throw new IllegalArgumentException("Missing encounter node " + key);
		return number.intValue();
	}
	private String required(ConfigurationSection section, String path) {
		String value = section.getString(path);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing encounter value " + path);
		return value;
	}
}
