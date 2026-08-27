package org.tomdang.dialogueframework.action;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DialogueChoiceActionRegistryTest {

	@Test
	void registeredActionCanBeRetrieved() {
		DialogueChoiceActionRegistry dialogueChoiceActionRegistry = new DialogueChoiceActionRegistry();

		DialogueChoiceAction choiceAction = mock(DialogueChoiceAction.class);

		dialogueChoiceActionRegistry.registerAction("OPEN_FORGE", choiceAction);

		assertTrue(dialogueChoiceActionRegistry.isActionRegistered("OPEN_FORGE"));
		assertSame(choiceAction, dialogueChoiceActionRegistry.lookupAction("OPEN_FORGE"));
	}

	@Test
	void duplicateActionIDIsRejected() {
		DialogueChoiceActionRegistry dialogueChoiceActionRegistry = new DialogueChoiceActionRegistry();

		DialogueChoiceAction choiceAction = mock(DialogueChoiceAction.class);
		DialogueChoiceAction anotherChoiceAction = mock(DialogueChoiceAction.class);

		dialogueChoiceActionRegistry.registerAction("OPEN_FORGE", choiceAction);

		assertThrows(IllegalStateException.class, () -> {
			dialogueChoiceActionRegistry.registerAction("OPEN_FORGE", anotherChoiceAction);
		});

		assertTrue(dialogueChoiceActionRegistry.isActionRegistered("OPEN_FORGE"));
		assertSame(choiceAction, dialogueChoiceActionRegistry.lookupAction("OPEN_FORGE"));
	}

	@Test
	void unknownActionIDReturnsNull() {
		DialogueChoiceActionRegistry dialogueChoiceActionRegistry = new DialogueChoiceActionRegistry();

		assertNull(dialogueChoiceActionRegistry.lookupAction("OPEN_FORGE"));
	}
}
