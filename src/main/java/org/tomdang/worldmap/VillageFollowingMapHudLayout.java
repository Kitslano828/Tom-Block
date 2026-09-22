package org.tomdang.worldmap;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.region.position.BlockPosition;

/** Player-following, quantized village minimap prototype. */
public final class VillageFollowingMapHudLayout {
	private static final int PIXELS = 64;
	private static final int BLOCKS = 192;
	private static final int STEP = 32;
	private static final int CENTERS = 9;
	private static final int FIRST_CENTER_X = 73;
	private static final int FIRST_CENTER_Z = -213;
	private static final int DIRECTIONS = 8;
	private static final Key MAP_FONT = Key.key("tomblock", "hud_follow_map");
	private static final Key MARKER_FONT = Key.key("tomblock", "hud_follow_markers");
	private final HudSpacingService spacing = new HudSpacingService();

	public Component compose(BlockPosition position, float yaw) {
		Selection selection = select(position);
		if (selection == null) return Component.empty();
		Component map = Component.text(String.valueOf((char) ('\uE600' + selection.index())))
				.font(MAP_FONT).color(NamedTextColor.WHITE);
		int direction = Math.floorMod(Math.round(yaw / 45f), DIRECTIONS);
		int markerCodePoint = 0xE700 + selection.markerRow() * DIRECTIONS + direction;
		return map.append(spacing.createSpacing(-PIXELS - 1 + selection.markerX() - 2))
				.append(Component.text(String.valueOf((char) markerCodePoint))
						.font(MARKER_FONT).color(NamedTextColor.WHITE))
				// Restore a stable line width: the sidebar positions itself from total advance.
				.append(spacing.createSpacing(PIXELS - selection.markerX() + 2));
	}

	public Selection select(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		if (!position.worldId().equals("world") || position.x() < FIRST_CENTER_X
				|| position.x() >= FIRST_CENTER_X + (CENTERS - 1) * STEP
				|| position.z() < FIRST_CENTER_Z
				|| position.z() >= FIRST_CENTER_Z + (CENTERS - 1) * STEP) return null;
		int column = Math.clamp(Math.round((position.x() - FIRST_CENTER_X) / (float) STEP), 0, CENTERS - 1);
		int row = Math.clamp(Math.round((position.z() - FIRST_CENTER_Z) / (float) STEP), 0, CENTERS - 1);
		int centerX = FIRST_CENTER_X + column * STEP;
		int centerZ = FIRST_CENTER_Z + row * STEP;
		int markerX = PIXELS / 2 + (position.x() - centerX) * PIXELS / BLOCKS;
		int markerY = PIXELS / 2 + (position.z() - centerZ) * PIXELS / BLOCKS;
		return new Selection(row * CENTERS + column, markerX, markerY * 16 / PIXELS);
	}

	public record Selection(int index, int markerX, int markerRow) {}
}
