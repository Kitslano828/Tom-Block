package org.tomdang.actorframework.movement;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class GroundPathPlannerTest {
	private final GroundPathPlanner planner = new GroundPathPlanner(new GroundTraversalPolicy());

	@Test
	void findsClearRouteWithExactEndpoints() {
		Terrain terrain = new Terrain();
		GroundPathResult result = planner.findPath(terrain.world, at(0, 64, 0), at(3, 64, 0), 100);
		assertEquals(GroundPathResult.Status.FOUND, result.status());
		assertEquals(at(0, 64, 0), result.path().getFirst());
		assertEquals(at(3, 64, 0), result.path().getLast());
		assertEquals(4, result.path().size());
	}

	@Test
	void includesAReachableOneBlockStep() {
		Terrain terrain = new Terrain();
		terrain.block(1, 64, 0);
		GroundPathResult result = planner.findPath(terrain.world, at(0, 64, 0), at(1, 65, 0), 50);
		assertEquals(GroundPathResult.Status.FOUND, result.status());
		assertEquals(java.util.List.of(at(0, 64, 0), at(1, 65, 0)), result.path());
	}

	@Test
	void routesAroundWallWithoutCuttingItsCorner() {
		Terrain terrain = new Terrain();
		terrain.block(1, 64, 0);
		terrain.block(1, 65, 0);
		GroundPathResult result = planner.findPath(terrain.world, at(0, 64, 0), at(2, 64, 0), 100);
		assertEquals(GroundPathResult.Status.FOUND, result.status());
		assertFalse(result.path().contains(at(1, 64, 0)));
		assertTrue(result.path().stream().anyMatch(position -> position.z() != 0));
		for (int index = 1; index < result.path().size(); index++)
			assertTrue(new GroundTraversalPolicy().canTransition(terrain.world,
					result.path().get(index - 1), result.path().get(index)));
	}

	@Test
	void distinguishesBlockedEndpointsFromExhaustedSearch() {
		Terrain terrain = new Terrain();
		terrain.block(0, 64, 0);
		assertEquals(GroundPathResult.Status.BLOCKED_START,
				planner.findPath(terrain.world, at(0, 64, 0), at(2, 64, 0), 100).status());
		terrain.unblock(0, 64, 0);
		terrain.block(2, 64, 0);
		assertEquals(GroundPathResult.Status.BLOCKED_GOAL,
				planner.findPath(terrain.world, at(0, 64, 0), at(2, 64, 0), 100).status());
	}

	@Test
	void returnsBudgetStatusInsteadOfSearchingIndefinitely() {
		Terrain terrain = new Terrain();
		GroundPathResult result = planner.findPath(terrain.world, at(0, 64, 0), at(12, 64, 0), 1);
		assertEquals(GroundPathResult.Status.SEARCH_LIMIT_REACHED, result.status());
		assertEquals(1, result.exploredNodes());
		assertTrue(result.path().isEmpty());
	}

	@Test
	void reportsNoRouteWhenLoadedIslandIsSeparatedByWall() {
		Terrain terrain = new Terrain();
		terrain.onlyChunkZeroLoaded = true;
		for (int z = 0; z < 16; z++) {
			terrain.block(5, 64, z);
			terrain.block(5, 65, z);
		}
		GroundPathResult result = planner.findPath(terrain.world, at(3, 64, 8), at(7, 64, 8), 1000);
		assertEquals(GroundPathResult.Status.NO_ROUTE, result.status());
		assertTrue(result.path().isEmpty());
		verify(terrain.world, never()).getBlockAt(eq(16), anyInt(), anyInt());
	}

	@Test
	void rejectsInvalidRequestAndHandlesAlreadyArrived() {
		Terrain terrain = new Terrain();
		assertThrows(IllegalArgumentException.class,
				() -> planner.findPath(terrain.world, at(0, 64, 0), at(1, 64, 0), 0));
		assertThrows(IllegalArgumentException.class,
				() -> planner.findPath(terrain.world, at(0, 64, 0), new BlockPosition("other", 1, 64, 0), 10));
		assertEquals(java.util.List.of(at(0, 64, 0)),
				planner.findPath(terrain.world, at(0, 64, 0), at(0, 64, 0), 10).path());
	}

	private BlockPosition at(int x, int y, int z) {
		return new BlockPosition("world", x, y, z);
	}

	private static final class Terrain {
		private final World world = mock(World.class);
		private final Block solid = mock(Block.class);
		private final Block open = mock(Block.class);
		private final Set<String> blockedFeet = new HashSet<>();
		private boolean onlyChunkZeroLoaded;

		private Terrain() {
			when(world.getName()).thenReturn("world");
			when(world.getMinHeight()).thenReturn(-64);
			when(world.getMaxHeight()).thenReturn(320);
			when(solid.isSolid()).thenReturn(true);
			when(open.isPassable()).thenReturn(true);
			when(world.isChunkLoaded(anyInt(), anyInt())).thenAnswer(invocation ->
					!onlyChunkZeroLoaded || ((int) invocation.getArgument(0) == 0
							&& (int) invocation.getArgument(1) == 0));
			when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenAnswer(invocation -> {
				int x = invocation.getArgument(0), y = invocation.getArgument(1), z = invocation.getArgument(2);
				return y == 63 || blockedFeet.contains(key(x, y, z)) ? solid : open;
			});
		}

		private void block(int x, int y, int z) { blockedFeet.add(key(x, y, z)); }
		private void unblock(int x, int y, int z) { blockedFeet.remove(key(x, y, z)); }
		private String key(int x, int y, int z) { return x + ":" + y + ":" + z; }
	}
}
