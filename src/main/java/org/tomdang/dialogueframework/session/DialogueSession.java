package org.tomdang.dialogueframework.session;

import lombok.Getter;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueDefinition;
import org.tomdang.dialogueframework.definition.DialogueNode;

import java.util.UUID;

public class DialogueSession {

	@Getter
	private final UUID playerUUID;
	@Getter
	private final DialogueDefinition dialogueDefinition;
	@Getter
	private String currentNodeID;
	@Getter
	private final DialogueContext dialogueContext;

	public DialogueSession(UUID playerUUID, DialogueDefinition dialogueDefinition, DialogueContext dialogueContext) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (dialogueDefinition == null) throw new IllegalArgumentException("Dialogue definition cannot be null");
		if (dialogueContext == null) throw new IllegalArgumentException("Dialogue Context cannot be null");

		this.playerUUID = playerUUID;
		this.dialogueDefinition = dialogueDefinition;
		this.currentNodeID = dialogueDefinition.getStartingNodeID();
		this.dialogueContext = dialogueContext;
	}

	public DialogueNode getCurrentNode() {
		return dialogueDefinition.getNode(currentNodeID);
	}

	public void moveToNode(String nodeID) {
		if (nodeID == null) throw new IllegalArgumentException("Node ID cannot be null");
		if (nodeID.isBlank()) throw new IllegalArgumentException("Node ID cannot be blank");
		if (dialogueDefinition.getNode(nodeID) == null) throw new IllegalArgumentException("Node ID does not exist");
		this.currentNodeID = nodeID;
	}

}
