package org.tomdang.hud.composition.draw;

import org.tomdang.hud.composition.HudElementId;
import org.tomdang.hud.composition.HudRect;
import org.tomdang.hud.composition.HudRegion;

public record PositionedHudCommand(HudElementId owner, HudRegion region, int zIndex,
		HudRect bounds, HudRect clip, HudDrawCommand command) {
	public PositionedHudCommand {
		if (owner == null || region == null || bounds == null || command == null)
			throw new IllegalArgumentException("Positioned HUD command is incomplete");
	}
}
