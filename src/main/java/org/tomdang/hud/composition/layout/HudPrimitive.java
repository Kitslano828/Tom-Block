package org.tomdang.hud.composition.layout;

import org.tomdang.hud.composition.draw.HudDrawCommand;

public record HudPrimitive(HudDrawCommand command) implements HudNode {
	public HudPrimitive {
		if (command == null) throw new IllegalArgumentException("Primitive command is required");
	}
}
