package org.tomdang.dialogueframework.presentation.hud.skin;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.tomdang.hud.glyph.HudGlyph;

import java.util.List;

public class DialogueHudSkin {

	@Getter
	private final String skinID;
	@Getter
	private final HudGlyph backgroundGlyph;
	@Getter
	private final int textLeftPadding;
	@Getter
	private final int textRightPadding;
	@Getter
	private final List<Key> lineFonts;
	@Getter
	private final Key speakerFontKey;
	@Getter
	private final int speakerLeftPadding;
	@Getter
	private final int speakerRightPadding;

	public DialogueHudSkin(String skinID, HudGlyph backgroundGlyph, int textLeftPadding, int textRightPadding, List<Key> lineFonts, Key speakerFontKey, int speakerLeftPadding, int speakerRightPadding) {
		if (skinID == null) throw new IllegalArgumentException("skinID cannot be null");
		if (skinID.isBlank()) throw new IllegalArgumentException("skinID cannot be blank");
		if (backgroundGlyph == null) throw new IllegalArgumentException("backgroundGlyph cannot be null");
		if (textLeftPadding < 0) throw new IllegalArgumentException("textLeftPadding cannot be lower than 0");
		if (textRightPadding < 0) throw new IllegalArgumentException("textRightPadding cannot be lower than 0");
		if ( (long) textLeftPadding + textRightPadding >= backgroundGlyph.getPixelWidth()) throw new IllegalArgumentException("combined text padding must be less than the background glyph width");
		if (lineFonts == null) throw new IllegalArgumentException("lineFonts cannot be null");
		if (lineFonts.isEmpty()) throw new IllegalArgumentException("lineFonts cannot be empty");
		if (lineFonts.stream().anyMatch(font -> font == null)) throw new IllegalArgumentException("lineFonts cannot contain null");
		if (speakerFontKey == null) throw new IllegalArgumentException("speakerFontKey cannot be null");
		if (speakerLeftPadding < 0) throw new IllegalArgumentException("speakerLeftPadding cannot be negative");
		if (speakerRightPadding < 0) throw new IllegalArgumentException("speakerRightPadding cannot be negative");
		if ( (long) speakerLeftPadding + speakerRightPadding >= backgroundGlyph.getPixelWidth()) throw new IllegalArgumentException("combined speaker padding must be less than the background glyph width");

		this.skinID = skinID;
		this.backgroundGlyph = backgroundGlyph;
		this.textLeftPadding = textLeftPadding;
		this.textRightPadding = textRightPadding;
		this.lineFonts = List.copyOf(lineFonts);
		this.speakerFontKey = speakerFontKey;
		this.speakerLeftPadding = speakerLeftPadding;
		this.speakerRightPadding = speakerRightPadding;
	}

	public int getMaximumLines() {
		return lineFonts.size();
	}

	public Key getLineFont(int lineIndex) {
		if (lineIndex < 0) throw new IllegalArgumentException("lineIndex cannot be negative");
		if (lineIndex >= lineFonts.size()) throw new IllegalArgumentException("Index equal to or above the number of fonts.");
		return lineFonts.get(lineIndex);
	}

}
