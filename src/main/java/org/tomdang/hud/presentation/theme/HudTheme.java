package org.tomdang.hud.presentation.theme;

import org.tomdang.hud.composition.draw.HudTextStyle;
import java.util.Map;

/** Immutable named visual values. Presenters never contain raw colors or spacing constants. */
public final class HudTheme {
	private final String id;
	private final Map<HudTextStyleToken, HudTextStyle> textStyles;
	private final Map<HudMetricToken, Integer> metrics;
	public HudTheme(String id, Map<HudTextStyleToken, HudTextStyle> textStyles,
			Map<HudMetricToken, Integer> metrics) {
		if (id == null || id.isBlank() || textStyles == null || metrics == null)
			throw new IllegalArgumentException("HUD theme is incomplete");
		if (textStyles.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)
				|| metrics.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0))
			throw new IllegalArgumentException("HUD theme contains invalid values");
		this.id = id;
		this.textStyles = Map.copyOf(textStyles);
		this.metrics = Map.copyOf(metrics);
	}
	public String id() { return id; }
	public HudTextStyle text(HudTextStyleToken token) {
		HudTextStyle style = textStyles.get(token);
		if (style == null) throw new IllegalArgumentException("Unknown HUD text style: " + token.value());
		return style;
	}
	public int metric(HudMetricToken token) {
		Integer value = metrics.get(token);
		if (value == null) throw new IllegalArgumentException("Unknown HUD metric: " + token.value());
		return value;
	}
}
