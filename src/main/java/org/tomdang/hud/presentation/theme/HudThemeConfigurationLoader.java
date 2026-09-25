package org.tomdang.hud.presentation.theme;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.hud.composition.draw.HudTextStyle;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public final class HudThemeConfigurationLoader {
	public HudTheme load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("HUD theme configuration cannot be null");
		var yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		String id = yaml.getString("id");
		ConfigurationSection styles = require(yaml, "text-styles");
		ConfigurationSection metrics = require(yaml, "metrics");
		var textValues = new LinkedHashMap<HudTextStyleToken, HudTextStyle>();
		for (String key : styles.getKeys(false)) {
			ConfigurationSection style = require(styles, key);
			textValues.put(HudTextStyleToken.of(key), new HudTextStyle(key,
					parseColor(style.getString("color")), style.getBoolean("bold", false), style.getBoolean("shadow", true)));
		}
		var metricValues = new LinkedHashMap<HudMetricToken, Integer>();
		for (String key : metrics.getKeys(false)) metricValues.put(HudMetricToken.of(key), metrics.getInt(key));
		return new HudTheme(id, textValues, metricValues);
	}
	private ConfigurationSection require(ConfigurationSection parent, String key) {
		ConfigurationSection section = parent.getConfigurationSection(key);
		if (section == null) throw new IllegalArgumentException("HUD theme requires " + key);
		return section;
	}
	private int parseColor(String value) {
		if (value == null || !value.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("HUD color must use #RRGGBB");
		return Integer.parseInt(value.substring(1), 16);
	}
}
