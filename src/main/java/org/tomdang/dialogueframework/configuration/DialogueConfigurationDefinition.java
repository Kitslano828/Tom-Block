package org.tomdang.dialogueframework.configuration;

import java.util.List;

public record DialogueConfigurationDefinition(
		String dialogueID,
		String startingNodeID,
		List<DialogueNodeConfigurationDefinition> nodes
) {
	public DialogueConfigurationDefinition {
		if (nodes == null) throw new IllegalArgumentException("nodes cannot be null");
		nodes = List.copyOf(nodes);
	}
}
