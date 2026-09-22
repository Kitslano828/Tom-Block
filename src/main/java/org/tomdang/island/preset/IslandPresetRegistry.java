package org.tomdang.island.preset;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class IslandPresetRegistry {
	private final Map<String, IslandPreset> presets = new LinkedHashMap<>();
	public void register(IslandPreset preset) {
		if (presets.putIfAbsent(preset.id(), preset) != null) throw new IllegalArgumentException("Duplicate island preset: " + preset.id());
	}
	public IslandPreset require(String id) {
		IslandPreset preset = presets.get(id);
		if (preset == null) throw new IllegalArgumentException("Unknown island preset: " + id);
		return preset;
	}
	public Collection<IslandPreset> all() { return java.util.List.copyOf(presets.values()); }
}
