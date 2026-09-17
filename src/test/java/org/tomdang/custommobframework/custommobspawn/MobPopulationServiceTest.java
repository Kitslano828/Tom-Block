package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.configuration.MobPopulationRule;
import org.tomdang.custommobframework.configuration.MobSpawnPlacement;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.region.resolution.RegionResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobPopulationServiceTest {
	@Test
	void nearbyCapCountsOnlyMatchingMobsWithinActivationRadius() {
		World world = mock(World.class);
		Entity nearby = mock(Entity.class);
		Entity farAway = mock(Entity.class);
		Entity otherMob = mock(Entity.class);
		NamespacedKey mobKey = NamespacedKey.minecraft("mob_id");
		when(nearby.isValid()).thenReturn(true);
		when(farAway.isValid()).thenReturn(true);
		when(otherMob.isValid()).thenReturn(true);
		PersistentDataContainer jellyfishData = mobData("JELLYFISH", mobKey);
		PersistentDataContainer zombieData = mobData("ZOMBIE", mobKey);
		when(nearby.getPersistentDataContainer()).thenReturn(jellyfishData);
		when(farAway.getPersistentDataContainer()).thenReturn(jellyfishData);
		when(otherMob.getPersistentDataContainer()).thenReturn(zombieData);
		when(nearby.getLocation()).thenReturn(new Location(world, 5, 75, 0));
		when(farAway.getLocation()).thenReturn(new Location(world, 100, 75, 0));
		when(world.getEntities()).thenReturn(List.of(nearby, farAway, otherMob));
		MobPopulationRule rule = new MobPopulationRule("JELLYFISH", "AREA", 30, 40,
				48, 80, 200, 12, 40, MobSpawnPlacement.AIR, 65, 135, 6);
		MobPopulationService service = new MobPopulationService(mock(TomBlock.class),
				mock(CustomMobRegistry.class), mock(CustomMobSpawner.class),
				mock(CustomMobContextRegistry.class), mock(RegionResolver.class),
				mobKey, NamespacedKey.minecraft("population_rule_id"), List.of(rule));
		assertEquals(1, service.countNearby(rule, new Location(world, 0, 75, 0)));
	}

	@SuppressWarnings("unchecked")
	private PersistentDataContainer mobData(String mobId, NamespacedKey mobKey) {
		PersistentDataContainer data = mock(PersistentDataContainer.class);
		when(data.get(mobKey, PersistentDataType.STRING)).thenReturn(mobId);
		return data;
	}
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

	@Test
	void airPopulationNeverQueriesGroundHeightInUnloadedChunks() {
		World world = mock(World.class);
		Player player = mock(Player.class);
		when(player.getWorld()).thenReturn(world);
		when(player.getLocation()).thenReturn(new Location(world, 100, 75, 100));
		MobPopulationRule rule = new MobPopulationRule("JELLYFISH", "AREA", 3, 100,
				48, 80, 200, 8, 24, MobSpawnPlacement.AIR, 65, 90);
		MobPopulationService service = new MobPopulationService(mock(TomBlock.class),
				mock(CustomMobRegistry.class), mock(CustomMobSpawner.class),
				mock(CustomMobContextRegistry.class), mock(RegionResolver.class),
				NamespacedKey.minecraft("mob_id"), NamespacedKey.minecraft("population_rule_id"),
				List.of(rule));
		assertNull(service.findCandidate(rule, player));
		verify(world, never()).getHighestBlockYAt(anyInt(), anyInt());
	}
}
