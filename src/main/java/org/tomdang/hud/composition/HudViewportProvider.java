package org.tomdang.hud.composition;

import java.util.UUID;

@FunctionalInterface
public interface HudViewportProvider {
	HudViewport viewport(UUID playerId);
}
