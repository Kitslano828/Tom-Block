package org.tomdang.worldmap;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.region.position.BlockPosition;

/** Opt-in sidebar probe: quantized map crop with a fixed-center player marker. */
public final class VillageCenteredMapHudLayout {
	private static final int FIRST_X = 73;
	private static final int FIRST_Z = -213;
	private static final int STEP = 16;
	private static final int VIEWS = 17;
	private static final int PIXELS = 144;
	private static final Key MAP_FONT = Key.key("tomblock", "hud_centered_map");
	private static final Key MARKER_FONT = Key.key("tomblock", "hud_centered_markers");
	private final HudSpacingService spacing = new HudSpacingService();

	public Component compose(BlockPosition position, float yaw) {
		int index = indexAt(position);
		if (index < 0) return Component.empty();
		int direction = Math.floorMod(Math.round(yaw / 45f), 8);
		return Component.text(String.valueOf((char) (0xE800 + index)))
				.font(MAP_FONT).color(NamedTextColor.WHITE)
				.append(spacing.createSpacing(-PIXELS - 1 + PIXELS / 2 - 7))
				.append(Component.text(String.valueOf((char) (0xE700 + 8 * 8 + direction)))
						.font(MARKER_FONT).color(NamedTextColor.WHITE))
				// Marker glyphs have a fixed 16-pixel visible width plus one-pixel advance.
				.append(spacing.createSpacing(PIXELS + 1 - (PIXELS / 2 - 7) - 17));
	}

	public int indexAt(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		if (!position.worldId().equals("world") || position.x() < FIRST_X || position.z() < FIRST_Z
				|| position.x() >= FIRST_X + (VIEWS - 1) * STEP
				|| position.z() >= FIRST_Z + (VIEWS - 1) * STEP) return -1;
		int column = Math.clamp(Math.round((position.x() - FIRST_X) / (float) STEP), 0, VIEWS - 1);
		int row = Math.clamp(Math.round((position.z() - FIRST_Z) / (float) STEP), 0, VIEWS - 1);
		return row * VIEWS + column;
	}
}
