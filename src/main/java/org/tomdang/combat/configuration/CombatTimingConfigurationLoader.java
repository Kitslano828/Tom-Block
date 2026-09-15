package org.tomdang.combat.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;

public class CombatTimingConfigurationLoader {

	public CombatTimingConfiguration load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection combat = configuration.getConfigurationSection("combat");
		if (combat == null) throw new IllegalArgumentException("combat.yml does not contain the required combat section");

		String path = "default-basic-attack-recovery-ticks";
		if (!combat.isInt(path) && !combat.isLong(path)) {
			throw new IllegalArgumentException(path + " must be a whole number of ticks");
		}
		return new CombatTimingConfiguration(combat.getLong(path));
	}
}
