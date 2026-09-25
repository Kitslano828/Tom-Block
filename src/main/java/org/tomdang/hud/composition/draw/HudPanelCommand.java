package org.tomdang.hud.composition.draw;

public record HudPanelCommand(String styleId, int width, int height) implements HudDrawCommand {
	public HudPanelCommand {
		if (styleId == null || styleId.isBlank()) throw new IllegalArgumentException("Panel style id cannot be blank");
		if (width <= 0 || height <= 0) throw new IllegalArgumentException("Panel dimensions must be positive");
	}
}
