package org.tomdang.hud.presentation.theme;

public record HudTextStyleToken(String value) {
	public HudTextStyleToken {
		if (value == null || !value.matches("[a-z0-9._/-]+")) throw new IllegalArgumentException("Invalid HUD text token");
	}
	public static HudTextStyleToken of(String value) { return new HudTextStyleToken(value); }
}
