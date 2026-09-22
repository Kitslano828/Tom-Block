package org.tomdang.region.shape;

import org.tomdang.region.position.BlockPosition;

import java.util.List;

/** Inclusive X/Z polygon extruded between two Y levels. */
public final class PolygonPrismRegionShape implements RegionShape {
	private final String worldId;
	private final int minimumY;
	private final int maximumY;
	private final List<RegionPolygonVertex> vertices;

	public PolygonPrismRegionShape(String worldId, int minimumY, int maximumY,
			List<RegionPolygonVertex> vertices) {
		if (worldId == null || worldId.isBlank()) throw new IllegalArgumentException("worldId cannot be blank");
		if (minimumY > maximumY) throw new IllegalArgumentException("minimumY cannot exceed maximumY");
		if (vertices == null || vertices.size() < 3 || vertices.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("polygon needs at least three non-null vertices");
		}
		this.worldId = worldId.trim();
		this.minimumY = minimumY;
		this.maximumY = maximumY;
		this.vertices = List.copyOf(vertices);
	}

	@Override
	public boolean contains(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		if (!worldId.equals(position.worldId()) || position.y() < minimumY || position.y() > maximumY) {
			return false;
		}
		boolean inside = false;
		for (int i = 0, j = vertices.size() - 1; i < vertices.size(); j = i++) {
			RegionPolygonVertex a = vertices.get(j);
			RegionPolygonVertex b = vertices.get(i);
			long dx = (long) b.x() - a.x();
			long dz = (long) b.z() - a.z();
			long px = (long) position.x() - a.x();
			long pz = (long) position.z() - a.z();
			long cross = dx * pz - dz * px;
			if (cross == 0 && position.x() >= Math.min(a.x(), b.x())
					&& position.x() <= Math.max(a.x(), b.x())
					&& position.z() >= Math.min(a.z(), b.z())
					&& position.z() <= Math.max(a.z(), b.z())) return true;
			if ((a.z() > position.z()) != (b.z() > position.z())) {
				double crossingX = a.x() + (double) dx * (position.z() - a.z()) / dz;
				if (position.x() < crossingX) inside = !inside;
			}
		}
		return inside;
	}

	public String worldId() { return worldId; }
	public int minimumY() { return minimumY; }
	public int maximumY() { return maximumY; }
	public List<RegionPolygonVertex> vertices() { return vertices; }
}
