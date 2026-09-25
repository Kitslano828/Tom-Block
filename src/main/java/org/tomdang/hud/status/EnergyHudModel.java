package org.tomdang.hud.status;

import org.tomdang.hud.presentation.HudViewModel;

public record EnergyHudModel(double current, double maximum) implements HudViewModel {
	public EnergyHudModel { validate(current, maximum); current = Math.min(current, maximum); }
	private static void validate(double current, double maximum) {
		if (!Double.isFinite(current) || !Double.isFinite(maximum) || current < 0 || maximum <= 0)
			throw new IllegalArgumentException("Invalid energy HUD values");
	}
}
