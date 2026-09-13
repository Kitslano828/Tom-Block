package org.tomdang.dialogueframework.presentation.hud;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueDisplayStateTest {

	private final UUID playerUUID = UUID.randomUUID();

	@Test
	void newStateRetainsIdentityAndStartsOnFirstUnrevealedPage() {
		DialogueDisplayState state = new DialogueDisplayState(playerUUID, "GREETING");

		assertEquals(playerUUID, state.getPlayerUUID());
		assertEquals("GREETING", state.getDisplayNodeID());
		assertEquals(0, state.getCurrentPageIndex());
		assertEquals(0, state.getRevealedCharacterCount());
		assertFalse(state.isCurrentPageFullyRevealed());
		assertFalse(state.areChoicesShown());
	}

	@Test
	void nullPlayerUUIDIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueDisplayState(null, "GREETING"));
	}

	@Test
	void nullNodeIDIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueDisplayState(playerUUID, null));
	}

	@Test
	void blankNodeIDIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueDisplayState(playerUUID, "   "));
	}

	@Test
	void revealsCharactersWithoutPassingCurrentPageEnd() {
		DialogueDisplayState state = createState();

		state.revealCharacters(4, 10);

		assertEquals(4, state.getRevealedCharacterCount());
		assertFalse(state.isCurrentPageFullyRevealed());
	}

	@Test
	void revealingPastCurrentPageEndIsClampedAndCompletesPage() {
		DialogueDisplayState state = createState();

		state.revealCharacters(20, 10);

		assertEquals(10, state.getRevealedCharacterCount());
		assertTrue(state.isCurrentPageFullyRevealed());
	}

	@Test
	void revealAllMovesDirectlyToCurrentPageEnd() {
		DialogueDisplayState state = createState();
		state.revealCharacters(3, 10);

		state.revealAll(10);

		assertEquals(10, state.getRevealedCharacterCount());
		assertTrue(state.isCurrentPageFullyRevealed());
	}

	@Test
	void nonPositiveRevealAmountIsRejected() {
		DialogueDisplayState state = createState();

		assertThrows(IllegalArgumentException.class, () -> state.revealCharacters(0, 10));
		assertThrows(IllegalArgumentException.class, () -> state.revealCharacters(-1, 10));
	}

	@Test
	void negativePageEndIsRejected() {
		DialogueDisplayState state = createState();

		assertThrows(IllegalArgumentException.class, () -> state.revealCharacters(1, -1));
		assertThrows(IllegalArgumentException.class, () -> state.revealAll(-1));
	}

	@Test
	void pageEndBehindRevealPositionIsRejected() {
		DialogueDisplayState state = createState();
		state.revealCharacters(6, 10);

		assertThrows(IllegalStateException.class, () -> state.revealCharacters(1, 5));
		assertThrows(IllegalStateException.class, () -> state.revealAll(5));
	}

	@Test
	void advancingMovesToNextPageBeginningAndResetsRevealState() {
		DialogueDisplayState state = createState();
		state.revealAll(10);

		state.advancePage(3, 12);

		assertEquals(1, state.getCurrentPageIndex());
		assertEquals(12, state.getRevealedCharacterCount());
		assertFalse(state.isCurrentPageFullyRevealed());
	}

	@Test
	void advancingAcrossSeveralPagesRetainsPageOrder() {
		DialogueDisplayState state = createState();

		state.advancePage(3, 12);
		state.advancePage(3, 25);

		assertEquals(2, state.getCurrentPageIndex());
		assertEquals(25, state.getRevealedCharacterCount());
	}

	@Test
	void advancingPastFinalPageIsRejected() {
		DialogueDisplayState state = createState();
		state.advancePage(2, 12);

		assertThrows(IllegalStateException.class, () -> state.advancePage(2, 25));
	}

	@Test
	void invalidPageCountIsRejected() {
		DialogueDisplayState state = createState();

		assertThrows(IllegalArgumentException.class, () -> state.advancePage(0, 12));
		assertThrows(IllegalArgumentException.class, () -> state.advancePage(-1, 12));
	}

	@Test
	void negativeNextPageBeginningIsRejected() {
		DialogueDisplayState state = createState();

		assertThrows(IllegalArgumentException.class, () -> state.advancePage(2, -1));
	}

	@Test
	void choicesCanBeMarkedAsShown() {
		DialogueDisplayState state = createState();

		state.markChoicesShown();

		assertTrue(state.areChoicesShown());
	}

	private DialogueDisplayState createState() {
		return new DialogueDisplayState(playerUUID, "GREETING");
	}
}
