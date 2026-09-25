package org.tomdang.hud.composition.draw;

public record HudProgressBarCommand(String styleId, double current, double maximum, int width, int height)
		implements HudDrawCommand {
	public HudProgressBarCommand {
		if (styleId == null || styleId.isBlank()) throw new IllegalArgumentException("Bar style id cannot be blank");
		if (!Double.isFinite(current) || !Double.isFinite(maximum) || maximum <= 0 || current < 0)
			throw new IllegalArgumentException("Progress values are invalid");
		if (width <= 0 || height <= 0) throw new IllegalArgumentException("Bar dimensions must be positive");
		current = Math.min(current, maximum);
	}
	public double progress() { return current / maximum; }
}
