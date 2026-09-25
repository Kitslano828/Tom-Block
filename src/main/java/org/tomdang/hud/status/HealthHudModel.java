package org.tomdang.hud.status;

import org.tomdang.hud.presentation.HudViewModel;

public record HealthHudModel(double current, double maximum) implements HudViewModel {
	public HealthHudModel { validate(current, maximum); current = Math.min(current, maximum); }
	private static void validate(double current, double maximum) {
		if (!Double.isFinite(current) || !Double.isFinite(maximum) || current < 0 || maximum <= 0)
			throw new IllegalArgumentException("Invalid health HUD values");
	}
}
