package org.tomdang.dialogueframework.presentation.hud;

import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.text.HudTextWrapper;

import java.util.ArrayList;
import java.util.List;

public class DialogueVisibleLineService {

	private final HudTextWrapper hudTextWrapper;

	public DialogueVisibleLineService(HudTextWrapper hudTextWrapper) {
		if (hudTextWrapper == null) throw new IllegalArgumentException("hudTextWrapper cannot be null");

		this.hudTextWrapper = hudTextWrapper;
	}

	public List<String> prepare(DialogueHudSkin skin, String text, int revealedCharacterCount) {
		if (skin == null) throw new IllegalArgumentException("Skin cannot be null");
		if (text == null) throw new IllegalArgumentException("text cannot be null");
		if (revealedCharacterCount < 0) throw new IllegalArgumentException("revealedCharacterCount cannot be negative");
		if (revealedCharacterCount > text.length()) throw new IllegalArgumentException("revealedCharacterCount cannot be greater than length of text");

		int usableWidth = skin.getBackgroundGlyph().getPixelWidth() - skin.getTextLeftPadding() - skin.getTextRightPadding();
		List<String> wrappedLines = hudTextWrapper.wrap(text, usableWidth);
		if (wrappedLines.size() > skin.getMaximumLines()) throw new IllegalStateException("resulting number of lines exceeds skin.getMaximumLines()");

		List<String> visibleLines = new ArrayList<>(wrappedLines.size());

		int searchCursor = 0;

		for (String line : wrappedLines) {
			int lineStart = text.indexOf(line, searchCursor);
			if (lineStart == -1) {
				// Fallback in case of unexpected character mapping differences
				throw new IllegalStateException("wrapped line cannot be found in the original text");
			}
			int lineEnd = lineStart + line.length();

			if (revealedCharacterCount <= lineStart) {
				// Reveal count hasn't reached this line yet
				visibleLines.add("");
			} else if (revealedCharacterCount >= lineEnd) {
				// Reveal count has completely passed this line
				visibleLines.add(line);
			} else {
				// Reveal count is currently inside this line
				int visibleLength = revealedCharacterCount - lineStart;
				visibleLines.add(line.substring(0, visibleLength));
			}

			searchCursor = lineEnd;
		}

		return visibleLines;
	}

}
