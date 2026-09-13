package org.tomdang.dialogueframework.presentation.hud.indicator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;

public record DialogueHudIndicatorStyle(String text, Key fontKey, TextColor color, int rightPadding) {

	public DialogueHudIndicatorStyle {
		if (text == null || text.isBlank()) throw new IllegalArgumentException("text cannot be null or blank");
		if (fontKey == null) throw new IllegalArgumentException("fontKey cannot be null");
		if (color == null) throw new IllegalArgumentException("color cannot be null");
		if (rightPadding < 0) throw new IllegalArgumentException("rightPadding cannot be negative");
	}
}
