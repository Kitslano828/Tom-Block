package org.tomdang.region.visualization;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;

public final class RegionVisualizationConfigurationLoader {
	public RegionVisualizationSettings load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		return new RegionVisualizationSettings(
				requireLong(configuration, "visualization.interval-ticks"),
				requireInteger(configuration, "visualization.radius"),
				requireInteger(configuration, "visualization.boundary-spacing"),
				requireInteger(configuration, "visualization.maximum-markers"),
				requireInteger(configuration, "visualization.target-distance")
		);
	}

	private int requireInteger(YamlConfiguration configuration, String path) {
		if (!configuration.isInt(path)) throw new IllegalArgumentException(path + " must be an integer");
		return configuration.getInt(path);
	}

	private long requireLong(YamlConfiguration configuration, String path) {
		if (!configuration.isLong(path) && !configuration.isInt(path)) {
			throw new IllegalArgumentException(path + " must be an integer");
		}
		return configuration.getLong(path);
	}
}
