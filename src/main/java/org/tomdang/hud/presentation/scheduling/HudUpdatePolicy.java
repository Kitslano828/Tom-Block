package org.tomdang.hud.presentation.scheduling;

public record HudUpdatePolicy(long minimumIntervalTicks) {
	public static final HudUpdatePolicy ON_CHANGE = new HudUpdatePolicy(0);
	public HudUpdatePolicy {
		if (minimumIntervalTicks < 0) throw new IllegalArgumentException("HUD update interval cannot be negative");
	}
}
