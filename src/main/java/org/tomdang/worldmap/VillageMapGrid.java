package org.tomdang.worldmap;

import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.CuboidRegionShape;

/** A conceptual village layout, not a survey of the built world. North is at the top. */
public final class VillageMapGrid {
	private static final String[] CONCEPT = {
			"TTTTTTTTTTTTT",
			"T..HH..R.HH.T",
			"T..HH..R.HH.T",
			"T......R....T",
			"RRRRRRRRRRRRR",
			"T....H.R....T",
			"T....H.R.WW.T",
			"T......R.WW.T",
			"RRRRRRRRRRRRR",
			"T..HH..R.HH.T",
			"T..HH..R.HH.T",
			"T......R....T",
			"TTTTTTTTTTTTT"
	};
	private final RegionDefinition region;
	private final CuboidRegionShape bounds;

	public VillageMapGrid(RegionDefinition region) {
		if (region == null) throw new IllegalArgumentException("region cannot be null");
		if (!(region.shape() instanceof CuboidRegionShape cuboid)) {
			throw new IllegalArgumentException("The map prototype requires one cuboid region");
		}
		this.region = region;
		this.bounds = cuboid;
	}

	public String row(BlockPosition player, int row, int size) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		if (size < 2 || row < 0 || row >= size) throw new IllegalArgumentException("invalid grid row or size");
		StringBuilder result = new StringBuilder(size);
		int playerColumn = column(player.x(), bounds.minimum().x(), bounds.maximum().x(), size);
		int playerRow = column(player.z(), bounds.minimum().z(), bounds.maximum().z(), size);
		boolean sameWorld = player.worldId().equals(bounds.minimum().worldId());
		for (int column = 0; column < size; column++) {
			int x = sample(bounds.minimum().x(), bounds.maximum().x(), column, size);
			int z = sample(bounds.minimum().z(), bounds.maximum().z(), row, size);
			BlockPosition sample = new BlockPosition(bounds.minimum().worldId(), x, player.y(), z);
			if (sameWorld && row == playerRow && column == playerColumn
					&& player.x() >= bounds.minimum().x() && player.x() <= bounds.maximum().x()
					&& player.z() >= bounds.minimum().z() && player.z() <= bounds.maximum().z()) {
				result.append('P');
			} else {
				result.append(region.directlyContains(sample) ? conceptCell(row, column, size) : '.');
			}
		}
		return result.toString();
	}

	private static char conceptCell(int row, int column, int size) {
		int modelRow = row * CONCEPT.length / size;
		int modelColumn = column * CONCEPT[modelRow].length() / size;
		return CONCEPT[modelRow].charAt(modelColumn);
	}

	private static int column(int coordinate, int minimum, int maximum, int size) {
		return (int) Math.min(size - 1, Math.max(0,
				(long) (coordinate - minimum) * size / (maximum - minimum + 1L)));
	}

	private static int sample(int minimum, int maximum, int index, int size) {
		return minimum + (int) ((long) (maximum - minimum) * (2L * index + 1) / (2L * size));
	}
}
