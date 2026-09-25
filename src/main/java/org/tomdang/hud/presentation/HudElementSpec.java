package org.tomdang.hud.presentation;

import org.tomdang.hud.composition.HudElementId;
import org.tomdang.hud.composition.HudRegion;
import java.util.Set;

/** Stable production placement and lifecycle metadata, separate from feature data. */
public record HudElementSpec(HudElementId id, HudRegion region, int priority, boolean compact,
		long expiresAtTick, Set<HudRegion> suppressesRegions) {
	public HudElementSpec {
		if (id == null || region == null || suppressesRegions == null)
			throw new IllegalArgumentException("HUD element specification is incomplete");
		suppressesRegions = Set.copyOf(suppressesRegions);
	}
	public static HudElementSpec persistent(HudElementId id, HudRegion region, int priority) {
		return new HudElementSpec(id, region, priority, false, Long.MAX_VALUE, Set.of());
	}
}
