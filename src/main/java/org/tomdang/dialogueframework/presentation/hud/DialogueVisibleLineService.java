package org.tomdang.dialogueframework.presentation.hud;

import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;

import java.util.ArrayList;
import java.util.List;

public class DialogueVisibleLineService {

	public List<String> prepare(DialoguePage current, String text, int revealedCharacterCount) {
		if (current == null) throw new IllegalArgumentException("current cannot be null");
		if (text == null) throw new IllegalArgumentException("text cannot be null");
		if (revealedCharacterCount < 0) throw new IllegalArgumentException("revealedCharacterCount cannot be negative");
		if (current.getEndingIndex() > text.length()) throw new IllegalArgumentException("current page ending index cannot exceed text length");
		if (revealedCharacterCount < current.getBeginningIndex()) throw new IllegalArgumentException("revealedCharacterCount cannot be before current page beginning");
		if (revealedCharacterCount > current.getEndingIndex()) throw new IllegalArgumentException("revealedCharacterCount cannot exceed current page ending");


		List<String> visibleLines = new ArrayList<>(current.getLines().size());

		int searchCursor = current.getBeginningIndex();

		for (String line : current.getLines()) {
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
