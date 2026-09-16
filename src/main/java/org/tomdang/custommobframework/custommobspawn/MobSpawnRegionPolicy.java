package org.tomdang.custommobframework.custommobspawn;

import org.tomdang.custommobframework.configuration.CustomMobDefinition;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Restricts new mob spawns to resolved region membership; an empty list is unrestricted. */
public final class MobSpawnRegionPolicy {
	private final RegionResolver regions;
	private final Map<String, Set<String>> allowedByMob;

	public MobSpawnRegionPolicy(RegionResolver regions, RegionRegistry registry,
	                            List<CustomMobDefinition> definitions) {
		if (regions == null || registry == null || definitions == null) {
			throw new IllegalArgumentException("regions, registry, and definitions are required");
		}
		this.regions = regions;
		Map<String, Set<String>> rules = new HashMap<>();
		for (CustomMobDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definitions cannot contain null");
			for (String regionId : definition.allowedSpawnRegions()) {
				if (registry.find(regionId).isEmpty()) {
					throw new IllegalArgumentException("Mob " + definition.id()
							+ " references unknown spawn region " + regionId);
				}
			}
			rules.put(definition.id(), Set.copyOf(definition.allowedSpawnRegions()));
		}
		allowedByMob = Map.copyOf(rules);
	}

	public boolean allows(String mobId, BlockPosition position) {
		if (mobId == null || mobId.isBlank() || position == null) {
			throw new IllegalArgumentException("mobId and position are required");
		}
		Set<String> allowed = allowedByMob.get(mobId);
		if (allowed == null) throw new IllegalArgumentException("Unknown mob spawn rule: " + mobId);
		if (allowed.isEmpty()) return true;
		return regions.regionsAt(position).stream().anyMatch(region -> allowed.contains(region.id()));
	}

	public boolean isRestricted(String mobId) {
		Set<String> allowed = allowedByMob.get(mobId);
		return allowed != null && !allowed.isEmpty();
	}

}
