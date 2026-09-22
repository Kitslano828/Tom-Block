package org.tomdang.collection;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.counter.CounterKey;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class CollectionConfigurationLoader {
	public CollectionRegistry load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("collections.yml is missing");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection root = yaml.getConfigurationSection("collections");
		if (root == null) throw new IllegalArgumentException("collections.yml requires a collections section");
		CollectionRegistry registry = new CollectionRegistry();
		for (String id : root.getKeys(false)) {
			ConfigurationSection section = root.getConfigurationSection(id);
			if (section == null) throw new IllegalArgumentException("Collection must be a section: " + id);
			registry.register(new CollectionDefinition(id, required(section, "display-name"), required(section, "category"),
					Material.valueOf(required(section, "material")), CounterKey.of(required(section, "counter")), milestones(section, id)));
		}
		return registry;
	}
	private java.util.List<CollectionMilestone> milestones(ConfigurationSection section, String id) {
		var result = new ArrayList<CollectionMilestone>();
		for (java.util.Map<?, ?> value : section.getMapList("milestones")) {
			Object amount = value.get("amount"); Object reward = value.get("skill-xp");
			if (!(amount instanceof Number threshold) || !(reward instanceof Number xp))
				throw new IllegalArgumentException("Invalid milestone in collection " + id);
			result.add(new CollectionMilestone(threshold.longValue(), xp.longValue()));
		}
		return result;
	}
	private String required(ConfigurationSection section, String path) {
		String value = section.getString(path);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing collection value: " + section.getCurrentPath() + "." + path);
		return value;
	}
}
