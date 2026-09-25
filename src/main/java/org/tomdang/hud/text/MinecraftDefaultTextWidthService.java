package org.tomdang.hud.text;

public class MinecraftDefaultTextWidthService implements HudTextWidthService {
	private static final int DEFAULT_ADVANCE = 6;

	@Override
	public int measure(String text) {
		if (text == null) {
			throw new IllegalArgumentException("Text cannot be null");
		}
		if (text.isEmpty()) {
			return 0;
		}

		int totalAdvance = 0;
		for (int i = 0; i < text.length(); i++) {
			totalAdvance += getCharacterWidth(text.charAt(i));
		}

		return totalAdvance;
	}

	/**
	 * Resolves the complete horizontal advance (character width + trailing gap)
	 * for a single character in Minecraft's default font.
	 */
	private int getCharacterWidth(char c) {
		return switch (c) {
			// 2 pixels: thin punctuation
			case '!', '.', ',', ':', ';', '|', '\'', 'i' -> 2;

			// These glyphs occupy two bitmap columns plus Minecraft's trailing gap.
			case '`', 'l' -> 3;

			// 4 pixels: medium-thin characters and spaces
			case ' ', 'I', '[', ']', 't', '"', '*', '(', ')', '{', '}'-> 4;

			// 5 pixels: medium characters & brackets
			case 'f', 'k', '<', '>'  -> 5;

			case 'W', 'M' -> 6;

			// 7 pixels: extra wide characters
			case '@', '~'  -> 7;

			// 6 pixels: Default width for standard digits (0-9), uppercase/lowercase letters,
			// question mark (?), dash (-), plus (+), equal (=), and unknown characters.
			default -> DEFAULT_ADVANCE;
		};
	}
}
