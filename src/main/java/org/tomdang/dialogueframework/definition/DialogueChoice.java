package org.tomdang.dialogueframework.definition;

import lombok.Getter;

public class DialogueChoice {

	@Getter
	private final String choiceID;
	@Getter
	private final String displayText;
	@Getter
	private final String nextNodeID;
	@Getter
	private final String actionID;
	@Getter
	private final java.util.Map<String, String> actionParameters;

	public DialogueChoice(String choiceID, String displayText, String nextNodeID) {
		this(choiceID, displayText, nextNodeID, null);
	}

	public DialogueChoice(String choiceID, String displayText, String nextNodeID, String actionID) {
		this(choiceID, displayText, nextNodeID, actionID, java.util.Map.of());
	}

	public DialogueChoice(String choiceID, String displayText, String nextNodeID, String actionID,
			java.util.Map<String, String> actionParameters) {
		if (choiceID == null ) throw new IllegalArgumentException("Choice ID cannot be null");
		if (choiceID.isBlank()) throw new IllegalArgumentException("Choice ID cannot be blank");
		if (displayText == null) throw new IllegalArgumentException("Display Text cannot be null");
		if (displayText.isBlank()) throw new IllegalArgumentException("Display Text cannot be blank");
		if (nextNodeID != null && nextNodeID.isBlank()) throw new IllegalArgumentException("Next node cannot be a non-null blank");
		if (actionID != null && actionID.isBlank()) throw new IllegalArgumentException("actionID cannot be blank");
		if (actionParameters == null || actionParameters.entrySet().stream().anyMatch(entry ->
				entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null))
			throw new IllegalArgumentException("actionParameters cannot contain null or blank keys");

		this.choiceID = choiceID;
		this.displayText = displayText;
		this.nextNodeID = nextNodeID;
		this.actionID = actionID;
		this.actionParameters = java.util.Map.copyOf(actionParameters);
	}

}
