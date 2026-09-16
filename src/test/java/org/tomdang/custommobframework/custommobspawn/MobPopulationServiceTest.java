package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.configuration.MobPopulationRule;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.region.resolution.RegionResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobPopulationServiceTest {
	@Test
	void neverQueriesTerrainInUnloadedChunks() {
		TomBlock plugin = mock(TomBlock.class);
		World world = mock(World.class);
		Player player = mock(Player.class);
		when(player.getWorld()).thenReturn(world);
		when(player.getLocation()).thenReturn(new Location(world, 100, 75, 100));
		when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(false);
		MobPopulationRule rule = new MobPopulationRule("ZOMBIE", "AREA", 3, 100,
				48, 80, 200, 16, 40);
		MobPopulationService service = new MobPopulationService(plugin,
				mock(CustomMobRegistry.class), mock(CustomMobSpawner.class),
				mock(CustomMobContextRegistry.class), mock(RegionResolver.class),
				NamespacedKey.minecraft("mob_id"), NamespacedKey.minecraft("population_rule_id"),
				List.of(rule));

		assertNull(service.findGroundCandidate(rule, player));
		verify(world, never()).getHighestBlockYAt(anyInt(), anyInt());
	}
}
