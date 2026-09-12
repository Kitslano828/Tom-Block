package org.tomdang.dialogueframework.presentation.hud.skin;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;
import org.tomdang.hud.glyph.HudGlyph;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialogueHudSkinTest {

	private final HudGlyph backgroundGlyph = new HudGlyph(
			Key.key("tomblock", "dialogue"),
			"\uE001",
			256
	);
	private final Key lineOneFont = Key.key("tomblock", "dialogue_line_1");
	private final Key lineTwoFont = Key.key("tomblock", "dialogue_line_2");
	private final Key lineThreeFont = Key.key("tomblock", "dialogue_line_3");
	private final List<Key> lineFonts = List.of(lineOneFont, lineTwoFont, lineThreeFont);

	@Test
	void retainsSkinIDAndBackgroundGlyph() {
		DialogueHudSkin skin = new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 12, 14, lineFonts);

		assertEquals("BLACKSMITH_BOX", skin.getSkinID());
		assertSame(backgroundGlyph, skin.getBackgroundGlyph());
		assertEquals(12, skin.getTextLeftPadding());
		assertEquals(14, skin.getTextRightPadding());
		assertEquals(3, skin.getMaximumLines());
		assertEquals(lineOneFont, skin.getLineFont(0));
		assertEquals(lineTwoFont, skin.getLineFont(1));
		assertEquals(lineThreeFont, skin.getLineFont(2));
	}

	@Test
	void constructSkinWithZeroZero() {
		DialogueHudSkin skin = new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, 0, lineFonts);

		assertEquals(0, skin.getTextLeftPadding());
		assertEquals(0, skin.getTextRightPadding());
	}

	@Test
	void nullSkinIDIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin(null, backgroundGlyph, 0, 0, lineFonts)
		);
	}

	@Test
	void blankSkinIDIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("   ", backgroundGlyph, 0, 0, lineFonts)
		);
	}

	@Test
	void nullBackgroundGlyphIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", null, 0, 0, lineFonts)
		);
	}

	@Test
	void negativeLeftPaddingIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, -10, 0, lineFonts)
		);
	}

	@Test
	void negativeRightPaddingIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, -10, lineFonts)
		);
	}

	@Test
	void paddingConsumingEntireBackgroundWidthIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 128, 128, lineFonts)
		);
	}

	@Test
	void paddingExceedingBackgroundWidthIsRejected() {
		// background width is 256; left (200) + right (100) == 300 (> 256)
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 200, 100, lineFonts)
		);
	}

	@Test
	void nullLineFontsAreRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, 0, null)
		);
	}

	@Test
	void emptyLineFontsAreRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, 0, List.of())
		);
	}

	@Test
	void nullEntryInLineFontsIsRejected() {
		List<Key> fontsWithNull = new ArrayList<>();
		fontsWithNull.add(lineOneFont);
		fontsWithNull.add(null);

		assertThrows(
				IllegalArgumentException.class,
				() -> new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, 0, fontsWithNull)
		);
	}

	@Test
	void negativeLineIndexIsRejected() {
		DialogueHudSkin skin = new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, 0, lineFonts);

		assertThrows(IllegalArgumentException.class, () -> skin.getLineFont(-1));
	}

	@Test
	void indexEqualToMaximumLinesIsRejected() {
		DialogueHudSkin skin = new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, 0, lineFonts);

		assertThrows(IllegalArgumentException.class, () -> skin.getLineFont(3));
	}

	@Test
	void lineFontsAreDefensivelyCopied() {
		List<Key> mutableFonts = new ArrayList<>(lineFonts);
		DialogueHudSkin skin = new DialogueHudSkin("BLACKSMITH_BOX", backgroundGlyph, 0, 0, mutableFonts);

		mutableFonts.clear();

		assertEquals(3, skin.getMaximumLines());
		assertEquals(lineOneFont, skin.getLineFont(0));
		assertEquals(lineTwoFont, skin.getLineFont(1));
		assertEquals(lineThreeFont, skin.getLineFont(2));
	}
}
