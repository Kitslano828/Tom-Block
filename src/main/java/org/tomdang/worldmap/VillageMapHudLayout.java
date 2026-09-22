package org.tomdang.worldmap;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.region.position.BlockPosition;

/** Chooses a calibrated world tile and overlays a direction-aware player marker. */
public final class VillageMapHudLayout {
	public static final int PIXELS = 64;
	public static final int MINIMUM_X = 73;
	public static final int MINIMUM_Z = -213;
	private static final int BLOCKS = 256;
	private static final int MARKER_ROWS = 16;
	private static final int DIRECTIONS = 8;
	private static final int FIRST_TILE_X = -7;
	private static final int FIRST_TILE_Z = -4;
	private static final int TILE_COLUMNS = 14;
	private static final int TILE_ROWS = 10;
	private static final int MIN_WORLD_X = -1536;
	private static final int MAX_WORLD_X = 1663;
	private static final int MIN_WORLD_Z = -1152;
	private static final int MAX_WORLD_Z = 1151;
	private static final Key MAP_FONT = Key.key("tomblock", "hud_world_map");
	private static final Key MARKER_FONT = Key.key("tomblock", "hud_follow_markers");
	private final HudSpacingService spacing = new HudSpacingService();

	public Component compose(BlockPosition position) {
		return compose(position, 0);
	}

	public Component compose(BlockPosition position, float yaw) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		Tile tile = tileAt(position);
		if (tile == null) return Component.empty();
		Component map = Component.text(String.valueOf((char) ('\uE500' + tile.index())))
				.font(MAP_FONT).color(NamedTextColor.WHITE);
		Marker marker = markerAt(position);
		int direction = Math.floorMod(Math.round(yaw / 45f), DIRECTIONS);
		// Bitmap glyph advance includes one extra pixel; rewind to the image's left edge.
		return map.append(spacing.createSpacing(-PIXELS - 1 + marker.x() - 2))
				.append(Component.text(String.valueOf((char) ('\uE700' + marker.row() * DIRECTIONS + direction)))
						.font(MARKER_FONT).color(NamedTextColor.WHITE))
				.append(spacing.createSpacing(PIXELS - marker.x() + 2));
	}

	public Tile tileAt(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		if (!position.worldId().equals("world") || position.x() < MIN_WORLD_X
				|| position.x() > MAX_WORLD_X || position.z() < MIN_WORLD_Z
				|| position.z() > MAX_WORLD_Z) return null;
		int column = Math.floorDiv(position.x() - MINIMUM_X, BLOCKS) - FIRST_TILE_X;
		int row = Math.floorDiv(position.z() - MINIMUM_Z, BLOCKS) - FIRST_TILE_Z;
		if (column < 0 || column >= TILE_COLUMNS || row < 0 || row >= TILE_ROWS)
			throw new IllegalStateException("World tile grid does not cover calibrated bounds");
		return new Tile(column, row, row * TILE_COLUMNS + column);
	}

	public Marker markerAt(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		Tile tile = tileAt(position);
		if (tile == null) return null;
		int tileX = MINIMUM_X + (tile.column() + FIRST_TILE_X) * BLOCKS;
		int tileZ = MINIMUM_Z + (tile.row() + FIRST_TILE_Z) * BLOCKS;
		int x = (position.x() - tileX) * PIXELS / BLOCKS;
		int y = (position.z() - tileZ) * PIXELS / BLOCKS;
		return new Marker(x, y * MARKER_ROWS / PIXELS);
	}

	public record Marker(int x, int row) {}
	public record Tile(int column, int row, int index) {}
}
