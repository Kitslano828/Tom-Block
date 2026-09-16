package org.tomdang.region.policy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.cap.LocationStatCap;
import org.tomdang.region.registry.RegionRegistry;

import java.io.Reader;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RegionStatCapConfigurationLoader {
	public Map<String, Map<PlayerStatType, Double>> load(Reader reader, RegionRegistry regions) {
		if (reader == null || regions == null) throw new IllegalArgumentException("reader and regions are required");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection root = yaml.getConfigurationSection("region-stat-caps");
		if (root == null) throw new IllegalArgumentException("Missing region-stat-caps section");
		Map<String, Map<PlayerStatType, Double>> loaded = new LinkedHashMap<>();
		for (String regionId : root.getKeys(false)) {
			regions.require(regionId);
			ConfigurationSection section = root.getConfigurationSection(regionId);
			if (section == null) throw new IllegalArgumentException("Expected stat caps for region " + regionId);
			EnumMap<PlayerStatType, Double> values = new EnumMap<>(PlayerStatType.class);
			for (String statKey : section.getKeys(false)) {
				PlayerStatType statType;
				try { statType = PlayerStatType.valueOf(statKey); }
				catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unknown stat cap " + statKey + " in " + regionId, exception); }
				Object configured = section.get(statKey);
				if (!(configured instanceof Number number)) throw new IllegalArgumentException("Non-numeric cap " + statKey + " in " + regionId);
				double value = number.doubleValue();
				values.put(statType, new LocationStatCap(regionId, statType, value).value());
			}
			loaded.put(regionId, Map.copyOf(values));
		}
		return Map.copyOf(loaded);
	}
}
