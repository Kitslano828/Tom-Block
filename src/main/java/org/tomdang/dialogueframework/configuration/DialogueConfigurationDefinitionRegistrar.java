package org.tomdang.dialogueframework.configuration;

import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.definition.DialogueDefinition;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.registry.DialogueRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogueConfigurationDefinitionRegistrar {

	private final DialogueRegistry dialogueRegistry;

	public DialogueConfigurationDefinitionRegistrar(DialogueRegistry dialogueRegistry) {
		if (dialogueRegistry == null) throw new IllegalArgumentException("dialogueRegistry cannot be null");
		this.dialogueRegistry = dialogueRegistry;
	}

	public void registerDefinitions(List<DialogueConfigurationDefinition> configurationDefinitions) {
		if (configurationDefinitions == null) throw new IllegalArgumentException("configurationDefinitions cannot be null");

		Set<String> dialogueIDs = new HashSet<>();
		List<DialogueDefinition> runtimeDefinitions = new ArrayList<>();
		for (DialogueConfigurationDefinition configurationDefinition : configurationDefinitions) {
			if (configurationDefinition == null) throw new IllegalArgumentException("configuration definition cannot be null");
			if (!dialogueIDs.add(configurationDefinition.dialogueID())) {
				throw new IllegalStateException("Duplicate dialogue definition exists: " + configurationDefinition.dialogueID());
			}
			if (dialogueRegistry.isDialogueRegistered(configurationDefinition.dialogueID())) {
				throw new IllegalStateException("Dialogue " + configurationDefinition.dialogueID() + " is already registered");
			}
			runtimeDefinitions.add(createRuntimeDefinition(configurationDefinition));
		}

		for (DialogueDefinition runtimeDefinition : runtimeDefinitions) {
			dialogueRegistry.registerDialogue(runtimeDefinition);
		}
	}

	private DialogueDefinition createRuntimeDefinition(DialogueConfigurationDefinition definition) {
		List<DialogueNode> nodes = new ArrayList<>();
		for (DialogueNodeConfigurationDefinition nodeDefinition : definition.nodes()) {
			if (nodeDefinition == null) throw new IllegalArgumentException("Dialogue " + definition.dialogueID() + " contains a null node definition");
			List<DialogueChoice> choices = new ArrayList<>();
			for (DialogueChoiceConfigurationDefinition choiceDefinition : nodeDefinition.choices()) {
				if (choiceDefinition == null) throw new IllegalArgumentException("Node " + nodeDefinition.nodeID() + " contains a null choice definition");
				choices.add(new DialogueChoice(
						choiceDefinition.choiceID(),
						choiceDefinition.displayText(),
						choiceDefinition.nextNodeID(),
						choiceDefinition.actionID(),
						choiceDefinition.actionParameters()
				));
			}
			nodes.add(new DialogueNode(nodeDefinition.nodeID(), nodeDefinition.text(), choices));
		}
		return new DialogueDefinition(definition.dialogueID(), definition.startingNodeID(), nodes);
	}
}
