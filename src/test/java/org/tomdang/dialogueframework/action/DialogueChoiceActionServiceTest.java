package org.tomdang.dialogueframework.action;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DialogueChoiceActionServiceTest {

	@Test
	void registeredActionIsExecuted() {
		DialogueChoiceActionRegistry dialogueChoiceActionRegistry = new DialogueChoiceActionRegistry();
		DialogueChoiceAction choiceAction = mock(DialogueChoiceAction.class);
		DialogueChoiceActionContext context = mock(DialogueChoiceActionContext.class);
		dialogueChoiceActionRegistry.registerAction("OPEN_FORGE", choiceAction);

		DialogueChoiceActionService dialogueChoiceActionService = new DialogueChoiceActionService(dialogueChoiceActionRegistry);
		dialogueChoiceActionService.executeAction("OPEN_FORGE", context);

		verify(choiceAction).execute(context);
	}

	@Test
	void unknownActionIsRejected() {
		DialogueChoiceActionRegistry dialogueChoiceActionRegistry = new DialogueChoiceActionRegistry();
		DialogueChoiceActionService dialogueChoiceActionService = new DialogueChoiceActionService(dialogueChoiceActionRegistry);
		DialogueChoiceActionContext context = mock(DialogueChoiceActionContext.class);

		assertThrows(IllegalStateException.class, () -> {
			dialogueChoiceActionService.executeAction("MISSING_ACTION", context);
		});

	}

}
