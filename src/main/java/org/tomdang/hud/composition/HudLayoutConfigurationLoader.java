package org.tomdang.hud.composition;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
	import java.util.LinkedHashMap;
import org.tomdang.hud.composition.layout.HudAlignment;
import org.tomdang.hud.composition.layout.HudAxis;

public final class HudLayoutConfigurationLoader {
	public HudLayoutPolicy load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("HUD layout configuration cannot be null");
		var yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
		var section = yaml.getConfigurationSection("regions");
		if (section == null) throw new IllegalArgumentException("hud-layout.yml requires regions");
		EnumMap<HudRegion, HudRegionLayout> layouts = new EnumMap<>(HudRegion.class);
		HudLayoutPolicy defaults = new HudLayoutPolicy(Map.of());
		for (String key : section.getKeys(false)) {
			HudRegion region;
			try { region = HudRegion.valueOf(key.toUpperCase(Locale.ROOT).replace('-', '_')); }
			catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unknown HUD region: " + key, exception); }
			HudRegionLayout fallback = defaults.region(region);
			String path = key + ".";
			layouts.put(region, new HudRegionLayout(
					parse(HudAnchor.class, section.getString(path + "anchor", fallback.anchor().name()), "anchor"),
					section.getInt(path + "offset-x", fallback.offsetX()),
					section.getInt(path + "offset-y", fallback.offsetY()),
					section.getInt(path + "max-width", fallback.maxWidth()),
					section.getInt(path + "capacity", fallback.capacity()),
					parse(HudAxis.class, section.getString(path + "stack", fallback.stackAxis().name()), "stack"),
					section.getInt(path + "gap", fallback.gap()),
					parse(HudAlignment.class, section.getString(path + "alignment", fallback.alignment().name()), "alignment")
			));
		}
		Map<HudElementId, HudElementLayout> elements = new LinkedHashMap<>();
		var elementSection = yaml.getConfigurationSection("elements");
		if (elementSection != null) {
			for (String key : elementSection.getKeys(false)) {
				HudElementId id = HudElementId.parse(key);
				String path = key + ".";
				HudRegion fallbackRegion = parse(HudRegion.class,
						elementSection.getString(path + "region", "notification"), "element region");
				HudRegionLayout fallback = layouts.getOrDefault(fallbackRegion, defaults.region(fallbackRegion));
				elements.put(id, new HudElementLayout(
						elementSection.getBoolean(path + "enabled", true),
						parse(HudAnchor.class, elementSection.getString(path + "anchor", fallback.anchor().name()), "element anchor"),
						elementSection.getInt(path + "offset-x", fallback.offsetX()),
						elementSection.getInt(path + "offset-y", fallback.offsetY()),
						elementSection.getInt(path + "max-width", fallback.maxWidth())
				));
			}
		}
		return new HudLayoutPolicy(layouts, elements);
	}

	private <T extends Enum<T>> T parse(Class<T> type, String value, String label) {
		try { return Enum.valueOf(type, value.toUpperCase(Locale.ROOT).replace('-', '_')); }
		catch (RuntimeException exception) { throw new IllegalArgumentException("Unknown HUD " + label + ": " + value, exception); }
	}
}
