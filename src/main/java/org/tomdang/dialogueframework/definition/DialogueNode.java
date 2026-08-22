package org.tomdang.dialogueframework.definition;

import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogueNode {

	@Getter
	private final String nodeID;
	@Getter
	private final String dialogueText;
	@Getter
	private final List<DialogueChoice> dialogueChoices;

	public DialogueNode(String nodeID, String dialogueText, List<DialogueChoice> dialogueChoices) {
		if (nodeID == null) throw new IllegalArgumentException("Node ID cannot be null");
		if (nodeID.isBlank()) throw new IllegalArgumentException("Node ID cannot be blank");
		if (dialogueText == null) throw new IllegalArgumentException("Dialogue Text cannot be null");
		if (dialogueText.isBlank()) throw new IllegalArgumentException("Dialogue Text cannot be blank");
		if (dialogueChoices == null) throw new IllegalArgumentException("Dialogue choices cannot be null");

		// Set to track unique choice IDs during validation
		Set<String> uniqueChoiceIDs = new HashSet<>();

		// Validate contents of the choice list
		for (DialogueChoice choice : dialogueChoices) {
			if (choice == null) {
				throw new IllegalArgumentException("Any null choice inside the list is invalid");
			}

			// Check for duplicate choice IDs
			if (!uniqueChoiceIDs.add(choice.getChoiceID())) {
				throw new IllegalArgumentException("Two choices with the same choiceID: " + choice.getChoiceID());
			}
		}

		this.nodeID = nodeID;
		this.dialogueText = dialogueText;
		this.dialogueChoices = List.copyOf(dialogueChoices);

	}

	public DialogueChoice getChoice(String choiceID) {
		if (choiceID == null) throw new IllegalArgumentException("Choice ID cannot be null");
		if (choiceID.isBlank()) throw new IllegalArgumentException("Choice ID cannot be blank");
		for (DialogueChoice choice : dialogueChoices) {
			if (choiceID.equals(choice.getChoiceID()))
				return choice;
		}
		return null;
	}


}
