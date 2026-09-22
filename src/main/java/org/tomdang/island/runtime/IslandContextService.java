package org.tomdang.island.runtime;

import org.tomdang.island.PrivateIsland;
import org.tomdang.island.preset.IslandPreset;
import org.tomdang.island.preset.IslandPresetRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class IslandContextService {
	private final Map<String, IslandRuntime> byWorld = new HashMap<>();
	public IslandContextService(IslandPresetRegistry presets) {
		for (IslandPreset preset : presets.all()) if (preset.worldName() != null && !preset.worldName().isBlank())
			register(new IslandRuntime(preset.worldName(), preset, null, null));
	}
	public void register(PrivateIsland island, IslandPreset preset) {
		register(new IslandRuntime(island.worldName(), preset, island.islandId(), island.ownerId()));
	}
	public void register(IslandRuntime runtime) { byWorld.put(runtime.worldName(), runtime); }
	public void unregister(String worldName) { byWorld.remove(worldName); }
	public Optional<IslandRuntime> runtime(String worldName) { return Optional.ofNullable(byWorld.get(worldName)); }
	public Optional<IslandContext> resolve(String worldName, UUID playerId) {
		return runtime(worldName).map(runtime -> new IslandContext(runtime, playerId,
				playerId != null && playerId.equals(runtime.ownerId()) ? IslandRole.OWNER : IslandRole.VISITOR));
	}
}
