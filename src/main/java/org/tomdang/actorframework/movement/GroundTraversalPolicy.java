package org.tomdang.actorframework.movement;

import org.bukkit.World;
import org.tomdang.region.position.BlockPosition;

/** Conservative full-block rules for a two-block-tall ground actor. Never loads chunks. */
public final class GroundTraversalPolicy {
	/** Position is the block containing the actor's feet, not the block supporting them. */
	public boolean canStandAt(World world, BlockPosition position) {
		validate(world, position);
		int x = position.x(), y = position.y(), z = position.z();
		if (!world.isChunkLoaded(x >> 4, z >> 4)) return false;
		if (y <= world.getMinHeight() || y + 1 >= world.getMaxHeight()) return false;
		return world.getBlockAt(x, y - 1, z).isSolid()
				&& world.getBlockAt(x, y, z).isPassable()
				&& world.getBlockAt(x, y + 1, z).isPassable();
	}

	/** Adjacent horizontal move, with at most one block of elevation change. */
	public boolean canTransition(World world, BlockPosition from, BlockPosition to) {
		validate(world, from);
		validate(world, to);
		long dx = (long) to.x() - from.x();
		long dy = (long) to.y() - from.y();
		long dz = (long) to.z() - from.z();
		if (Math.abs(dx) > 1 || Math.abs(dz) > 1 || (dx == 0 && dz == 0) || Math.abs(dy) > 1) return false;
		if (!canStandAt(world, from) || !canStandAt(world, to)) return false;
		if (dy > 0 && !world.getBlockAt(from.x(), from.y() + 2, from.z()).isPassable()) return false;
		if (dx != 0 && dz != 0) {
			// Require both orthogonal corridors; a diagonal must not cut a solid corner.
			BlockPosition xSide = new BlockPosition(from.worldId(), to.x(), from.y(), from.z());
			BlockPosition zSide = new BlockPosition(from.worldId(), from.x(), from.y(), to.z());
			if (!canStandAt(world, xSide) || !canStandAt(world, zSide)) return false;
		}
		return true;
	}

	private void validate(World world, BlockPosition position) {
		if (world == null || position == null) throw new IllegalArgumentException("World and position are required");
		if (!position.worldId().equals(world.getName()))
			throw new IllegalArgumentException("Position belongs to a different world");
	}
}
