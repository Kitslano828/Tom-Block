package org.tomdang.hud.composition;

/** Client GUI dimensions expressed in GUI-scaled pixels, not physical display pixels. */
public record HudViewport(int width, int height, double guiScale, HudInsets safeArea) {
	public static final HudViewport DEFAULT = new HudViewport(320, 180, 1.0, HudInsets.NONE);
	public HudViewport {
		if (width <= 0 || height <= 0) throw new IllegalArgumentException("HUD viewport must be positive");
		if (!Double.isFinite(guiScale) || guiScale <= 0) throw new IllegalArgumentException("GUI scale must be positive");
		if (safeArea == null) throw new IllegalArgumentException("Safe area is required");
		if (safeArea.horizontal() >= width || safeArea.vertical() >= height)
			throw new IllegalArgumentException("Safe area must fit inside the viewport");
	}
}
