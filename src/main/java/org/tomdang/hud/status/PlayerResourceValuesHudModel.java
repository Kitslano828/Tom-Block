package org.tomdang.hud.status;

import org.tomdang.hud.presentation.HudViewModel;

/** Numeric labels displayed above Minecraft's native health and food bars. */
public record PlayerResourceValuesHudModel(double health, double energy) implements HudViewModel {
	public PlayerResourceValuesHudModel {
		if (!Double.isFinite(health) || !Double.isFinite(energy) || health < 0 || energy < 0)
			throw new IllegalArgumentException("Invalid player resource HUD values");
	}
}
