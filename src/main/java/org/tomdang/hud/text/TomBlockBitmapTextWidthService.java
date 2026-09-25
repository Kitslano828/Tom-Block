package org.tomdang.hud.text;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Exact horizontal advances generated from the bitmap atlas shipped in TomBlock's pack. */
public final class TomBlockBitmapTextWidthService implements HudTextWidthService {
	private static final String RESOURCE = "/hud-font-advances.properties";
	private final Map<Integer, Integer> advances;
	private final MinecraftDefaultTextWidthService fallback = new MinecraftDefaultTextWidthService();

	public TomBlockBitmapTextWidthService() { this(RESOURCE); }

	public TomBlockBitmapTextWidthService(String resource) { this(load(resource)); }

	TomBlockBitmapTextWidthService(Map<Integer, Integer> advances) {
		this.advances = Map.copyOf(advances);
	}

	@Override public int measure(String text) {
		if (text == null) throw new IllegalArgumentException("Text cannot be null");
		int width = 0;
		for (int codePoint : text.codePoints().toArray()) {
			Integer advance = advances.get(codePoint);
			width += advance == null ? fallback.measure(new String(Character.toChars(codePoint))) : advance;
		}
		return width;
	}

	private static Map<Integer, Integer> load(String resource) {
		if (resource == null || resource.isBlank() || resource.charAt(0) != '/')
			throw new IllegalArgumentException("Font metric resource must be an absolute classpath path");
		Properties properties = new Properties();
		try (InputStream input = TomBlockBitmapTextWidthService.class.getResourceAsStream(resource)) {
			if (input == null) throw new IllegalStateException("Missing generated HUD font advances " + resource);
			properties.load(input);
		} catch (IOException exception) {
			throw new IllegalStateException("Cannot load generated HUD font advances", exception);
		}
		Map<Integer, Integer> result = new HashMap<>();
		properties.forEach((key, value) -> result.put(Integer.parseInt(key.toString(), 16),
				Integer.parseInt(value.toString())));
		return result;
	}
}
