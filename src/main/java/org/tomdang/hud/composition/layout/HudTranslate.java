package org.tomdang.hud.composition.layout;

/** Moves a child without changing its measured layout footprint. */
public record HudTranslate(int x, int y, HudNode child) implements HudNode {
	public HudTranslate {
		if (child == null) throw new IllegalArgumentException("Translated HUD child cannot be null");
	}
}
