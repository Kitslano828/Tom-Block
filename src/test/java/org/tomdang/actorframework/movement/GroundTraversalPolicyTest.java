package org.tomdang.actorframework.movement;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class GroundTraversalPolicyTest {
	private final GroundTraversalPolicy policy = new GroundTraversalPolicy();

	@Test
	void requiresLoadedChunkAndNeverQueriesItsTerrain() {
		Terrain terrain = new Terrain();
		when(terrain.world.isChunkLoaded(anyInt(), anyInt())).thenReturn(false);
		assertFalse(policy.canStandAt(terrain.world, at(0, 64, 0)));
		verify(terrain.world, never()).getBlockAt(anyInt(), anyInt(), anyInt());
	}

	@Test
	void requiresSupportAndTwoClearBlocksWithinWorldHeight() {
		Terrain terrain = new Terrain();
		assertTrue(policy.canStandAt(terrain.world, at(0, 64, 0)));
		terrain.set(0, 65, 0, true);
		assertFalse(policy.canStandAt(terrain.world, at(0, 64, 0)));
		terrain.set(0, 65, 0, false);
		terrain.set(0, 63, 0, false);
		assertFalse(policy.canStandAt(terrain.world, at(0, 64, 0)));
		assertFalse(policy.canStandAt(terrain.world, at(0, -64, 0)));
		assertFalse(policy.canStandAt(terrain.world, at(0, 319, 0)));
	}

	@Test
	void permitsAdjacentStepsButNotJumpsOrSolidOverhead() {
		Terrain terrain = new Terrain();
		assertTrue(policy.canTransition(terrain.world, at(0, 64, 0), at(1, 64, 0)));
		assertFalse(policy.canTransition(terrain.world, at(0, 64, 0), at(2, 64, 0)));
		assertFalse(policy.canTransition(terrain.world, at(0, 64, 0), at(1, 66, 0)));
		terrain.set(1, 64, 0, true);
		assertTrue(policy.canTransition(terrain.world, at(0, 64, 0), at(1, 65, 0)));
		terrain.set(0, 66, 0, true);
		assertFalse(policy.canTransition(terrain.world, at(0, 64, 0), at(1, 65, 0)));
		terrain.set(0, 66, 0, false);
		assertTrue(policy.canTransition(terrain.world, at(1, 65, 0), at(0, 64, 0)));
	}

	@Test
	void doesNotInspectDestinationTerrainAcrossAnUnloadedChunkBorder() {
		Terrain terrain = new Terrain();
		when(terrain.world.isChunkLoaded(1, 0)).thenReturn(false);
		assertFalse(policy.canTransition(terrain.world, at(15, 64, 0), at(16, 64, 0)));
		verify(terrain.world, never()).getBlockAt(eq(16), anyInt(), eq(0));
	}

	@Test
	void diagonalCannotCutABlockedCorner() {
		Terrain terrain = new Terrain();
		assertTrue(policy.canTransition(terrain.world, at(0, 64, 0), at(1, 64, 1)));
		terrain.set(1, 64, 0, true);
		assertFalse(policy.canTransition(terrain.world, at(0, 64, 0), at(1, 64, 1)));
	}

	@Test
	void rejectsMismatchedWorld() {
		Terrain terrain = new Terrain();
		assertThrows(IllegalArgumentException.class,
				() -> policy.canStandAt(terrain.world, new BlockPosition("other", 0, 64, 0)));
	}

	private BlockPosition at(int x, int y, int z) {
		return new BlockPosition("world", x, y, z);
	}

	private static final class Terrain {
		private final World world = mock(World.class);
		private final Map<String, Boolean> blocks = new HashMap<>();
		private final Block solid = mock(Block.class);
		private final Block open = mock(Block.class);

		private Terrain() {
			when(solid.isSolid()).thenReturn(true);
			when(open.isPassable()).thenReturn(true);
			when(world.getName()).thenReturn("world");
			when(world.getMinHeight()).thenReturn(-64);
			when(world.getMaxHeight()).thenReturn(320);
			when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(true);
			when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenAnswer(invocation -> {
				int x = invocation.getArgument(0), y = invocation.getArgument(1), z = invocation.getArgument(2);
				return blocks.getOrDefault(key(x, y, z), y == 63) ? solid : open;
			});
		}

		private void set(int x, int y, int z, boolean solid) {
			blocks.put(key(x, y, z), solid);
		}

		private String key(int x, int y, int z) {
			return x + ":" + y + ":" + z;
		}
	}
}
