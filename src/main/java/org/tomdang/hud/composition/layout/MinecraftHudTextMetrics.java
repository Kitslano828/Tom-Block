package org.tomdang.hud.composition.layout;

import org.tomdang.hud.composition.HudSize;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.text.TomBlockBitmapTextWidthService;
import org.tomdang.hud.text.TomBlockHudFonts;
import org.tomdang.hud.text.HudProtocolTextSanitizer;

/** Measures engine text with the exact bitmap atlas advances generated for the resource pack. */
public final class MinecraftHudTextMetrics implements HudTextMetrics {
	private final TomBlockBitmapTextWidthService widths = new TomBlockBitmapTextWidthService();
	@Override public HudSize measure(HudTextCommand text) {
		var profile = TomBlockHudFonts.profile(text.style().font()).orElse(null);
		var selected = profile == null ? widths : profile.widths();
		int raw = selected.measure(HudProtocolTextSanitizer.sanitize(text.text()));
		int lines = text.wrap() && text.maxWidth() > 0 ? Math.max(1, (raw + text.maxWidth() - 1) / text.maxWidth()) : 1;
		int width = text.maxWidth() > 0 ? Math.min(raw, text.maxWidth()) : raw;
		return new HudSize(width, lines * (profile == null ? 9 : profile.lineHeight()));
	}
}
