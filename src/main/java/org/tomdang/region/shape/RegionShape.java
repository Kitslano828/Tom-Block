package org.tomdang.region.shape;

import org.tomdang.region.position.BlockPosition;

@FunctionalInterface
public interface RegionShape {
	boolean contains(BlockPosition position);
}
