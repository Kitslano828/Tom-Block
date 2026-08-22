package org.tomdang.dialogueframework.definition;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogueDefinition {

	@Getter
	private final String dialogueID;
	@Getter
	private final String startingNodeID;
	@Getter
	private final Map<String, DialogueNode> dialogueNodes;

	public DialogueDefinition(String dialogueID, String startingNodeID, List<DialogueNode> dialogueNodes) {
		if (dialogueID == null) throw new IllegalArgumentException("Dialogue ID cannot be null");
		if (dialogueID.isBlank()) throw new IllegalArgumentException("Dialogue ID cannot be blank");
		if (startingNodeID == null) throw new IllegalArgumentException("Starting Node ID cannot be null");
		if (startingNodeID.isBlank()) throw new IllegalArgumentException("Starting Node ID cannot be blank");
		if (dialogueNodes == null) throw new IllegalArgumentException("Dialogue Nodes cannot be null");
		if (dialogueNodes.isEmpty()) throw new IllegalArgumentException("Dialogue Nodes cannot be empty");

		Map<String, DialogueNode> tempMap = new HashMap<>();

		for (DialogueNode node : dialogueNodes) {
			// Reject a null node
			if (node == null) {
				throw new IllegalArgumentException("Dialogue node list cannot contain null elements");
			}
			String nodeID = node.getNodeID();
			if (tempMap.containsKey(nodeID)) {
				throw new IllegalArgumentException("Duplicate node ID found: " + nodeID);
			}
			tempMap.put(nodeID, node);
		}

		if (!tempMap.containsKey(startingNodeID)) throw new IllegalArgumentException("Starting node ID '" + startingNodeID + "' does not exist in the dialogue nodes map");

		for (DialogueNode node : tempMap.values()) {
			for (DialogueChoice choice : node.getDialogueChoices()) {
				String nextNodeID = choice.getNextNodeID();

				// If nextNodeID is non-null, the node map must contain that ID
				if (nextNodeID != null && !tempMap.containsKey(nextNodeID)) {
					throw new IllegalArgumentException("Choice '" + choice.getChoiceID() +
							"' in node '" + node.getNodeID() +
								"' points to a non-existent node ID: '" + nextNodeID + "'");
				}
			}
		}

		this.dialogueID = dialogueID;
		this.startingNodeID = startingNodeID;
		this.dialogueNodes = Map.copyOf(tempMap);
	}

	public DialogueNode getNode(String nodeID) {
		if (nodeID == null) throw new IllegalArgumentException("Node ID cannot be null");
		return dialogueNodes.get(nodeID);
	}

}
