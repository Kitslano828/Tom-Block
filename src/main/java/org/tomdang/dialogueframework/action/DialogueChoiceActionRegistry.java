package org.tomdang.dialogueframework.action;

import java.util.HashMap;
import java.util.Map;

public class DialogueChoiceActionRegistry {

	private final Map<String, DialogueChoiceAction> choiceActions = new HashMap<>();

	public void registerAction(String actionID, DialogueChoiceAction action) {
		if (actionID == null) throw new IllegalArgumentException("actionID cannot be null");
		if (actionID.isBlank()) throw new IllegalArgumentException("actionID cannot be blank");
		if (action == null) throw new IllegalArgumentException("action cannot be null");

		if (choiceActions.containsKey(actionID)) throw new IllegalStateException("Action already exist under that ID");

		choiceActions.put(actionID, action);
	}

	public DialogueChoiceAction lookupAction(String actionID) {
		if (actionID == null) throw new IllegalArgumentException("actionID cannot be null");
		if (actionID.isBlank()) throw new IllegalArgumentException("actionID cannot be blank");
		return choiceActions.get(actionID);
	}

	public boolean isActionRegistered(String actionID) {
		if (actionID == null) throw new IllegalArgumentException("actionID cannot be null");
		if (actionID.isBlank()) throw new IllegalArgumentException("actionID cannot be blank");
		return choiceActions.containsKey(actionID);
	}

}
