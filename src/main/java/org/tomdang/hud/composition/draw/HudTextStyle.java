package org.tomdang.hud.composition.draw;

import net.kyori.adventure.key.Key;

public record HudTextStyle(String styleId, int color, boolean bold, boolean shadow, Key font) {
	public static final HudTextStyle DEFAULT = new HudTextStyle("default", 0xFFFFFF, false, true, null);
	public HudTextStyle(String styleId, int color, boolean bold, boolean shadow) {
		this(styleId, color, bold, shadow, null);
	}
	public HudTextStyle {
		if (styleId == null || styleId.isBlank()) throw new IllegalArgumentException("Text style id cannot be blank");
		if (color < 0 || color > 0xFFFFFF) throw new IllegalArgumentException("Text color must be RGB");
	}
}
