package org.tomdang.dialogueframework.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.definition.DialogueDefinition;
import org.tomdang.dialogueframework.registry.DialogueRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DialogueConfigurationIntegrationTest {

	@Test
	void bundledBlacksmithDialogueLoadsAndRegisters() throws IOException {
		DialogueConfigurationLoader loader = new DialogueConfigurationLoader();
		List<DialogueConfigurationDefinition> definitions;

		try (InputStream stream = getClass().getClassLoader().getResourceAsStream("dialogues.yml")) {
			assertNotNull(stream, "The built plugin resources must contain dialogues.yml");
			definitions = loader.loadDefinitions(new InputStreamReader(stream, StandardCharsets.UTF_8));
		}

		DialogueRegistry registry = new DialogueRegistry();
		new DialogueConfigurationDefinitionRegistrar(registry).registerDefinitions(definitions);

		DialogueDefinition dialogue = registry.lookupDialogue("BLACKSMITH_TEST_DIALOGUE");
		assertNotNull(dialogue);
		assertEquals("GREETING", dialogue.getStartingNodeID());
		assertNotNull(dialogue.getNode("CHECKUP"));
		assertNotNull(dialogue.getNode("PAGINATION_TEST"));
		assertNotNull(dialogue.getNode("GOODBYE"));
		assertEquals("OPEN_FORGE", dialogue.getNode("GREETING").getChoice("OPEN_FORGE").getActionID());
	}
}
