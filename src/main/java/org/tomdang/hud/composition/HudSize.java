package org.tomdang.hud.composition;

public record HudSize(int width, int height) {
	public static final HudSize ZERO = new HudSize(0, 0);
	public HudSize {
		if (width < 0 || height < 0) throw new IllegalArgumentException("HUD size cannot be negative");
	}
}
