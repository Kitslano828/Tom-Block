package org.tomdang.hud.composition;

import java.util.Set;

/** Feature-owned view model. Implementations contain presentation data, never transport calls. */
public interface HudElement {
	HudElementId id();
	HudRegion region();
	int priority();
	default boolean canCompact() { return false; }
	default boolean visible(HudRenderContext context) { return true; }
	default long expiresAtTick() { return Long.MAX_VALUE; }
	default Set<HudRegion> suppressesRegions() { return Set.of(); }
	HudContent render(HudRenderContext context);
}
