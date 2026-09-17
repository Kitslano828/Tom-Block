package org.tomdang.actorframework.skin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ActorSkinConfigurationLoader {
	public Map<String, ActorSkin> load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection skins = configuration.getConfigurationSection("skins");
		if (skins == null) throw new IllegalArgumentException("skins.yml requires a skins section");
		Map<String, ActorSkin> loaded = new LinkedHashMap<>();
		for (String id : skins.getKeys(false)) {
			ConfigurationSection entry = skins.getConfigurationSection(id);
			if (entry == null) throw new IllegalArgumentException("Skin " + id + " must be a section");
			if (!entry.isString("value")) throw new IllegalArgumentException("Skin " + id + " requires a texture value");
			if (entry.contains("signature") && !entry.isString("signature"))
				throw new IllegalArgumentException("Skin " + id + " has an invalid signature");
			loaded.put(id, new ActorSkin(id, entry.getString("value"), entry.getString("signature")));
		}
		return Map.copyOf(loaded);
	}
}
