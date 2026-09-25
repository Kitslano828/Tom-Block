package org.tomdang.hud.text;

import net.kyori.adventure.key.Key;
import java.util.Map;

/** Version-locked font advances shared by all engine-owned precomposed HUD components. */
public final class TomBlockHudFontAdvances {
	private static final Map<Character, Integer> SPACING = Map.ofEntries(
			Map.entry('\uE108', -256), Map.entry('\uE107', -128), Map.entry('\uE106', -64),
			Map.entry('\uE105', -32), Map.entry('\uE104', -16), Map.entry('\uE103', -8),
			Map.entry('\uE102', -4), Map.entry('\uE101', -2), Map.entry('\uE100', -1),
			Map.entry('\uE118', 256), Map.entry('\uE117', 128), Map.entry('\uE116', 64),
			Map.entry('\uE115', 32), Map.entry('\uE114', 16), Map.entry('\uE113', 8),
			Map.entry('\uE112', 4), Map.entry('\uE111', 2), Map.entry('\uE110', 1));

	public static HudComponentAdvanceService create() {
		MinecraftDefaultTextWidthService defaults = new MinecraftDefaultTextWidthService();
		TomBlockBitmapTextWidthService bitmap = new TomBlockBitmapTextWidthService();
		HudFontAdvanceRegistry registry = new HudFontAdvanceRegistry();
		registry.register(Key.key("minecraft", "default"), defaults::measure);
		registry.register(Key.key("tomblock", "spacing"), TomBlockHudFontAdvances::spacing);
		registry.register(Key.key("tomblock", "dialogue"), text -> dialogue(text, defaults));
		for (String font : new String[]{"dialogue_line_1", "dialogue_line_2", "dialogue_line_3",
				"dialogue_speaker", "dialogue_indicator"})
			registry.register(Key.key("tomblock", font), bitmap::measure);
		registry.seal();
		return new HudComponentAdvanceService(registry);
	}

	private static int spacing(String text) {
		int advance = 0;
		for (int index = 0; index < text.length(); index++) {
			Integer value = SPACING.get(text.charAt(index));
			if (value == null) throw new IllegalArgumentException("Unknown TomBlock spacing glyph U+"
					+ String.format("%04X", (int) text.charAt(index)));
			advance += value;
		}
		return advance;
	}

	private static int dialogue(String text, MinecraftDefaultTextWidthService fallback) {
		int advance = 0;
		StringBuilder ordinary = new StringBuilder();
		for (int index = 0; index < text.length(); index++) {
			char value = text.charAt(index);
			if (value == '\uE001') {
				advance += fallback.measure(ordinary.toString());
				ordinary.setLength(0);
				advance += 257;
			} else ordinary.append(value);
		}
		return advance + fallback.measure(ordinary.toString());
	}

	private TomBlockHudFontAdvances() {}
}
