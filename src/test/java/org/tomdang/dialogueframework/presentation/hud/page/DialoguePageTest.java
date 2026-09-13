package org.tomdang.dialogueframework.presentation.hud.page;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialoguePageTest {

	@Test
	void retainsLinesAndSourceIndexes() {
		DialoguePage page = new DialoguePage(List.of("First", "Second"), 4, 15);

		assertEquals(List.of("First", "Second"), page.getLines());
		assertEquals(4, page.getBeginningIndex());
		assertEquals(15, page.getEndingIndex());
	}

	@Test
	void copiesSuppliedLines() {
		List<String> suppliedLines = new ArrayList<>(List.of("First"));
		DialoguePage page = new DialoguePage(suppliedLines, 0, 5);

		suppliedLines.add("Second");

		assertEquals(List.of("First"), page.getLines());
	}

	@Test
	void exposedLinesCannotBeModified() {
		DialoguePage page = new DialoguePage(List.of("First"), 0, 5);

		assertThrows(UnsupportedOperationException.class, () -> page.getLines().add("Second"));
	}

	@Test
	void nullLinesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialoguePage(null, 0, 0));
	}

	@Test
	void emptyLinesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialoguePage(List.of(), 0, 0));
	}

	@Test
	void nullLineIsRejected() {
		List<String> lines = new ArrayList<>();
		lines.add("First");
		lines.add(null);

		assertThrows(IllegalArgumentException.class, () -> new DialoguePage(lines, 0, 5));
	}

	@Test
	void negativeBeginningIndexIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialoguePage(List.of("First"), -1, 5));
	}

	@Test
	void endingIndexBeforeBeginningIndexIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialoguePage(List.of("First"), 6, 5));
	}

	@Test
	void emptySourceRangeIsAllowed() {
		DialoguePage page = new DialoguePage(List.of(""), 0, 0);

		assertEquals(List.of(""), page.getLines());
		assertEquals(0, page.getBeginningIndex());
		assertEquals(0, page.getEndingIndex());
	}
}
