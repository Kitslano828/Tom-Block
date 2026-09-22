package org.tomdang.island.preset;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public final class IslandPresetConfigurationLoader {
	public IslandPresetRegistry load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("Island preset resource is missing");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
				new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection root = requireSection(yaml, "island-presets");
		IslandPresetRegistry registry = new IslandPresetRegistry();
		for (String id : root.getKeys(false)) registry.register(read(id, requireSection(root, id)));
		if (registry.all().isEmpty()) throw new IllegalArgumentException("At least one island preset is required");
		return registry;
	}

	private IslandPreset read(String id, ConfigurationSection section) {
		ConfigurationSection lifecycle = requireSection(section, "lifecycle");
		ConfigurationSection world = requireSection(section, "world");
		ConfigurationSection interactions = requireSection(section, "interactions");
		EnumSet<IslandTag> tags = EnumSet.noneOf(IslandTag.class);
		for (String tag : section.getStringList("tags")) tags.add(value(IslandTag.class, tag, id + ".tags"));
		return new IslandPreset(id, requiredString(section, "display-name"),
				value(IslandClassification.class, requiredString(section, "primary-type"), id + ".primary-type"), tags,
				value(IslandLifecycleMode.class, requiredString(lifecycle, "mode"), id + ".lifecycle.mode"),
				lifecycle.getInt("unload-delay-seconds", 0), world.getString("world-name"),
				requiredString(world, "generator"), world.getInt("travel-radius"),
				world.getDouble("spawn.x"), world.getDouble("spawn.y"), world.getDouble("spawn.z"),
				new IslandInteractionPolicy(
						policy(interactions, "placement", id), policy(interactions, "player-placed-breaking", id),
						policy(interactions, "terrain-breaking", id), policy(interactions, "registered-resources", id),
						interactions.getBoolean("explosions"), interactions.getBoolean("pistons"),
						interactions.getBoolean("fluid-modification")));
	}

	private IslandAccessPolicy policy(ConfigurationSection section, String path, String id) {
		return value(IslandAccessPolicy.class, requiredString(section, path), id + ".interactions." + path);
	}
	private <E extends Enum<E>> E value(Class<E> type, String raw, String path) {
		try { return Enum.valueOf(type, raw); }
		catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Invalid " + path + ": " + raw, exception); }
	}
	private ConfigurationSection requireSection(ConfigurationSection parent, String path) {
		ConfigurationSection section = parent.getConfigurationSection(path);
		if (section == null) throw new IllegalArgumentException("Missing island preset section: " + path);
		return section;
	}
	private String requiredString(ConfigurationSection section, String path) {
		String value = section.getString(path);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing island preset value: " + section.getCurrentPath() + "." + path);
		return value;
	}
}
