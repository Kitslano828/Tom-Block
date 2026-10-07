package org.tomdang.hud.composition.layout;

import org.tomdang.hud.composition.HudInsets;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.draw.HudTextStyle;

import java.util.ArrayList;
import java.util.List;

/** Deterministic pixel effects for bitmap HUD text, independent of Minecraft's text shadow pass. */
public final class HudTextEffects {
	private HudTextEffects() {}

	/** Adds an Isles-inspired one-pixel black lower-right edge. Weight belongs in the glyph atlas. */
	public static HudNode edged(HudTextCommand source) {
		HudTextStyle requested = source.style();
		HudTextStyle face = new HudTextStyle(requested.styleId(), requested.color(), false, false, requested.font());
		HudTextStyle edge = new HudTextStyle(requested.styleId() + "-edge", 0x000000, false, false, requested.font());
		List<HudNode> layers = new ArrayList<>();
		layers.add(offset(copy(source, edge), 1, 1));
		layers.add(new HudPrimitive(copy(source, face)));
		return new HudOverlay(HudAlignment.START, HudAlignment.START, layers);
	}

	/** Extra horizontal pixels occupied by {@link #edged(HudTextCommand)}. */
	public static int extraWidth(HudTextStyle style) {
		return 1;
	}

	private static HudTextCommand copy(HudTextCommand source, HudTextStyle style) {
		return new HudTextCommand(source.text(), style, source.maxWidth(), source.wrap());
	}

	private static HudNode offset(HudTextCommand command, int x, int y) {
		return new HudPadding(new HudInsets(y, 0, 0, x), new HudPrimitive(command));
	}
}
