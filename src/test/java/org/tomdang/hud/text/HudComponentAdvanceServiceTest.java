package org.tomdang.hud.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.presentation.hud.indicator.*;
import org.tomdang.dialogueframework.presentation.hud.layout.DialogueHudLayoutComposer;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.hud.spacing.HudSpacingService;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HudComponentAdvanceServiceTest {
	@Test void completedDialogueIndicatorDoesNotChangeTheComponentsCursorAdvance() {
		Key indicatorFont = Key.key("tomblock", "dialogue_indicator");
		DialogueHudSkin skin = new DialogueHudSkin("test",
				new HudGlyph(Key.key("tomblock", "dialogue"), "\uE001", 256), 12, 12,
				List.of(Key.key("tomblock", "dialogue_line_1"), Key.key("tomblock", "dialogue_line_2"),
						Key.key("tomblock", "dialogue_line_3")), Key.key("tomblock", "dialogue_speaker"),
				12, 12, TextColor.color(0xD8D8D8), TextColor.color(0xFFF1D0), Map.of(
				DialogueHudIndicatorState.CONTINUE,
				new DialogueHudIndicatorStyle("»", indicatorFont, TextColor.color(0xFFF1D0), 12),
				DialogueHudIndicatorState.CHOICE_REQUIRED,
				new DialogueHudIndicatorStyle("?", indicatorFont, TextColor.color(0xFFF1D0), 12)));
		DialogueHudLayoutComposer composer = new DialogueHudLayoutComposer(
				new HudSpacingService(), new TomBlockBitmapTextWidthService());
		HudComponentAdvanceService advances = TomBlockHudFontAdvances.create();

		int typing = advances.measure(composer.compose(skin, List.of("Easy there."), "Will",
				DialogueHudIndicatorState.HIDDEN));
		int complete = advances.measure(composer.compose(skin, List.of("Easy there."), "Will",
				DialogueHudIndicatorState.CONTINUE));

		assertEquals(257, typing);
		assertEquals(typing, complete);
	}

	@Test void indicatorAdvanceComesFromTheShippedBitmapRatherThanAWidthGuess() {
		assertEquals(7, new TomBlockBitmapTextWidthService().measure("»"));
	}

	@Test void rejectsUnregisteredFontsInsteadOfAllowingSilentDrift() {
		assertThrows(IllegalArgumentException.class, () -> TomBlockHudFontAdvances.create().measure(
				net.kyori.adventure.text.Component.text("unsafe").font(Key.key("other", "unknown"))));
	}
}
