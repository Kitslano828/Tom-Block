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
	private boolean fullyRevealed = false;
	private boolean choicesShown;

	public DialogueDisplayState(UUID playerUUID, String displayNodeID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (displayNodeID == null) throw new IllegalArgumentException("Display Node ID cannot be null");
		if (displayNodeID.isBlank()) throw new IllegalArgumentException("Display Node ID cannot be blank");

		this.playerUUID = playerUUID;
		this.displayNodeID = displayNodeID;
		choicesShown = false;
	}

	public void revealCharacters(int amountToReveal, int totalTextLength) {
		if (amountToReveal <= 0) throw new IllegalArgumentException("Amount to reveal cannot be 0 or below");
		if (totalTextLength < 0) throw new IllegalArgumentException("Total Text Length cannot be negative");
		if (this.revealedCharacterCount > totalTextLength) throw new IllegalStateException("Revealed Character Amount is higher than total");

		this.revealedCharacterCount += amountToReveal;
		if (this.revealedCharacterCount > totalTextLength) this.revealedCharacterCount = totalTextLength;
		if (this.revealedCharacterCount >= totalTextLength) fullyRevealed = true;
	}

	public void revealAll(int totalTextLength) {
		if (totalTextLength < 0) throw new IllegalArgumentException("Total Text Length cannot be negative");
		this.revealedCharacterCount = totalTextLength;
		fullyRevealed = true;
	}

	public boolean areChoicesShown() {
		return this.choicesShown;
	}

	public void markChoicesShown() {
		this.choicesShown = true;
	}

}
