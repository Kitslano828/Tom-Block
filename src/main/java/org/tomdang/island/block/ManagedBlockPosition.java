package org.tomdang.island.block;

import org.bukkit.block.Block;

public record ManagedBlockPosition(String worldName, int x, int y, int z) {
	public ManagedBlockPosition {
		if (worldName == null || worldName.isBlank()) throw new IllegalArgumentException("World name is required");
	}
	public static ManagedBlockPosition from(Block block) {
		return new ManagedBlockPosition(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
}
