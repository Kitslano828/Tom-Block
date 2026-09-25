package org.tomdang.hud.composition;

import java.util.UUID;

public record HudRenderContext(UUID playerId, long tick, HudPresentationMode mode, HudViewport viewport, int availableWidth) {
	public HudRenderContext {
		if (playerId == null || mode == null || viewport == null)
			throw new IllegalArgumentException("HUD render context is incomplete");
		if (tick < 0) throw new IllegalArgumentException("HUD render tick cannot be negative");
		if (availableWidth <= 0) throw new IllegalArgumentException("HUD available width must be positive");
	}
}
