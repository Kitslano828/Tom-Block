package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import org.tomdang.custommobframework.MobType;
import org.tomdang.custommobframework.configuration.CustomMobDefinition;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSpawnRegionPolicyTest {
	@Test
	void allowsConfiguredRegionAndInheritedParentButRejectsOutside() {
		RegionRegistry registry = registry();
		MobSpawnRegionPolicy policy = new MobSpawnRegionPolicy(new RegionResolver(registry), registry,
				List.of(mob("JELLYFISH", List.of("ISLAND"))));

		assertTrue(policy.allows("JELLYFISH", position(5)));
		assertFalse(policy.allows("JELLYFISH", position(20)));
		assertFalse(policy.allows("JELLYFISH", new BlockPosition("other", 5, 0, 0)));
	}

	@Test
	void emptyListLeavesMobUnrestricted() {
		RegionRegistry registry = registry();
		MobSpawnRegionPolicy policy = new MobSpawnRegionPolicy(new RegionResolver(registry), registry,
				List.of(mob("ZOMBIE", List.of())));
		assertTrue(policy.allows("ZOMBIE", position(20)));
	}

	@Test
	void followsLiveBlockOverridesWithoutReloadingMobRules() {
		RegionRegistry registry = registry();
		AtomicReference<RegionOverrideState> state = new AtomicReference<>(RegionOverrideState.NONE);
		RegionResolver resolver = new RegionResolver(registry,
				(regionId, position) -> regionId.equals("ISLAND") && position.equals(position(20))
						? state.get() : RegionOverrideState.NONE);
		MobSpawnRegionPolicy policy = new MobSpawnRegionPolicy(resolver, registry,
				List.of(mob("JELLYFISH", List.of("ISLAND"))));

		assertFalse(policy.allows("JELLYFISH", position(20)));
		state.set(RegionOverrideState.INCLUSION);
		assertTrue(policy.allows("JELLYFISH", position(20)));
		state.set(RegionOverrideState.EXCLUSION);
		assertFalse(policy.allows("JELLYFISH", position(20)));
	}

	@Test
	void unknownConfiguredRegionFailsAtStartup() {
		RegionRegistry registry = registry();
		assertThrows(IllegalArgumentException.class, () -> new MobSpawnRegionPolicy(
				new RegionResolver(registry), registry, List.of(mob("ZOMBIE", List.of("TYPO")))));
	}

	private RegionRegistry registry() {
		RegionRegistry registry = new RegionRegistry();
		registry.registerAll(List.of(
				new RegionDefinition("ISLAND", Optional.empty(), 0, Set.of(),
						new CuboidRegionShape(position(0), position(3)), RegionOverrides.empty()),
				new RegionDefinition("BEACH", Optional.of("ISLAND"), 1, Set.of(),
						new CuboidRegionShape(position(4), position(10)), RegionOverrides.empty())));
		return registry;
	}

	private CustomMobDefinition mob(String id, List<String> allowedRegions) {
		return new CustomMobDefinition(id, EntityType.ZOMBIE, id, 100, 20,
				MobType.COMMON_MOB, 0, false, allowedRegions, null, List.of());
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 0, 0);
	}
}
