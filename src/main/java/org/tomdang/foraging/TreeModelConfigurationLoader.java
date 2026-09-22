package org.tomdang.foraging;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class TreeModelConfigurationLoader {
	public TreeModelRegistry load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("foraging/trees.yml is missing");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection root = yaml.getConfigurationSection("tree-models");
		if (root == null) throw new IllegalArgumentException("Missing tree-models section");
		TreeModelRegistry registry = new TreeModelRegistry();
		for (String id : root.getKeys(false)) {
			ConfigurationSection value = root.getConfigurationSection(id);
			if (value == null) throw new IllegalArgumentException("Tree model must be a section: " + id);
			registry.register(TreeModel.standard(id, Material.valueOf(text(value, "log-material")),
					Material.valueOf(text(value, "leaf-material")), value.getInt("tier"), value.getDouble("durability"),
					value.getDouble("required-power"), value.getLong("xp"), value.getInt("regeneration-seconds"), text(value, "collection")));
		}
		return registry;
	}
	private String text(ConfigurationSection section, String path) {
		String value = section.getString(path);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing tree value: " + section.getCurrentPath() + "." + path);
		return value;
	}
}
