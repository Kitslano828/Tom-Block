package org.tomdang.hud.composition.draw;

import net.kyori.adventure.text.Component;

/** A precomposed visual retained as one engine-owned draw command. */
public record HudComponentCommand(Component component, int width, int height) implements HudDrawCommand {
	public HudComponentCommand {
		if (component == null) throw new IllegalArgumentException("HUD component is required");
		if (width <= 0 || height <= 0) throw new IllegalArgumentException("HUD component dimensions must be positive");
	}
}
