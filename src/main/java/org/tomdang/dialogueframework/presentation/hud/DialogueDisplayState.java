package org.tomdang.dialogueframework.presentation.hud;

import lombok.Getter;

import java.util.UUID;

public class DialogueDisplayState {

	@Getter
	private final UUID playerUUID;
	@Getter
	private final String displayNodeID;
	@Getter
	private int revealedCharacterCount = 0;
	@Getter
	private boolean currentPageFullyRevealed = false;
	@Getter
	private int currentPageIndex = 0;
	private boolean choicesShown;

	public DialogueDisplayState(UUID playerUUID, String displayNodeID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (displayNodeID == null) throw new IllegalArgumentException("Display Node ID cannot be null");
		if (displayNodeID.isBlank()) throw new IllegalArgumentException("Display Node ID cannot be blank");

		this.playerUUID = playerUUID;
		this.displayNodeID = displayNodeID;
		choicesShown = false;
	}

	public void revealCharacters(int amountToReveal, int currentPageEndingIndex) {
		if (amountToReveal <= 0) throw new IllegalArgumentException("Amount to reveal cannot be 0 or below");
		if (currentPageEndingIndex < 0) throw new IllegalArgumentException("Current page ending index cannot be negative");
		if (this.revealedCharacterCount > currentPageEndingIndex) throw new IllegalStateException("Current page ending index cannot be behind the revealed character count");

		this.revealedCharacterCount += amountToReveal;
		if (this.revealedCharacterCount > currentPageEndingIndex) this.revealedCharacterCount = currentPageEndingIndex;
		if (this.revealedCharacterCount >= currentPageEndingIndex) currentPageFullyRevealed = true;
	}

	public void revealAll(int currentPageEndingIndex) {
		if (currentPageEndingIndex < 0) throw new IllegalArgumentException("Current page ending index cannot be negative");
		if (this.revealedCharacterCount > currentPageEndingIndex) throw new IllegalStateException("Current page ending index cannot be behind the revealed character count");

		this.revealedCharacterCount = currentPageEndingIndex;
		currentPageFullyRevealed = true;
	}

	public void advancePage(int numberOfPages, int beginningCharacterIndex) {
		if (numberOfPages <= 0) {
			throw new IllegalArgumentException("numberOfPages must be greater than 0");
		}
		if (beginningCharacterIndex < 0) {
			throw new IllegalArgumentException("beginningCharacterIndex cannot be negative");
		}
		if (this.currentPageIndex + 1 >= numberOfPages) {
			throw new IllegalStateException("Cannot advance page: no next page exists");
		}

		this.currentPageIndex++;
		this.revealedCharacterCount = beginningCharacterIndex;
		this.currentPageFullyRevealed = false;
	}

	public boolean areChoicesShown() {
		return this.choicesShown;
	}

	public void markChoicesShown() {
		this.choicesShown = true;
	}

}
