package org.tomdang.hud.composition.layout;

public record HudClip(int width, int height, HudNode child) implements HudNode {
	public HudClip {
		if (width <= 0 || height <= 0 || child == null) throw new IllegalArgumentException("Clip is invalid");
	}
}
