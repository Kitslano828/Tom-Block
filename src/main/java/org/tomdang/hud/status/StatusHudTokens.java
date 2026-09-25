package org.tomdang.hud.status;

import org.tomdang.hud.presentation.asset.HudAssetId;
import org.tomdang.hud.presentation.theme.*;

public final class StatusHudTokens {
	public static final HudTextStyleToken HEALTH_TEXT = HudTextStyleToken.of("status-health");
	public static final HudTextStyleToken ENERGY_TEXT = HudTextStyleToken.of("status-energy");
	public static final HudMetricToken GAP = HudMetricToken.of("status-gap");
	public static final HudMetricToken BAR_WIDTH = HudMetricToken.of("status-bar-width");
	public static final HudMetricToken VALUE_TRACK_WIDTH = HudMetricToken.of("status-value-track-width");
	public static final HudAssetId HEART = HudAssetId.of("tomblock", "status-heart");
	public static final HudAssetId ENERGY = HudAssetId.of("tomblock", "status-energy");
	public static final HudAssetId HEALTH_BAR = HudAssetId.of("tomblock", "status-health-bar");
	public static final HudAssetId ENERGY_BAR = HudAssetId.of("tomblock", "status-energy-bar");
	private StatusHudTokens() {}
}
