package org.tomdang.dialogueframework.presentation.hud.page;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

public class DialoguePage {
	@Getter
	private final List<String> lines;
	@Getter
	private final int beginningIndex;
	@Getter
	private final int endingIndex;

	public DialoguePage(List<String> lines, int beginningIndex, int endingIndex) {
		if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Lines cannot be null or empty");
		if (!lines.stream().allMatch(Objects::nonNull)) throw new IllegalArgumentException("Elements in lines cannot be null");
		if (beginningIndex < 0) throw new IllegalArgumentException("BeginningIndex cannot be negative");
		if (endingIndex < beginningIndex) throw new IllegalArgumentException("endingIndex cannot be before beginningIndex");

		this.lines = List.copyOf(lines);
		this.beginningIndex = beginningIndex;
		this.endingIndex = endingIndex;
	}
}
