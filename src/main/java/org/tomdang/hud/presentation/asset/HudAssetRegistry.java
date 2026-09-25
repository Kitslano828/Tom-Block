package org.tomdang.hud.presentation.asset;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HudAssetRegistry {
	private final Map<HudAssetId, HudAsset> assets = new LinkedHashMap<>();
	private boolean sealed;
	public void register(HudAsset asset) {
		if (sealed) throw new IllegalStateException("HUD asset registry is sealed");
		if (asset == null) throw new IllegalArgumentException("HUD asset cannot be null");
		if (assets.putIfAbsent(asset.id(), asset) != null) throw new IllegalArgumentException("Duplicate HUD asset: " + asset.id());
	}
	public HudAsset require(HudAssetId id) {
		HudAsset asset = assets.get(id);
		if (asset == null) throw new IllegalArgumentException("Unknown HUD asset: " + id);
		return asset;
	}
	public void seal() { sealed = true; }
	public boolean isSealed() { return sealed; }
}
