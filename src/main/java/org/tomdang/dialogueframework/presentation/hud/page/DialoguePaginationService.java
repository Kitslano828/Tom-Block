package org.tomdang.dialogueframework.presentation.hud.page;

import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.hud.text.HudTextWrapper;

import java.util.ArrayList;
import java.util.List;

public class DialoguePaginationService {

	private final HudTextWrapper hudTextWrapper;

	public DialoguePaginationService(HudTextWrapper hudTextWrapper) {
		if (hudTextWrapper == null) throw new IllegalArgumentException("hudTextWrapper cannot be null");

		this.hudTextWrapper = hudTextWrapper;
	}

	public List<DialoguePage> paginate(DialogueHudSkin skin, String text) {
		if (skin == null) throw new IllegalArgumentException("Skin cannot be null");
		if (text == null) throw new IllegalArgumentException("text cannot be null");

		int usableWidth = skin.getBackgroundGlyph().getPixelWidth() - skin.getTextLeftPadding() - skin.getTextRightPadding();
		List<String> wrappedLines = hudTextWrapper.wrap(text, usableWidth);
		int maxLines = skin.getMaximumLines();

		List<DialoguePage> pages = new ArrayList<>();

		if (wrappedLines.isEmpty() || (wrappedLines.size() == 1 && wrappedLines.get(0).isEmpty())) {
			pages.add(new DialoguePage(List.of(""), 0, 0));
			return List.copyOf(pages);
		}

		int searchPosition = 0;
		int totalLines = wrappedLines.size();

		for (int pageStart = 0; pageStart < totalLines; pageStart += maxLines) {
			int pageEnd = Math.min(pageStart + maxLines, totalLines);
			List<String> pageLines = wrappedLines.subList(pageStart, pageEnd);

			int pageBeginningIndex = -1;
			int pageEndingIndex = -1;

			for (int i = 0; i < pageLines.size(); i++) {
				String line = pageLines.get(i);
				int linePos = text.indexOf(line, searchPosition);
				if (linePos == -1) throw new IllegalStateException("the wrapped line could not be located in the source text.");

				// Record beginning index for the first line on this page
				if (i == 0) {
					pageBeginningIndex = linePos;
				}

				// Record exclusive ending index for the last line on this page
				if (i == pageLines.size() - 1) {
					pageEndingIndex = linePos + line.length();
				}

				// Move search position forward past this line
				searchPosition = linePos + line.length();
			}

			pages.add(new DialoguePage(pageLines, pageBeginningIndex, pageEndingIndex));
		}

		return List.copyOf(pages);
	}

}
