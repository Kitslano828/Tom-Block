package org.tomdang.dialogueframework.presentation.hud.skin;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorStyle;
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
	@Getter
	private final TextColor bodyTextColor;
	@Getter
	private final TextColor speakerNameColor;
	@Getter
	private final DialogueHudIndicatorStyle continueIndicatorStyle;

	public DialogueHudSkin(String skinID, HudGlyph backgroundGlyph, int textLeftPadding, int textRightPadding, List<Key> lineFonts, Key speakerFontKey,
						   int speakerLeftPadding, int speakerRightPadding, TextColor bodyTextColor, TextColor speakerNameColor,
						   DialogueHudIndicatorStyle continueIndicatorStyle) {
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
		if (bodyTextColor == null) throw new IllegalArgumentException("bodyTextColor cannot be null");
		if (speakerNameColor == null) throw new IllegalArgumentException("speakerNameColor cannot be null");
		if (continueIndicatorStyle == null) throw new IllegalArgumentException("continueIndicatorStyle cannot be null");
		if (continueIndicatorStyle.rightPadding() >= backgroundGlyph.getPixelWidth()) throw new IllegalArgumentException("indicator right padding must be less than the background glyph width");

		this.skinID = skinID;
		this.backgroundGlyph = backgroundGlyph;
		this.textLeftPadding = textLeftPadding;
		this.textRightPadding = textRightPadding;
		this.lineFonts = List.copyOf(lineFonts);
		this.speakerFontKey = speakerFontKey;
		this.speakerLeftPadding = speakerLeftPadding;
		this.speakerRightPadding = speakerRightPadding;
		this.bodyTextColor = bodyTextColor;
		this.speakerNameColor = speakerNameColor;
		this.continueIndicatorStyle = continueIndicatorStyle;
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
