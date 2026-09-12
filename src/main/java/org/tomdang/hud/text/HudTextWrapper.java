package org.tomdang.hud.text;

import java.util.ArrayList;
import java.util.List;

public class HudTextWrapper {

	private final HudTextWidthService hudTextWidthService;

	public HudTextWrapper(HudTextWidthService hudTextWidthService) {
		if (hudTextWidthService == null) {
			throw new IllegalArgumentException("HudTextWidthService cannot be null");
		}

		this.hudTextWidthService = hudTextWidthService;
	}

	public List<String> wrap(String text, int maxWidthPixels) {
		if (text == null) {
			throw new IllegalArgumentException("Text cannot be null");
		}
		if (maxWidthPixels <= 0) {
			throw new IllegalArgumentException("Maximum width must be greater than zero");
		}
		if (text.isEmpty()) {
			return List.of("");
		}

		List<String> wrappedLines = new ArrayList<>();

		String[] rawLines = text.split("\n", -1);
		for (String rawLine : rawLines) {
			if (rawLine.isEmpty()) {
				wrappedLines.add("");
			} else {
				wrapSingleLine(rawLine, maxWidthPixels, wrappedLines);
			}
		}

		return wrappedLines;
	}

	private void wrapSingleLine(String text, int maxWidthPixels, List<String> result) {
		String[] words = text.split(" ");
		StringBuilder currentLine = new StringBuilder();

		for (String word : words) {
			if (hudTextWidthService.measure(word) > maxWidthPixels) {
				if (!currentLine.isEmpty()) {
					result.add(currentLine.toString());
					currentLine.setLength(0);
				}
				wrapOversizedWord(word, maxWidthPixels, currentLine, result);
				continue;
			}

			String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;

			if (hudTextWidthService.measure(candidate) <= maxWidthPixels) {
				currentLine.setLength(0);
				currentLine.append(candidate);
			} else {
				result.add(currentLine.toString());
				currentLine.setLength(0);
				currentLine.append(word);
			}
		}

		if (!currentLine.isEmpty()) {
			result.add(currentLine.toString());
		}
	}

	private void wrapOversizedWord(String word, int maxWidthPixels, StringBuilder currentLine, List<String> result) {
		StringBuilder currentChunk = new StringBuilder();

		for (char c : word.toCharArray()) {
			String charStr = String.valueOf(c);
			int charWidth = hudTextWidthService.measure(charStr);

			if (charWidth > maxWidthPixels) {
				throw new IllegalArgumentException(
						"Maximum width of " + maxWidthPixels + "px cannot fit single font character '" + c + "' (" + charWidth + "px)"
				);
			}

			String candidate = currentChunk.toString() + c;
			if (hudTextWidthService.measure(candidate) <= maxWidthPixels) {
				currentChunk.append(c);
			} else {
				if (!currentChunk.isEmpty()) {
					result.add(currentChunk.toString());
				}
				currentChunk.setLength(0);
				currentChunk.append(c);
			}
		}

		if (!currentChunk.isEmpty()) {
			currentLine.append(currentChunk);
		}
	}
}