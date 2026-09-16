package org.tomdang.region.position;

public record BlockPosition(String worldId, int x, int y, int z) {
	public BlockPosition {
		if (worldId == null || worldId.isBlank()) {
			throw new IllegalArgumentException("worldId cannot be null or blank");
		}
		worldId = worldId.trim();
	}
}
