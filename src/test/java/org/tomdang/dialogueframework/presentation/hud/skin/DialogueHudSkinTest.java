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

	private final HudGlyph backgroundGlyph = new HudGlyph(Key.key("tomblock", "dialogue"), "\uE001", 256);
	private final Key speakerFont = Key.key("tomblock", "dialogue_speaker");
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
				speakerRightPadding
		);
	}
}
