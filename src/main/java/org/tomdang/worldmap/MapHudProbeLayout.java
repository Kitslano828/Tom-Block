package org.tomdang.worldmap;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.hud.spacing.HudSpacingService;

/** Fixed 64-pixel map used to isolate sidebar layout from live map logic. */
public final class MapHudProbeLayout {
	private static final int PIXELS = 64;
	private static final int VILLAGE_GLYPH = 0xE600 + 4 * 9 + 4;
	private static final int CENTER_MARKER = 0xE700 + 8 * 8;
	private static final Key MAP_FONT = Key.key("tomblock", "hud_follow_map");
	private static final Key MARKER_FONT = Key.key("tomblock", "hud_follow_markers");
	private final HudSpacingService spacing = new HudSpacingService();

	public Component staticMap() {
		return Component.text(String.valueOf((char) VILLAGE_GLYPH))
				.font(MAP_FONT).color(NamedTextColor.WHITE);
	}

	public Component mapWithMarker() {
		int center = PIXELS / 2;
		return staticMap().append(spacing.createSpacing(-PIXELS - 1 + center - 2))
				.append(Component.text(String.valueOf((char) CENTER_MARKER))
						.font(MARKER_FONT).color(NamedTextColor.WHITE))
				.append(spacing.createSpacing(PIXELS - center + 2));
	}
}
