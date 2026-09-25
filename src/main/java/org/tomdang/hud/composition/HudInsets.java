package org.tomdang.hud.composition;

public record HudInsets(int top, int right, int bottom, int left) {
	public static final HudInsets NONE = new HudInsets(0, 0, 0, 0);
	public HudInsets {
		if (top < 0 || right < 0 || bottom < 0 || left < 0)
			throw new IllegalArgumentException("HUD insets cannot be negative");
	}
	public int horizontal() { return left + right; }
	public int vertical() { return top + bottom; }
}
