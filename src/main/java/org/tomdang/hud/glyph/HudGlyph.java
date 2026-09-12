package org.tomdang.hud.glyph;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class HudGlyph {

	@Getter
	private final Key font;
	@Getter
	private final String characters;
	@Getter
	private final int pixelWidth;

	public HudGlyph(Key font, String characters, int pixelWidth) {
		if (font == null) throw new IllegalArgumentException("font cannot be null");
		if (characters == null || characters.isEmpty()) throw new IllegalArgumentException("characters cannot be null or empty");
		if (pixelWidth <= 0) throw new IllegalArgumentException("pixel width cannot be lesser than 1");

		this.font = font;
		this.characters = characters;
		this.pixelWidth = pixelWidth;
	}

	public TextComponent createComponent() {
		return Component.text(this.characters).font(this.font);
	}

}
