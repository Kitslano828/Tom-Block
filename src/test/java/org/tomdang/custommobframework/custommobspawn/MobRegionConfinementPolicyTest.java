package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import org.tomdang.custommobframework.MobType;
import org.tomdang.custommobframework.configuration.CustomMobDefinition;
import org.tomdang.custommobframework.configuration.MobPopulationRule;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobRegionConfinementPolicyTest {
	@Test
	void ambientMobStaysInAssignedRegionEvenIfMobAllowsAnotherRegion() {
		RegionRegistry registry = new RegionRegistry();
		registry.registerAll(List.of(region("VILLAGE", 0, 5), region("FOREST", 10, 15)));
		RegionResolver resolver = new RegionResolver(registry);
		CustomMobDefinition mob = new CustomMobDefinition("ZOMBIE", EntityType.ZOMBIE, "Zombie", 100, 20,
				MobType.COMMON_MOB, 0, false, List.of("VILLAGE", "FOREST"), null, List.of());
		MobSpawnRegionPolicy spawn = new MobSpawnRegionPolicy(resolver, registry, List.of(mob));
		MobPopulationRule population = new MobPopulationRule("ZOMBIE", "VILLAGE", 5, 100, 20, 30, 200, 5, 15);
		MobRegionConfinementPolicy policy = new MobRegionConfinementPolicy(spawn, resolver, List.of(population));

		assertTrue(policy.constrained("ZOMBIE", null));
		assertTrue(policy.contains("ZOMBIE", null, position(12)));
		assertTrue(policy.contains("ZOMBIE", population.id(), position(2)));
		assertFalse(policy.contains("ZOMBIE", population.id(), position(12)));
		assertFalse(policy.contains("ZOMBIE", population.id(), position(20)));
	}

	private RegionDefinition region(String id, int min, int max) {
		return new RegionDefinition(id, Optional.empty(), 0, Set.of(),
				new CuboidRegionShape(position(min), position(max)), RegionOverrides.empty());
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 0, 0);
	}
}
