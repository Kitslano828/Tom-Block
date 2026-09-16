package org.tomdang.region.shape;

import org.tomdang.region.position.BlockPosition;

public final class CuboidRegionShape implements RegionShape {
	private final String worldId;
	private final int minimumX;
	private final int minimumY;
	private final int minimumZ;
	private final int maximumX;
	private final int maximumY;
	private final int maximumZ;

	public CuboidRegionShape(BlockPosition firstCorner, BlockPosition secondCorner) {
		if (firstCorner == null) throw new IllegalArgumentException("firstCorner cannot be null");
		if (secondCorner == null) throw new IllegalArgumentException("secondCorner cannot be null");
		if (!firstCorner.worldId().equals(secondCorner.worldId())) {
			throw new IllegalArgumentException("cuboid corners must be in the same world");
		}

		worldId = firstCorner.worldId();
		minimumX = Math.min(firstCorner.x(), secondCorner.x());
		minimumY = Math.min(firstCorner.y(), secondCorner.y());
		minimumZ = Math.min(firstCorner.z(), secondCorner.z());
		maximumX = Math.max(firstCorner.x(), secondCorner.x());
		maximumY = Math.max(firstCorner.y(), secondCorner.y());
		maximumZ = Math.max(firstCorner.z(), secondCorner.z());
	}

	@Override
	public boolean contains(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		return worldId.equals(position.worldId())
				&& position.x() >= minimumX && position.x() <= maximumX
				&& position.y() >= minimumY && position.y() <= maximumY
				&& position.z() >= minimumZ && position.z() <= maximumZ;
	}

	public BlockPosition minimum() {
		return new BlockPosition(worldId, minimumX, minimumY, minimumZ);
	}

	public BlockPosition maximum() {
		return new BlockPosition(worldId, maximumX, maximumY, maximumZ);
	}
}
