package org.tomdang.foraging;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ForagingToolConfigurationLoader {
	public ForagingToolRegistry load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("foraging/tools.yml is missing");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection root = yaml.getConfigurationSection("foraging-tools");
		if (root == null) throw new IllegalArgumentException("Missing foraging-tools section");
		ForagingToolRegistry registry = new ForagingToolRegistry();
		for (String id : root.getKeys(false)) registry.register(new ForagingToolDefinition(id, root.getInt(id + ".required-foraging-level")));
		return registry;
	}
}
