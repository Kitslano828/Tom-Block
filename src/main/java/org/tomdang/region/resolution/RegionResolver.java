package org.tomdang.region.resolution;

import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.override.RegionRuntimeOverrideProvider;
import org.tomdang.region.registry.RegionRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RegionResolver {
	private static final Comparator<RegionDefinition> RESOLUTION_ORDER =
			Comparator.comparingInt(RegionDefinition::priority).reversed()
					.thenComparing(RegionDefinition::id);

	private final RegionRegistry registry;
	private final RegionMembershipEvaluator membershipEvaluator;

	public RegionResolver(RegionRegistry registry) {
		this(registry, RegionRuntimeOverrideProvider.empty());
	}

	public RegionResolver(RegionRegistry registry, RegionRuntimeOverrideProvider runtimeOverrides) {
		if (registry == null) throw new IllegalArgumentException("registry cannot be null");
		if (runtimeOverrides == null) throw new IllegalArgumentException("runtimeOverrides cannot be null");
		this.registry = registry;
		this.membershipEvaluator = new RegionMembershipEvaluator(runtimeOverrides);
	}

	public List<RegionDefinition> directRegionsAt(BlockPosition position) {
		requirePosition(position);
		return registry.all().stream()
				.filter(region -> evaluateDirect(region, position).member())
				.sorted(RESOLUTION_ORDER)
				.toList();
	}

	public RegionMembershipEvaluation evaluateDirect(RegionDefinition region, BlockPosition position) {
		return membershipEvaluator.evaluate(region, position);
	}

	public List<RegionDefinition> regionsAt(BlockPosition position) {
		Map<String, RegionDefinition> matches = new LinkedHashMap<>();
		for (RegionDefinition direct : directRegionsAt(position)) {
			RegionDefinition current = direct;
			while (current != null) {
				matches.putIfAbsent(current.id(), current);
				current = current.parentId().map(registry::require).orElse(null);
			}
		}

		List<RegionDefinition> sorted = new ArrayList<>(matches.values());
		sorted.sort(RESOLUTION_ORDER);
		return List.copyOf(sorted);
	}

	public Optional<RegionDefinition> primaryRegionAt(BlockPosition position) {
		List<RegionDefinition> matches = regionsAt(position);
		return matches.isEmpty() ? Optional.empty() : Optional.of(matches.getFirst());
	}

	private void requirePosition(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
	}
}
