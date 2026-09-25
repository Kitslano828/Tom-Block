package org.tomdang.hud.composition;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.tomdang.hud.composition.draw.PositionedHudCommand;

public record HudFrame(UUID playerId, long revision, HudViewport viewport,
		Map<HudRegion, List<HudElementSnapshot>> regions, List<PositionedHudCommand> commands) {
	public HudFrame {
		if (playerId == null || viewport == null || regions == null || commands == null)
			throw new IllegalArgumentException("HUD frame is incomplete");
		EnumMap<HudRegion, List<HudElementSnapshot>> copy = new EnumMap<>(HudRegion.class);
		regions.forEach((region, elements) -> copy.put(region, List.copyOf(elements)));
		regions = Map.copyOf(copy);
		commands = List.copyOf(commands);
	}

	public List<HudElementSnapshot> region(HudRegion region) { return regions.getOrDefault(region, List.of()); }
}
