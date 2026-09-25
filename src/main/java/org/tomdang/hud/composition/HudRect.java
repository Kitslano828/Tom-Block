package org.tomdang.hud.composition;

public record HudRect(int x, int y, int width, int height) {
	public HudRect {
		if (width < 0 || height < 0) throw new IllegalArgumentException("HUD rectangle size cannot be negative");
	}
	public int right() { return x + width; }
	public int bottom() { return y + height; }
	public boolean intersects(HudRect other) {
		return x < other.right() && right() > other.x && y < other.bottom() && bottom() > other.y;
	}
}
