package org.tomdang.dialogueframework.configuration;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueConfigurationLoaderTest {

	private final DialogueConfigurationLoader loader = new DialogueConfigurationLoader();

	@Test
	void validDialogueLoadsNodesChoicesAndOptionalFields() {
		List<DialogueConfigurationDefinition> definitions = load(validConfiguration());

		assertEquals(1, definitions.size());
		DialogueConfigurationDefinition dialogue = definitions.getFirst();
		assertEquals("BLACKSMITH_DIALOGUE", dialogue.dialogueID());
		assertEquals("GREETING", dialogue.startingNodeID());
		assertEquals(2, dialogue.nodes().size());

		DialogueNodeConfigurationDefinition greeting = dialogue.nodes().getFirst();
		assertEquals("GREETING", greeting.nodeID());
		assertEquals("Welcome to my forge.", greeting.text());
		assertEquals(2, greeting.choices().size());
		assertEquals("GOODBYE", greeting.choices().getFirst().nextNodeID());
		assertNull(greeting.choices().getFirst().actionID());
		assertNull(greeting.choices().get(1).nextNodeID());
		assertEquals("OPEN_FORGE", greeting.choices().get(1).actionID());
		assertTrue(dialogue.nodes().get(1).choices().isEmpty());
	}

	@Test
	void foldedDialogueTextLoadsAsOneLine() {
		String configuration = validConfiguration().replace(
				"text: \"Welcome to my forge.\"",
				"text: >-\n          Welcome to my forge.\n          Everything here is made by hand."
		);

		DialogueNodeConfigurationDefinition node = load(configuration).getFirst().nodes().getFirst();

		assertEquals("Welcome to my forge. Everything here is made by hand.", node.text());
	}

	@Test
	void missingRootAndInvalidDialogueSectionsAreRejected() {
		assertInvalid("something-else: {}\n", "dialogues");
		assertInvalid("dialogues:\n  BROKEN: value\n", "BROKEN");
	}

	@Test
	void missingStartingNodeAndNodesAreRejected() {
		assertInvalid(validConfiguration().replace("    starting-node: GREETING\n", ""), "starting-node");
		assertInvalid(validConfiguration().replace("    nodes:\n", "    something-else:\n"), "nodes");
	}

	@Test
	void invalidNodeTextIsRejected() {
		assertInvalid(validConfiguration().replace("        text: \"Welcome to my forge.\"\n", ""), "text");
	}

	@Test
	void invalidChoiceFieldsAreRejected() {
		assertInvalid(validConfiguration().replace("            display-text: \"Continue\"\n", ""), "display-text");
		assertInvalid(validConfiguration().replace("            next-node: GOODBYE", "            next-node: \"   \""), "next-node");
		assertInvalid(validConfiguration().replace("            action: OPEN_FORGE", "            action: \"   \""), "action");
	}

	@Test
	void nullReaderIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> loader.loadDefinitions(null));
	}

	private List<DialogueConfigurationDefinition> load(String contents) {
		return loader.loadDefinitions(new StringReader(contents));
	}

	private void assertInvalid(String contents, String expectedMessagePart) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> load(contents));
		assertTrue(exception.getMessage().contains(expectedMessagePart));
	}

	private String validConfiguration() {
		return """
				dialogues:
				  BLACKSMITH_DIALOGUE:
				    starting-node: GREETING
				    nodes:
				      GREETING:
				        text: "Welcome to my forge."
				        choices:
				          CONTINUE:
				            display-text: "Continue"
				            next-node: GOODBYE
				          OPEN_FORGE:
				            display-text: "Show me your work"
				            action: OPEN_FORGE
				      GOODBYE:
				        text: "Come back soon."
				""";
	}
}
