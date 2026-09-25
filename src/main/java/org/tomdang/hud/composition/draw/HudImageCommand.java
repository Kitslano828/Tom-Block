package org.tomdang.hud.composition.draw;

public record HudImageCommand(String assetId, int width, int height) implements HudDrawCommand {
	public HudImageCommand {
		if (assetId == null || assetId.isBlank()) throw new IllegalArgumentException("Image asset id cannot be blank");
		if (width <= 0 || height <= 0) throw new IllegalArgumentException("Image dimensions must be positive");
	}
}
