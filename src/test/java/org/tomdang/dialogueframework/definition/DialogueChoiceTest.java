package org.tomdang.dialogueframework.definition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DialogueChoiceTest {

	@Test
	void oldConstructorHasNoAction() {
		DialogueChoice choice = new DialogueChoice("BANANA", "BANANA!", "NEXTBANANA");

		assertNull(choice.getActionID());
		assertEquals("BANANA", choice.getChoiceID());
		assertEquals("BANANA!", choice.getDisplayText());
		assertEquals("NEXTBANANA", choice.getNextNodeID());
	}

	@Test
	void actionIDIsExposed() {
		DialogueChoice choice = new DialogueChoice("BANANA", "BANANA!", null, "OPEN_FORGE");
		assertEquals("OPEN_FORGE", choice.getActionID());
	}

	@Test
	void blankActionIDisRejected() {
		assertThrows(IllegalArgumentException.class, ()-> {
			new DialogueChoice("BANANA", "BANANA!", null, " ");
		});
	}
}
