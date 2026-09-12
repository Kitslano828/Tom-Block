package org.tomdang.hud.glyph;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.TextComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudGlyphTest {

	private final Key testFont = Key.key("my_pack", "hud_font");
	private final String testCharacters = "\uE001";
	private final int testPixelWidth = 8;

	@Test
	void testStateRetention() {
		HudGlyph glyph = new HudGlyph(testFont, testCharacters, testPixelWidth);

		assertEquals(testFont, glyph.getFont());
		assertEquals(testCharacters, glyph.getCharacters());
		assertEquals(testPixelWidth, glyph.getPixelWidth());
	}

	@Test
	void testCreateComponent() {
		HudGlyph glyph = new HudGlyph(testFont, testCharacters, testPixelWidth);
		TextComponent component = glyph.createComponent();

		assertEquals(testCharacters, component.content());
		assertEquals(testFont, component.font());
	}

	@Test
	void testInvalidConstructorArguments() {
		// Null font
		assertThrows(IllegalArgumentException.class, () ->
				new HudGlyph(null, testCharacters, testPixelWidth)
		);

		// Null characters
		assertThrows(IllegalArgumentException.class, () ->
				new HudGlyph(testFont, null, testPixelWidth)
		);

		// Empty characters
		assertThrows(IllegalArgumentException.class, () ->
				new HudGlyph(testFont, "", testPixelWidth)
		);

		// Zero pixel width
		assertThrows(IllegalArgumentException.class, () ->
				new HudGlyph(testFont, testCharacters, 0)
		);

		// Negative pixel width
		assertThrows(IllegalArgumentException.class, () ->
				new HudGlyph(testFont, testCharacters, -5)
		);
	}
}