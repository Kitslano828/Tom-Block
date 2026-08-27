package org.tomdang.dialogueframework.action;

public class DialogueChoiceActionService {

	private final DialogueChoiceActionRegistry dialogueChoiceActionRegistry;

	public DialogueChoiceActionService(DialogueChoiceActionRegistry dialogueChoiceActionRegistry) {
		if (dialogueChoiceActionRegistry == null) throw new IllegalArgumentException("dialogueChoiceActionRegistry should not be null");

		this.dialogueChoiceActionRegistry = dialogueChoiceActionRegistry;
	}

	public void executeAction(String actionID, DialogueChoiceActionContext context) {
		if (actionID == null) throw new IllegalArgumentException("ActionID should not be null");
		if (actionID.isBlank()) throw new IllegalArgumentException("ActionID should not be blank");
		if (context == null) throw new IllegalArgumentException("context should not be null");

		DialogueChoiceAction action = dialogueChoiceActionRegistry.lookupAction(actionID);
		if (action == null) throw new IllegalStateException("Action does not exist!");
		action.execute(context);
	}

}
