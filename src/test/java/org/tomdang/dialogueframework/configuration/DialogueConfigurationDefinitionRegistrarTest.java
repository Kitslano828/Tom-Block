package org.tomdang.dialogueframework.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.definition.DialogueDefinition;
import org.tomdang.dialogueframework.registry.DialogueRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialogueConfigurationDefinitionRegistrarTest {

	@Test
	void configurationBecomesCompleteRuntimeDialogue() {
		DialogueRegistry registry = new DialogueRegistry();
		DialogueConfigurationDefinitionRegistrar registrar = new DialogueConfigurationDefinitionRegistrar(registry);

		registrar.registerDefinitions(List.of(validDefinition("BLACKSMITH_DIALOGUE")));

		DialogueDefinition dialogue = registry.lookupDialogue("BLACKSMITH_DIALOGUE");
		assertNotNull(dialogue);
		assertEquals("GREETING", dialogue.getStartingNodeID());
		assertEquals("Welcome.", dialogue.getNode("GREETING").getDialogueText());
		assertEquals("Continue", dialogue.getNode("GREETING").getChoice("CONTINUE").getDisplayText());
		assertEquals("GOODBYE", dialogue.getNode("GREETING").getChoice("CONTINUE").getNextNodeID());
		assertEquals("OPEN_FORGE", dialogue.getNode("GREETING").getChoice("OPEN_FORGE").getActionID());
	}

	@Test
	void invalidNodeReferenceRejectsAllDefinitionsBeforeRegistration() {
		DialogueRegistry registry = new DialogueRegistry();
		DialogueConfigurationDefinitionRegistrar registrar = new DialogueConfigurationDefinitionRegistrar(registry);
		DialogueConfigurationDefinition broken = new DialogueConfigurationDefinition(
				"BROKEN",
				"START",
				List.of(new DialogueNodeConfigurationDefinition(
						"START",
						"Broken",
						List.of(new DialogueChoiceConfigurationDefinition("CONTINUE", "Continue", "MISSING", null))
				))
		);

		assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(List.of(
				validDefinition("VALID"),
				broken
		)));

		assertNull(registry.lookupDialogue("VALID"));
		assertNull(registry.lookupDialogue("BROKEN"));
	}

	@Test
	void duplicateConfiguredDialogueIDsAreRejectedBeforeRegistration() {
		DialogueRegistry registry = new DialogueRegistry();
		DialogueConfigurationDefinitionRegistrar registrar = new DialogueConfigurationDefinitionRegistrar(registry);

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(
				validDefinition("DUPLICATE"),
				validDefinition("DUPLICATE")
		)));
		assertNull(registry.lookupDialogue("DUPLICATE"));
	}

	@Test
	void dialogueAlreadyInRegistryIsRejected() {
		DialogueRegistry registry = new DialogueRegistry();
		DialogueConfigurationDefinitionRegistrar registrar = new DialogueConfigurationDefinitionRegistrar(registry);
		registrar.registerDefinitions(List.of(validDefinition("EXISTING")));

		assertThrows(IllegalStateException.class, () -> registrar.registerDefinitions(List.of(validDefinition("EXISTING"))));
	}

	@Test
	void nullDependenciesAndDefinitionsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new DialogueConfigurationDefinitionRegistrar(null));

		DialogueConfigurationDefinitionRegistrar registrar =
				new DialogueConfigurationDefinitionRegistrar(new DialogueRegistry());
		assertThrows(IllegalArgumentException.class, () -> registrar.registerDefinitions(null));
	}

	private DialogueConfigurationDefinition validDefinition(String dialogueID) {
		DialogueChoiceConfigurationDefinition continueChoice =
				new DialogueChoiceConfigurationDefinition("CONTINUE", "Continue", "GOODBYE", null);
		DialogueChoiceConfigurationDefinition forgeChoice =
				new DialogueChoiceConfigurationDefinition("OPEN_FORGE", "Show me your work", null, "OPEN_FORGE");

		return new DialogueConfigurationDefinition(
				dialogueID,
				"GREETING",
				List.of(
						new DialogueNodeConfigurationDefinition("GREETING", "Welcome.", List.of(continueChoice, forgeChoice)),
						new DialogueNodeConfigurationDefinition("GOODBYE", "Goodbye.", List.of())
				)
		);
	}
}
