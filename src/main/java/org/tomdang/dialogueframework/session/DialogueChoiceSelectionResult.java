package org.tomdang.dialogueframework.session;

import lombok.Getter;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueChoice;

public class DialogueChoiceSelectionResult {

	@Getter
	private final DialogueChoice selectedChoice;
	@Getter
	private final DialogueContext dialogueContext;
	@Getter
	private final DialogueSession activeSession;

	public DialogueChoiceSelectionResult(DialogueChoice selectedChoice, DialogueContext dialogueContext, DialogueSession activeSession) {
		if (selectedChoice == null) throw new IllegalArgumentException("SelectedChoice cannot be null");
		if (dialogueContext == null) throw new IllegalArgumentException("dialogueContext cannot be null");

		this.selectedChoice = selectedChoice;
		this.dialogueContext = dialogueContext;
		this.activeSession = activeSession;
	}

}
