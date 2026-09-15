package org.tomdang.combat.combo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;

public class ComboTimingConfigurationLoader {
	public ComboTimingConfiguration load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection combat = configuration.getConfigurationSection("combat");
		if (combat == null) throw new IllegalArgumentException("combat.yml does not contain the required combat section");
		ConfigurationSection combo = combat.getConfigurationSection("combo");
		if (combo == null) throw new IllegalArgumentException("combat.yml does not contain the required combat.combo section");

		long baseGraceTicks = readLong(combo, "base-grace-ticks");
		long minimumGraceTicks = readLong(combo, "minimum-grace-ticks");
		long hitsToMinimumGrace = readLong(combo, "hits-to-minimum-grace");
		if (hitsToMinimumGrace > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("hits-to-minimum-grace is too large");
		}
		return new ComboTimingConfiguration(baseGraceTicks, minimumGraceTicks, (int) hitsToMinimumGrace);
	}

	private long readLong(ConfigurationSection section, String path) {
		if (!section.isInt(path) && !section.isLong(path)) {
			throw new IllegalArgumentException("combat.combo." + path + " must be a whole number");
		}
		return section.getLong(path);
	}
}
