package org.tomdang.hud.protocol;

import net.kyori.adventure.key.Key;
import java.io.InputStream;
import java.util.*;

/** Shared server-side view of the generated resource-pack protocol manifest. */
public record HudProtocolConfiguration(String version, String minecraftVersion, UUID packId, Key font,
		int markerRed, int actionBarBaselineOffset, int encodedAscent, Map<String, Character> glyphs,
		Map<Integer, Integer> palette, int barCellAdvance, Map<String, Integer> barCellAdvances) {
	public HudProtocolConfiguration {
		if (version == null || version.isBlank() || minecraftVersion == null || minecraftVersion.isBlank()
				|| packId == null || font == null)
			throw new IllegalArgumentException("HUD protocol identity is incomplete");
		if (markerRed < 0 || markerRed > 255 || actionBarBaselineOffset < 0
				|| encodedAscent < 0 || encodedAscent > 8 || barCellAdvance <= 0)
			throw new IllegalArgumentException("HUD protocol numeric values are invalid");
		glyphs = Map.copyOf(glyphs);
		palette = Map.copyOf(palette);
		barCellAdvances = Map.copyOf(barCellAdvances);
		if (barCellAdvances.values().stream().anyMatch(advance -> advance == null || advance <= 0))
			throw new IllegalArgumentException("HUD style bar advances are invalid");
		if (!palette.containsKey(0xFFFFFF) || palette.values().stream().anyMatch(index -> index < 0 || index > 16)
				|| new HashSet<>(palette.values()).size() != palette.size())
			throw new IllegalArgumentException("HUD protocol palette is invalid");
	}

	public static HudProtocolConfiguration load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("HUD protocol manifest is missing");
		Properties properties = new Properties();
		try { properties.load(input); }
		catch (java.io.IOException exception) { throw new IllegalArgumentException("Could not read HUD protocol manifest", exception); }
		Map<String, Character> glyphs = new LinkedHashMap<>();
		for (String name : properties.stringPropertyNames()) if (name.startsWith("glyph.")) {
			int codePoint = Integer.parseInt(properties.getProperty(name), 16);
			if (!Character.isBmpCodePoint(codePoint)) throw new IllegalArgumentException("HUD glyph must be in the BMP: " + name);
			glyphs.put(name.substring("glyph.".length()).replace('_', '-'), (char) codePoint);
		}
		if (new HashSet<>(glyphs.values()).size() != glyphs.size())
			throw new IllegalArgumentException("HUD protocol contains duplicate glyph allocations");
		Map<Integer, Integer> palette = new LinkedHashMap<>();
		for (String name : properties.stringPropertyNames()) if (name.startsWith("palette.")) {
			int index = Integer.parseInt(name.substring("palette.".length()));
			int rgb = Integer.parseInt(required(properties, name), 16);
			if (palette.put(rgb, index) != null) throw new IllegalArgumentException("HUD palette contains duplicate colors");
		}
		Map<String, Integer> barCellAdvances = new LinkedHashMap<>();
		for (String name : properties.stringPropertyNames()) if (name.startsWith("bar.cell.advance."))
			barCellAdvances.put(name.substring("bar.cell.advance.".length()), integer(properties, name));
		return new HudProtocolConfiguration(required(properties, "protocol.version"), required(properties, "minecraft.version"),
				UUID.fromString(required(properties, "pack.id")), Key.key(required(properties, "font.id")), integer(properties, "marker.red"),
				integer(properties, "actionbar.baseline.offset"), integer(properties, "encoded.ascent"), glyphs, palette,
				integer(properties, "bar.cell.advance"), barCellAdvances);
	}

	public int barCellAdvance(String styleId) { return barCellAdvances.getOrDefault(styleId, barCellAdvance); }

	public char requireGlyph(String id) {
		Character glyph = glyphs.get(id);
		if (glyph == null) throw new IllegalArgumentException("Unknown HUD protocol glyph: " + id);
		return glyph;
	}
	public int requirePalette(int rgb) {
		Integer index = palette.get(rgb);
		if (index == null) throw new IllegalArgumentException("HUD color is not in the protocol palette: #" + String.format("%06X", rgb));
		return index;
	}
	private static String required(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing HUD protocol property: " + key);
		return value.trim();
	}
	private static int integer(Properties properties, String key) { return Integer.parseInt(required(properties, key)); }
}
