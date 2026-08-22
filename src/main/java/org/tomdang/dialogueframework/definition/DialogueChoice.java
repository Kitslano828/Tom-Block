package org.tomdang.dialogueframework.definition;

import lombok.Getter;

public class DialogueChoice {

	@Getter
	private final String choiceID;
	@Getter
	private final String displayText;
	@Getter
	private final String nextNodeID;

	public DialogueChoice(String choiceID, String displayText, String nextNodeID) {
		if (choiceID == null ) throw new IllegalArgumentException("Choice ID cannot be null");
		if (choiceID.isBlank()) throw new IllegalArgumentException("Choice ID cannot be blank");
		if (displayText == null) throw new IllegalArgumentException("Display Text cannot be null");
		if (displayText.isBlank()) throw new IllegalArgumentException("Display Text cannot be blank");
		if (nextNodeID != null && nextNodeID.isBlank()) throw new IllegalArgumentException("Next node cannot be a non-null blank");

		this.choiceID = choiceID;
		this.displayText = displayText;
		this.nextNodeID = nextNodeID;



	}

}
