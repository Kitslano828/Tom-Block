package org.tomdang.region.override;

import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class RegionOverrideService implements RegionRuntimeOverrideProvider {
	private final RegionRegistry regionRegistry;
	private final RegionOverrideRepository repository;
	private final Map<String, MutableOverrides> overrides = new LinkedHashMap<>();
	private boolean dirty;

	public RegionOverrideService(RegionRegistry regionRegistry, RegionOverrideRepository repository) {
		if (regionRegistry == null) throw new IllegalArgumentException("regionRegistry cannot be null");
		if (repository == null) throw new IllegalArgumentException("repository cannot be null");
		this.regionRegistry = regionRegistry;
		this.repository = repository;
	}

	public synchronized void load() throws IOException {
		Map<String, RegionOverrides> loaded = repository.load();
		Map<String, MutableOverrides> replacement = new LinkedHashMap<>();
		for (Map.Entry<String, RegionOverrides> entry : loaded.entrySet()) {
			regionRegistry.require(entry.getKey());
			RegionOverrides value = entry.getValue();
			replacement.put(entry.getKey(), new MutableOverrides(value.inclusions(), value.exclusions()));
		}
		overrides.clear();
		overrides.putAll(replacement);
		dirty = false;
	}

	public synchronized RegionOverrideState setState(String regionId, BlockPosition position,
			RegionOverrideState state) {
		requireInputs(regionId, position, state);
		RegionOverrideState previous = state(regionId, position);
		if (previous == state) return previous;

		MutableOverrides mutable = overrides.computeIfAbsent(regionId, ignored -> new MutableOverrides());
		mutable.inclusions.remove(position);
		mutable.exclusions.remove(position);
		if (state == RegionOverrideState.INCLUSION) mutable.inclusions.add(position);
		if (state == RegionOverrideState.EXCLUSION) mutable.exclusions.add(position);
		if (mutable.isEmpty()) overrides.remove(regionId);
		dirty = true;
		return previous;
	}

	@Override
	public synchronized RegionOverrideState state(String regionId, BlockPosition position) {
		if (regionId == null || regionId.isBlank()) throw new IllegalArgumentException("regionId cannot be null or blank");
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		regionRegistry.require(regionId);
		MutableOverrides mutable = overrides.get(regionId);
		if (mutable == null) return RegionOverrideState.NONE;
		if (mutable.exclusions.contains(position)) return RegionOverrideState.EXCLUSION;
		if (mutable.inclusions.contains(position)) return RegionOverrideState.INCLUSION;
		return RegionOverrideState.NONE;
	}

	public synchronized void save() throws IOException {
		repository.save(snapshot());
		dirty = false;
	}

	public synchronized boolean isDirty() {
		return dirty;
	}

	public synchronized Map<String, RegionOverrides> snapshot() {
		Map<String, RegionOverrides> snapshot = new LinkedHashMap<>();
		overrides.forEach((id, value) -> snapshot.put(id,
				new RegionOverrides(value.inclusions, value.exclusions)));
		return Map.copyOf(snapshot);
	}

	private void requireInputs(String regionId, BlockPosition position, RegionOverrideState state) {
		if (state == null) throw new IllegalArgumentException("state cannot be null");
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		regionRegistry.require(regionId);
	}

	private static final class MutableOverrides {
		private final Set<BlockPosition> inclusions;
		private final Set<BlockPosition> exclusions;

		private MutableOverrides() {
			this(Set.of(), Set.of());
		}

		private MutableOverrides(Set<BlockPosition> inclusions, Set<BlockPosition> exclusions) {
			this.inclusions = new LinkedHashSet<>(inclusions);
			this.exclusions = new LinkedHashSet<>(exclusions);
		}

		private boolean isEmpty() {
			return inclusions.isEmpty() && exclusions.isEmpty();
		}
	}
}
