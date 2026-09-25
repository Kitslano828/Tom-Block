package org.tomdang.hud.text;

import net.kyori.adventure.key.Key;
import java.util.LinkedHashMap;
import java.util.Map;

/** Authoritative logical advances for every font allowed inside a precomposed HUD component. */
public final class HudFontAdvanceRegistry {
	@FunctionalInterface public interface Metrics { int measure(String text); }
	private final Map<Key, Metrics> metrics = new LinkedHashMap<>();
	private boolean sealed;

	public void register(Key font, Metrics value) {
		if (sealed) throw new IllegalStateException("HUD font advance registry is sealed");
		if (font == null || value == null) throw new IllegalArgumentException("HUD font metrics are incomplete");
		if (metrics.putIfAbsent(font, value) != null) throw new IllegalArgumentException("Duplicate HUD font metrics: " + font);
	}

	public int measure(Key font, String text) {
		Metrics value = metrics.get(font);
		if (value == null) throw new IllegalArgumentException("No cursor-advance metrics registered for HUD font " + font);
		return value.measure(text);
	}

	public void seal() { sealed = true; }
}
