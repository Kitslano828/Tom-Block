package org.tomdang.region.policy;

import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.cap.LocationStatCap;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.resolution.RegionResolver;

import java.util.Map;
import java.util.Optional;

/** Highest-priority resolved region with a policy wins; parents act as fallbacks. */
public final class RegionStatCapResolver {
	private final RegionResolver regions;
	private final Map<String, Map<PlayerStatType, Double>> caps;

	public RegionStatCapResolver(RegionResolver regions, Map<String, Map<PlayerStatType, Double>> caps) {
		if (regions == null || caps == null) throw new IllegalArgumentException("regions and caps are required");
		this.regions = regions;
		this.caps = Map.copyOf(caps);
	}

	public Optional<LocationStatCap> resolve(BlockPosition position, PlayerStatType statType) {
		for (var region : regions.regionsAt(position)) {
			Double value = caps.getOrDefault(region.id(), Map.of()).get(statType);
			if (value != null) return Optional.of(new LocationStatCap(region.id(), statType, value));
		}
		return Optional.empty();
	}
}
