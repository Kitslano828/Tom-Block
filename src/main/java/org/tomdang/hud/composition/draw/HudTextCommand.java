package org.tomdang.hud.composition.draw;

public record HudTextCommand(String text, HudTextStyle style, int maxWidth, boolean wrap) implements HudDrawCommand {
	public HudTextCommand {
		if (text == null) throw new IllegalArgumentException("HUD text cannot be null");
		if (style == null) throw new IllegalArgumentException("HUD text style is required");
		if (maxWidth < 0) throw new IllegalArgumentException("Maximum text width cannot be negative");
	}
	public static HudTextCommand text(String text) { return new HudTextCommand(text, HudTextStyle.DEFAULT, 0, false); }
}
