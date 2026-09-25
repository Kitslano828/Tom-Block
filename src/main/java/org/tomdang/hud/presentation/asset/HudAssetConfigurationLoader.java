package org.tomdang.hud.presentation.asset;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class HudAssetConfigurationLoader {
	public HudAssetRegistry load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("HUD asset configuration cannot be null");
		var yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection definitions = yaml.getConfigurationSection("assets");
		if (definitions == null) throw new IllegalArgumentException("HUD assets require an assets section");
		HudAssetRegistry registry = new HudAssetRegistry();
		for (String rawId : definitions.getKeys(false)) {
			ConfigurationSection value = definitions.getConfigurationSection(rawId);
			if (value == null) throw new IllegalArgumentException("HUD asset must be an object: " + rawId);
			String[] id = rawId.split(":", 2);
			if (id.length != 2) throw new IllegalArgumentException("HUD asset id must be namespaced: " + rawId);
			HudAsset.Kind kind;
			try { kind = HudAsset.Kind.valueOf(value.getString("kind", "").toUpperCase(Locale.ROOT)); }
			catch (RuntimeException exception) { throw new IllegalArgumentException("Unknown HUD asset kind for " + rawId, exception); }
			registry.register(new HudAsset(HudAssetId.of(id[0], id[1]), kind,
					value.getInt("width"), value.getInt("height")));
		}
		registry.seal();
		return registry;
	}
}
