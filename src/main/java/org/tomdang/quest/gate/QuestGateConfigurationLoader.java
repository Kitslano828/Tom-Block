package org.tomdang.quest.gate;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class QuestGateConfigurationLoader {
	public QuestGateRegistry load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("Quest gate configuration cannot be null");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection section = yaml.getConfigurationSection("gates");
		if (section == null) throw new IllegalArgumentException("quest-gates.yml requires gates");
		QuestGateRegistry registry = new QuestGateRegistry();
		for (String id : section.getKeys(false)) registry.register(read(id, requiredSection(section, id)));
		return registry;
	}

	private QuestGateDefinition read(String id, ConfigurationSection value) {
		return new QuestGateDefinition(id, requiredString(value, "quest"), requiredString(value, "world"),
				value.getDouble("center.x"), value.getDouble("center.z"),
				value.getDouble("normal.x"), value.getDouble("normal.z"),
				value.getDouble("half-width"), value.getDouble("blocked-side-margin", -0.35),
				value.getDouble("return-distance", 3.5), value.getInt("minimum-y"), value.getInt("maximum-y"),
				value.getInt("blink-ticks", 8), value.getLong("message-cooldown-ms", 2500),
				value.getString("actor"), value.getString("dialogue"), value.getString("prompt-message"));
	}

	private static ConfigurationSection requiredSection(ConfigurationSection parent, String path) {
		ConfigurationSection value = parent.getConfigurationSection(path);
		if (value == null) throw new IllegalArgumentException("Missing section " + path);
		return value;
	}
	private static String requiredString(ConfigurationSection section, String path) {
		String value = section.getString(path);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing string " + section.getCurrentPath() + "." + path);
		return value;
	}
}
