package org.tomdang.region.shape;

import org.tomdang.region.position.BlockPosition;

import java.util.Collection;
import java.util.List;

public final class CompositeRegionShape implements RegionShape {
	private final List<RegionShape> shapes;

	public CompositeRegionShape(Collection<? extends RegionShape> shapes) {
		if (shapes == null) throw new IllegalArgumentException("shapes cannot be null");
		if (shapes.isEmpty()) throw new IllegalArgumentException("shapes cannot be empty");
		if (shapes.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("shapes cannot contain null entries");
		}
		this.shapes = List.copyOf(shapes);
	}

	@Override
	public boolean contains(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		return shapes.stream().anyMatch(shape -> shape.contains(position));
	}

	public List<RegionShape> shapes() {
		return shapes;
	}
}
