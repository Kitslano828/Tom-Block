package org.tomdang.dialogueframework.presentation.hud.skin;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorStyle;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialogueHudSkinTest {

	private final HudGlyph backgroundGlyph = new HudGlyph(Key.key("tomblock", "dialogue"), "\uE001", 256);
	private final Key speakerFont = Key.key("tomblock", "dialogue_speaker");
	private final TextColor bodyTextColor = TextColor.color(0xD8D8D8);
	private final TextColor speakerNameColor = TextColor.color(0xFFF1D0);
	private final DialogueHudIndicatorStyle indicatorStyle = new DialogueHudIndicatorStyle("»", speakerFont, speakerNameColor, 10);
	private final DialogueHudIndicatorStyle choiceIndicatorStyle = new DialogueHudIndicatorStyle("?", speakerFont, speakerNameColor, 10);
	private final List<Key> lineFonts = List.of(
			Key.key("tomblock", "dialogue_line_1"),
			Key.key("tomblock", "dialogue_line_2"),
			Key.key("tomblock", "dialogue_line_3")
	);

	@Test
	void retainsAllSkinLayoutValues() {
		DialogueHudSkin skin = createSkin("BLACKSMITH_BOX", backgroundGlyph, 12, 14, lineFonts, speakerFont, 8, 10);

		assertEquals("BLACKSMITH_BOX", skin.getSkinID());
		assertSame(backgroundGlyph, skin.getBackgroundGlyph());
		assertEquals(12, skin.getTextLeftPadding());
		assertEquals(14, skin.getTextRightPadding());
		assertEquals(3, skin.getMaximumLines());
		assertEquals(lineFonts.get(0), skin.getLineFont(0));
		assertEquals(lineFonts.get(2), skin.getLineFont(2));
		assertEquals(speakerFont, skin.getSpeakerFontKey());
		assertEquals(8, skin.getSpeakerLeftPadding());
		assertEquals(10, skin.getSpeakerRightPadding());
		assertEquals(bodyTextColor, skin.getBodyTextColor());
		assertEquals(speakerNameColor, skin.getSpeakerNameColor());
		assertSame(indicatorStyle, skin.getIndicatorStyle(DialogueHudIndicatorState.CONTINUE));
		assertSame(choiceIndicatorStyle, skin.getIndicatorStyle(DialogueHudIndicatorState.CHOICE_REQUIRED));
		assertEquals(null, skin.getIndicatorStyle(DialogueHudIndicatorState.HIDDEN));
	}

	@Test
	void invalidIdentityAndBackgroundAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> createSkin(null, backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> createSkin("   ", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", null, 0, 0, lineFonts, speakerFont, 0, 0));
	}

	@Test
	void invalidTextPaddingIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, -1, 0, lineFonts, speakerFont, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, -1, lineFonts, speakerFont, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 128, 128, lineFonts, speakerFont, 0, 0));
	}

	@Test
	void invalidLineFontsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, 0, null, speakerFont, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, 0, List.of(), speakerFont, 0, 0));

		List<Key> fontsWithNull = new ArrayList<>();
		fontsWithNull.add(lineFonts.getFirst());
		fontsWithNull.add(null);
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, 0, fontsWithNull, speakerFont, 0, 0));
	}

	@Test
	void invalidSpeakerFontAndPaddingAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, 0, lineFonts, null, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, -1, 0));
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, -1));
		assertThrows(IllegalArgumentException.class, () -> createSkin("SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 128, 128));
	}

	@Test
	void nullTextColorsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudSkin(
				"SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0, null, speakerNameColor, indicatorStyles()
		));
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudSkin(
				"SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0, bodyTextColor, null, indicatorStyles()
		));
	}

	@Test
	void invalidContinueIndicatorStyleIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudSkin(
				"SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0,
				bodyTextColor, speakerNameColor, null
		));
		DialogueHudIndicatorStyle excessivePadding = new DialogueHudIndicatorStyle("»", speakerFont, speakerNameColor, 256);
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudSkin(
				"SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0,
				bodyTextColor, speakerNameColor, Map.of(
						DialogueHudIndicatorState.CONTINUE, excessivePadding,
						DialogueHudIndicatorState.CHOICE_REQUIRED, choiceIndicatorStyle
				)
		));
	}

	@Test
	void missingAndHiddenIndicatorMappingsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudSkin(
				"SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0,
				bodyTextColor, speakerNameColor, Map.of(DialogueHudIndicatorState.CONTINUE, indicatorStyle)
		));
		assertThrows(IllegalArgumentException.class, () -> new DialogueHudSkin(
				"SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0,
				bodyTextColor, speakerNameColor, Map.of(
						DialogueHudIndicatorState.HIDDEN, indicatorStyle,
						DialogueHudIndicatorState.CONTINUE, indicatorStyle,
						DialogueHudIndicatorState.CHOICE_REQUIRED, choiceIndicatorStyle
				)
		));
	}

	@Test
	void invalidLineIndexesAreRejected() {
		DialogueHudSkin skin = createSkin("SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0);

		assertThrows(IllegalArgumentException.class, () -> skin.getLineFont(-1));
		assertThrows(IllegalArgumentException.class, () -> skin.getLineFont(3));
	}

	@Test
	void lineFontsAreDefensivelyCopied() {
		List<Key> mutableFonts = new ArrayList<>(lineFonts);
		DialogueHudSkin skin = createSkin("SKIN", backgroundGlyph, 0, 0, mutableFonts, speakerFont, 0, 0);

		mutableFonts.clear();

		assertEquals(3, skin.getMaximumLines());
		assertEquals(lineFonts.getFirst(), skin.getLineFont(0));
	}

	@Test
	void indicatorStylesAreDefensivelyCopied() {
		Map<DialogueHudIndicatorState, DialogueHudIndicatorStyle> mutableStyles = new HashMap<>(indicatorStyles());
		DialogueHudSkin skin = new DialogueHudSkin(
				"SKIN", backgroundGlyph, 0, 0, lineFonts, speakerFont, 0, 0,
				bodyTextColor, speakerNameColor, mutableStyles
		);

		mutableStyles.clear();

		assertSame(indicatorStyle, skin.getIndicatorStyle(DialogueHudIndicatorState.CONTINUE));
		assertSame(choiceIndicatorStyle, skin.getIndicatorStyle(DialogueHudIndicatorState.CHOICE_REQUIRED));
	}

	private DialogueHudSkin createSkin(
			String skinID,
			HudGlyph glyph,
			int textLeftPadding,
			int textRightPadding,
			List<Key> fonts,
			Key speakerFontKey,
			int speakerLeftPadding,
			int speakerRightPadding
	) {
		return new DialogueHudSkin(
				skinID,
				glyph,
				textLeftPadding,
				textRightPadding,
				fonts,
				speakerFontKey,
				speakerLeftPadding,
				speakerRightPadding,
				bodyTextColor,
				speakerNameColor,
				indicatorStyles()
		);
	}

	private Map<DialogueHudIndicatorState, DialogueHudIndicatorStyle> indicatorStyles() {
		return Map.of(
				DialogueHudIndicatorState.CONTINUE, indicatorStyle,
				DialogueHudIndicatorState.CHOICE_REQUIRED, choiceIndicatorStyle
		);
	}
}
