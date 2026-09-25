package org.tomdang.hud.composition;

public record HudElementSnapshot(
		HudElementId id,
		HudRegion region,
		int priority,
		HudPresentationMode mode,
		HudContent content,
		HudRect bounds
) {
	public HudElementSnapshot {
		if (id == null || region == null || mode == null || content == null || bounds == null)
			throw new IllegalArgumentException("HUD element snapshot is incomplete");
	}
}
