package org.tomdang.player.movement;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;

public final class PlayerMovementSpeedConfigurationLoader {
	public PlayerMovementSpeedSettings load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		return new PlayerMovementSpeedSettings(
				requireNumber(configuration, "movement-speed.reference-stat"),
				requireNumber(configuration, "movement-speed.reference-walk-speed"),
				requireNumber(configuration, "movement-speed.minimum-walk-speed"),
				requireNumber(configuration, "movement-speed.maximum-walk-speed")
		);
	}

	private double requireNumber(YamlConfiguration configuration, String path) {
		Object value = configuration.get(path);
		if (!(value instanceof Number number)) throw new IllegalArgumentException(path + " must be numeric");
		double result = number.doubleValue();
		if (!Double.isFinite(result)) throw new IllegalArgumentException(path + " must be finite");
		return result;
	}
}
