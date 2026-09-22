package org.tomdang.foraging;

public record BlockOffset(int x, int y, int z) {
	public int distance(BlockOffset other) {
		return Math.abs(x - other.x) + Math.abs(y - other.y) + Math.abs(z - other.z);
	}
}
