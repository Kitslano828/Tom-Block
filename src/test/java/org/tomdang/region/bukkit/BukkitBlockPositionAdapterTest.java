package org.tomdang.region.bukkit;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BukkitBlockPositionAdapterTest {
	private final BukkitBlockPositionAdapter adapter = new BukkitBlockPositionAdapter();

	@Test
	void convertsUsingMinecraftBlockCoordinates() {
		World world = mock(World.class);
		when(world.getName()).thenReturn("world_nether");

		BlockPosition result = adapter.fromLocation(new Location(world, -0.2, 64.9, -1.01));

		assertEquals(new BlockPosition("world_nether", -1, 64, -2), result);
	}

	@Test
	void rejectsNullLocationAndMissingWorld() {
		assertThrows(IllegalArgumentException.class, () -> adapter.fromLocation(null));
		assertThrows(IllegalArgumentException.class, () -> adapter.fromLocation(new Location(null, 0, 0, 0)));
	}
}
