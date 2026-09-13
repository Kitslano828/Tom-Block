package org.tomdang.dialogueframework.presentation.hud.indicator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialogueHudIndicatorStyleTest {

	private final Key fontKey = Key.key("tomblock", "dialogue_speaker");
	private final TextColor color = TextColor.color(0xFFF1D0);

	@Test
	void retainsConfiguredValues() {
		DialogueHudIndicatorStyle style = new DialogueHudIndicatorStyle("»", fontKey, color, 12);

		assertEquals("»", style.text());
		assertEquals(fontKey, style.fontKey());
		assertEquals(color, style.color());
		assertEquals(12, style.rightPadding());
	}

	@Test
	void invalidValuesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudIndicatorStyle(null, fontKey, color, 0));
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudIndicatorStyle("   ", fontKey, color, 0));
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudIndicatorStyle("»", null, color, 0));
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudIndicatorStyle("»", fontKey, null, 0));
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudIndicatorStyle("»", fontKey, color, -1));
	}
}
