package org.tomdang.dialogueframework.configuration;

public record DialogueChoiceConfigurationDefinition(String choiceID, String displayText, String nextNodeID, String actionID,
		java.util.Map<String, String> actionParameters) {
	public DialogueChoiceConfigurationDefinition(String choiceID, String displayText, String nextNodeID, String actionID) {
		this(choiceID, displayText, nextNodeID, actionID, java.util.Map.of());
	}
	public DialogueChoiceConfigurationDefinition {
		actionParameters = actionParameters == null ? java.util.Map.of() : java.util.Map.copyOf(actionParameters);
	}
}
