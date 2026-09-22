package org.tomdang.foraging;

import org.bukkit.Location;

public record ForagingTree(String id, Location root, TreeModel model) {
	public ForagingTree {
		root = root.toBlockLocation();
	}

	public Location location(BlockOffset offset) {
		return root.clone().add(offset.x(), offset.y(), offset.z());
	}

	public BlockOffset offset(Location location) {
		return new BlockOffset(location.getBlockX() - root.getBlockX(), location.getBlockY() - root.getBlockY(),
				location.getBlockZ() - root.getBlockZ());
	}
}
