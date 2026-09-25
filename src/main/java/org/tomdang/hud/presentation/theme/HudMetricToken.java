package org.tomdang.hud.presentation.theme;

public record HudMetricToken(String value) {
	public HudMetricToken {
		if (value == null || !value.matches("[a-z0-9._/-]+")) throw new IllegalArgumentException("Invalid HUD metric token");
	}
	public static HudMetricToken of(String value) { return new HudMetricToken(value); }
}
