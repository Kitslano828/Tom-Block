package org.tomdang.dialogueframework.presentation.hud;

import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialogueVisibleLineServiceTest {

	private final DialogueVisibleLineService service = new DialogueVisibleLineService();

	@Test
	void nullPageIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> service.prepare(null, "Hello", 0));
	}

	@Test
	void nullSourceTextIsRejected() {
		DialoguePage page = new DialoguePage(List.of("Hello"), 0, 5);
		assertThrows(IllegalArgumentException.class, () -> service.prepare(page, null, 0));
	}

	@Test
	void negativeRevealCountIsRejected() {
		DialoguePage page = new DialoguePage(List.of("Hello"), 0, 5);
		assertThrows(IllegalArgumentException.class, () -> service.prepare(page, "Hello", -1));
	}

	@Test
	void revealCountBeforePageBeginningIsRejected() {
		DialoguePage page = new DialoguePage(List.of("Second"), 6, 12);
		assertThrows(IllegalArgumentException.class, () -> service.prepare(page, "First Second", 5));
	}

	@Test
	void revealCountBeyondPageEndingIsRejected() {
		DialoguePage page = new DialoguePage(List.of("Hello"), 0, 5);
		assertThrows(IllegalArgumentException.class, () -> service.prepare(page, "Hello there", 6));
	}

	@Test
	void pageEndingBeyondSourceTextIsRejected() {
		DialoguePage page = new DialoguePage(List.of("Hello"), 0, 10);
		assertThrows(IllegalArgumentException.class, () -> service.prepare(page, "Hello", 0));
	}

	@Test
	void pageBeginningReturnsEmptyVersionOfEveryLine() {
		String text = "Welcome to my forge";
		DialoguePage page = new DialoguePage(List.of("Welcome to", "my forge"), 0, text.length());

		assertEquals(List.of("", ""), service.prepare(page, text, page.getBeginningIndex()));
	}

	@Test
	void partialFirstLineRevealsOnlyItsPrefix() {
		String text = "Welcome to my forge";
		DialoguePage page = new DialoguePage(List.of("Welcome to", "my forge"), 0, text.length());

		assertEquals(List.of("Welco", ""), service.prepare(page, text, 5));
	}

	@Test
	void completedFirstLineLeavesSecondLineEmpty() {
		String text = "Welcome to my forge";
		DialoguePage page = new DialoguePage(List.of("Welcome to", "my forge"), 0, text.length());

		assertEquals(List.of("Welcome to", ""), service.prepare(page, text, 10));
	}

	@Test
	void partialSecondLineRetainsCompleteFirstLine() {
		String text = "Welcome to my forge";
		DialoguePage page = new DialoguePage(List.of("Welcome to", "my forge"), 0, text.length());

		assertEquals(List.of("Welcome to", "m"), service.prepare(page, text, 12));
	}

	@Test
	void pageEndingRevealsAllPageLines() {
		String text = "Welcome to my forge";
		DialoguePage page = new DialoguePage(List.of("Welcome to", "my forge"), 0, text.length());

		assertEquals(List.of("Welcome to", "my forge"), service.prepare(page, text, page.getEndingIndex()));
	}

	@Test
	void laterPageUsesItsGlobalSourceIndexes() {
		String text = "same first same second";
		DialoguePage page = new DialoguePage(List.of("same", "second"), 11, text.length());

		assertEquals(List.of("sa", ""), service.prepare(page, text, 13));
	}

	@Test
	void explicitBlankLineIsPreserved() {
		String text = "Top\n\nBottom";
		DialoguePage page = new DialoguePage(List.of("Top", "", "Bottom"), 0, text.length());

		assertEquals(List.of("Top", "", "Bottom"), service.prepare(page, text, text.length()));
	}

	@Test
	void lineMissingFromSourceTextIsRejected() {
		DialoguePage page = new DialoguePage(List.of("Missing"), 0, 7);
		assertThrows(IllegalStateException.class, () -> service.prepare(page, "Present", 0));
	}
}
