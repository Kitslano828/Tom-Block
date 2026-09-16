package org.tomdang.region.bukkit;

import org.bukkit.Location;
import org.bukkit.World;
import org.tomdang.region.position.BlockPosition;

public final class BukkitBlockPositionAdapter {
	public BlockPosition fromLocation(Location location) {
		if (location == null) throw new IllegalArgumentException("location cannot be null");
		World world = location.getWorld();
		if (world == null) throw new IllegalArgumentException("location must have a world");
		return new BlockPosition(world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
}
