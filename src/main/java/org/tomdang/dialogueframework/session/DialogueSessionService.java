package org.tomdang.dialogueframework.session;

import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.definition.DialogueDefinition;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.registry.DialogueRegistry;

import java.util.UUID;

public class DialogueSessionService {

	private final DialogueRegistry dialogueRegistry;
	private final DialogueSessionRegistry dialogueSessionRegistry;

	public DialogueSessionService(DialogueRegistry dialogueRegistry, DialogueSessionRegistry dialogueSessionRegistry) {
		if (dialogueRegistry == null) throw new IllegalArgumentException("Dialogue Registry cannot be null");
		if (dialogueSessionRegistry == null) throw new IllegalArgumentException("Dialogue Session Registry cannot be null");

		this.dialogueRegistry = dialogueRegistry;
		this.dialogueSessionRegistry = dialogueSessionRegistry;
	}

	public DialogueSession startDialogue(UUID playerUUID, String dialogueID, DialogueContext dialogueContext) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (dialogueID == null) throw new IllegalArgumentException("Dialogue ID cannot be null");
		if (dialogueID.isBlank()) throw new IllegalArgumentException("Dialogue ID cannot be blank");
		if (dialogueContext == null) throw new IllegalArgumentException("Dialogue context cannot be null");

		if (dialogueSessionRegistry.hasSession(playerUUID)) throw new IllegalStateException("Player " + playerUUID + " is already in a dialogue");
		DialogueDefinition definition = dialogueRegistry.lookupDialogue(dialogueID);
		if (definition == null) throw new IllegalStateException(dialogueID + " is not registered");

		DialogueSession dialogueSession = new DialogueSession(playerUUID, definition, dialogueContext);
		dialogueSessionRegistry.registerSession(dialogueSession);
		return dialogueSession;
	}

	public DialogueSession getActiveSession(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		return dialogueSessionRegistry.getSession(playerUUID);
	}

	public DialogueSession endDialogue(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		return dialogueSessionRegistry.removeSession(playerUUID);
	}

	public DialogueChoiceSelectionResult selectChoice(UUID playerUUID, String choiceID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		if (choiceID == null) throw new IllegalArgumentException("Choice ID cannot be null");
		if (choiceID.isBlank()) throw new IllegalArgumentException("Choice ID cannot be blank");

		DialogueSession session = dialogueSessionRegistry.getSession(playerUUID);
		if (session == null) throw new IllegalStateException("Player Dialogue Session does not exist");
		DialogueNode node = session.getCurrentNode();
		if (node == null) throw new IllegalStateException("Dialogue Node does not exist");

		DialogueChoice choice = node.getChoice(choiceID);
		if (choice == null) throw new IllegalArgumentException("a player must only select choices belonging to their current node");

		DialogueContext context = session.getDialogueContext();

		String nextNodeID = choice.getNextNodeID();
		if (nextNodeID == null) {
			endDialogue(playerUUID);
			return new DialogueChoiceSelectionResult(choice, context, null);
		}

		session.moveToNode(nextNodeID);
		return new DialogueChoiceSelectionResult(choice, context, session);
	}

}
