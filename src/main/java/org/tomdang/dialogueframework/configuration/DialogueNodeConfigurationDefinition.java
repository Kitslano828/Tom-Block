package org.tomdang.dialogueframework.configuration;

import java.util.List;

public record DialogueNodeConfigurationDefinition(
		String nodeID,
		String text,
		List<DialogueChoiceConfigurationDefinition> choices
) {
	public DialogueNodeConfigurationDefinition {
		if (choices == null) throw new IllegalArgumentException("choices cannot be null");
		choices = List.copyOf(choices);
	}
}
