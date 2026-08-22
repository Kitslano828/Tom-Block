package org.tomdang.dialogueframework.registry;

import org.tomdang.dialogueframework.definition.DialogueDefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DialogueRegistry {

	private final Map<String, DialogueDefinition> dialogueDefinitions = new HashMap<>();


	public void registerDialogue(DialogueDefinition definition) {
		if (definition == null) throw new IllegalArgumentException("Definition cannot be null");
		String dialogueID = definition.getDialogueID();
		if (dialogueDefinitions.containsKey(dialogueID)) throw new IllegalStateException(dialogueID + " already exists");
		dialogueDefinitions.put(dialogueID, definition);
	}

	public DialogueDefinition lookupDialogue(String dialogueID) {
		if (dialogueID == null) throw new IllegalArgumentException("Dialogue ID cannot be null");
		if (dialogueID.isBlank()) throw new IllegalArgumentException("Dialogue ID cannot be blank");
		if (!dialogueDefinitions.containsKey(dialogueID)) return null;
		return dialogueDefinitions.get(dialogueID);
	}

	public boolean isDialogueRegistered(String dialogueID) {
		if (dialogueID == null) throw new IllegalArgumentException("Dialogue ID cannot be null");
		if (dialogueID.isBlank()) throw new IllegalArgumentException("Dialogue ID cannot be blank");
		return dialogueDefinitions.containsKey(dialogueID);
	}

	public Collection<DialogueDefinition> getAllDialogueDefinitions() {
		return Collections.unmodifiableCollection(dialogueDefinitions.values());
	}


}
