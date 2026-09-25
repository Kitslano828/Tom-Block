package org.tomdang.hud.presentation.asset;

public record HudAsset(HudAssetId id, Kind kind, int width, int height) {
	public enum Kind { IMAGE, PANEL, BAR }
	public HudAsset {
		if (id == null || kind == null || width <= 0 || height <= 0)
			throw new IllegalArgumentException("Invalid HUD asset");
	}
}
