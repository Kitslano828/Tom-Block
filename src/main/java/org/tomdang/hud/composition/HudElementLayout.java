package org.tomdang.hud.composition;

/** Optional placement override for one stable HUD element. */
public record HudElementLayout(boolean enabled, HudAnchor anchor, int offsetX, int offsetY, int maxWidth) {
	public HudElementLayout {
		if (anchor == null) throw new IllegalArgumentException("HUD element anchor cannot be null");
		if (maxWidth <= 0) throw new IllegalArgumentException("HUD element max width must be positive");
	}
}
