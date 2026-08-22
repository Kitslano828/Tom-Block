package org.tomdang.dialogueframework.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DialogueSessionRegistry {

	private final Map<UUID, DialogueSession> dialogueSessionMap = new HashMap<>();

	public void registerSession(DialogueSession session) {
		if (session == null) throw new IllegalArgumentException("session cannot be null");
		UUID playerUUID = session.getPlayerUUID();
		if (dialogueSessionMap.containsKey(playerUUID)) throw new IllegalStateException("Player Already in a conversation");
		dialogueSessionMap.put(playerUUID, session);
	}

	public DialogueSession getSession(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("UUID cannot be null");
		return dialogueSessionMap.get(playerUUID);
	}

	public boolean hasSession(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("UUID cannot be null");
		return dialogueSessionMap.containsKey(playerUUID);
	}

	public DialogueSession removeSession(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("UUID cannot be null");
		return dialogueSessionMap.remove(playerUUID);
	}

}
