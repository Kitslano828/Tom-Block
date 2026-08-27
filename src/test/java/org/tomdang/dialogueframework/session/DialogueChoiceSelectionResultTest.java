package org.tomdang.dialogueframework.session;

import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueChoice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DialogueChoiceSelectionResultTest {

	@Test
	void continuingDialogue() {
		DialogueChoice choice = mock(DialogueChoice.class);
		DialogueContext context = mock(DialogueContext.class);
		DialogueSession session = mock(DialogueSession.class);

		DialogueChoiceSelectionResult dialogueChoiceSelectionResult = new DialogueChoiceSelectionResult(choice, context, session);

		assertSame(choice, dialogueChoiceSelectionResult.getSelectedChoice());
		assertSame(context, dialogueChoiceSelectionResult.getDialogueContext());
		assertSame(session, dialogueChoiceSelectionResult.getActiveSession());
	}

	@Test
	void endedDialogue() {
		DialogueChoice choice = mock(DialogueChoice.class);
		DialogueContext context = mock(DialogueContext.class);

		DialogueChoiceSelectionResult dialogueChoiceSelectionResult = new DialogueChoiceSelectionResult(choice, context, null);
		assertNull(dialogueChoiceSelectionResult.getActiveSession());
		assertSame(choice, dialogueChoiceSelectionResult.getSelectedChoice());
		assertSame(context, dialogueChoiceSelectionResult.getDialogueContext());
	}

	@Test
	void nullChoice() {
		DialogueContext context = mock(DialogueContext.class);

		assertThrows(IllegalArgumentException.class, () -> {
			DialogueChoiceSelectionResult dialogueChoiceSelectionResult = new DialogueChoiceSelectionResult(null, context, null);
		});
	}

	@Test
	void nullContext() {
		DialogueChoice choice = mock(DialogueChoice.class);

		assertThrows(IllegalArgumentException.class, () -> {
			DialogueChoiceSelectionResult dialogueChoiceSelectionResult = new DialogueChoiceSelectionResult(choice, null, null);
		});
	}

}
