package org.tomdang.custommobframework.custommobspawn;

import org.tomdang.custommobframework.configuration.MobPopulationRule;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.resolution.RegionResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Population mobs stay in their assigned region; other restricted mobs stay in any allowed region. */
public final class MobRegionConfinementPolicy {
	private final MobSpawnRegionPolicy spawnPolicy;
	private final RegionResolver regions;
	private final Map<String, String> populationRegions;

	public MobRegionConfinementPolicy(MobSpawnRegionPolicy spawnPolicy, RegionResolver regions,
	                                  List<MobPopulationRule> rules) {
		if (spawnPolicy == null || regions == null || rules == null) {
			throw new IllegalArgumentException("spawnPolicy, regions, and rules are required");
		}
		this.spawnPolicy = spawnPolicy;
		this.regions = regions;
		Map<String, String> mapped = new HashMap<>();
		for (MobPopulationRule rule : rules) {
			if (rule == null) throw new IllegalArgumentException("rules cannot contain null");
			if (mapped.putIfAbsent(rule.id(), rule.regionId()) != null) {
				throw new IllegalArgumentException("Duplicate population rule " + rule.id());
			}
		}
		populationRegions = Map.copyOf(mapped);
	}

	public boolean constrained(String mobId, String populationId) {
		return populationId != null && populationRegions.containsKey(populationId)
				|| spawnPolicy.isRestricted(mobId);
	}

	public boolean contains(String mobId, String populationId, BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		String populationRegion = populationId == null ? null : populationRegions.get(populationId);
		if (populationRegion != null) {
			return regions.regionsAt(position).stream().anyMatch(region -> populationRegion.equals(region.id()));
		}
		return spawnPolicy.allows(mobId, position);
	}
}
