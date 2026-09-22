package org.tomdang.foraging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ForagingTreeStore {
	private final File file;
	public ForagingTreeStore(File file) { this.file = file; }

	public List<ForagingTree> load(TreeModelRegistry models) {
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection section = yaml.getConfigurationSection("trees");
		if (section == null) return List.of();
		List<ForagingTree> result = new ArrayList<>();
		for (String id : section.getKeys(false)) {
			String path = "trees." + id;
			World world = Bukkit.getWorld(yaml.getString(path + ".world", ""));
			if (world == null) continue;
			String modelId = yaml.getString(path + ".model", "MODEL_OAK");
			result.add(new ForagingTree(id, new Location(world, yaml.getInt(path + ".x"),
					yaml.getInt(path + ".y"), yaml.getInt(path + ".z")), models.require(modelId)));
		}
		return result;
	}

	public void save(Collection<ForagingTree> trees) {
		YamlConfiguration yaml = new YamlConfiguration();
		for (ForagingTree tree : trees) {
			String path = "trees." + tree.id();
			yaml.set(path + ".model", tree.model().id());
			yaml.set(path + ".world", tree.root().getWorld().getName());
			yaml.set(path + ".x", tree.root().getBlockX());
			yaml.set(path + ".y", tree.root().getBlockY());
			yaml.set(path + ".z", tree.root().getBlockZ());
		}
		try {
			file.getParentFile().mkdirs();
			yaml.save(file);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not save foraging tree instances", exception);
		}
	}
}
