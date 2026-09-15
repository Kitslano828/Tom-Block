package org.tomdang.player.stats.rule;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.stats.PlayerStatType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.Set;

public class PlayerStatRuleConfigurationLoader {

	public List<PlayerStatRule> load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection root = requireSection(configuration, "stat-rules", "Root configuration");
		if (root.getKeys(false).isEmpty()) {
			throw new IllegalArgumentException("stat-rules section cannot be empty");
		}

		List<PlayerStatRule> rules = new ArrayList<>();
		Set<PlayerStatType> encountered = EnumSet.noneOf(PlayerStatType.class);
		for (String configuredId : root.getKeys(false)) {
			PlayerStatType statType = parseStatType(configuredId);
			if (!encountered.add(statType)) {
				throw new IllegalArgumentException("Duplicate stat rule for " + statType.name());
			}

			String context = "Stat rule " + configuredId;
			ConfigurationSection section = requireSection(root, configuredId, context);
			rules.add(new PlayerStatRule(statType, parseCap(section, context)));
		}

		Set<PlayerStatType> missing = EnumSet.allOf(PlayerStatType.class);
		missing.removeAll(encountered);
		if (!missing.isEmpty()) {
			throw new IllegalArgumentException("Missing stat rules: " + missing);
		}
		return List.copyOf(rules);
	}

	private PlayerStatType parseStatType(String configuredId) {
		try {
			return PlayerStatType.valueOf(configuredId.toUpperCase(Locale.ROOT).replace('-', '_'));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Unknown stat rule ID: " + configuredId, exception);
		}
	}

	private OptionalDouble parseCap(ConfigurationSection section, String context) {
		Object configuredCap = section.get("cap");
		if (configuredCap == null) return OptionalDouble.empty();
		if (!(configuredCap instanceof Number number)) {
			throw new IllegalArgumentException(context + " has an invalid cap: expected a number or null");
		}
		double cap = number.doubleValue();
		if (!Double.isFinite(cap)) {
			throw new IllegalArgumentException(context + " has an invalid cap: expected a finite number");
		}
		return OptionalDouble.of(cap);
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String path, String context) {
		if (!parent.isConfigurationSection(path)) {
			throw new IllegalArgumentException(context + " has a missing or invalid section path: " + path);
		}
		return parent.getConfigurationSection(path);
	}
}
