package org.tomdang.dialogueframework.presentation.hud;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DialogueDisplayStateRegistry {

	private final Map<UUID, DialogueDisplayState> displayStates = new HashMap<>();


	public void registerState(DialogueDisplayState displayState) {
		if (displayState == null) throw new IllegalArgumentException("Display state cannot be null");
		UUID playerUUID = displayState.getPlayerUUID();
		if (displayStates.containsKey(playerUUID)) throw new IllegalStateException("Player already has a display state");
		displayStates.put(playerUUID, displayState);
	}

	public DialogueDisplayState getState(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		return displayStates.get(playerUUID);
	}

	public boolean hasState(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		return displayStates.containsKey(playerUUID);
	}

	public DialogueDisplayState removeState(UUID playerUUID) {
		if (playerUUID == null) throw new IllegalArgumentException("Player UUID cannot be null");
		return displayStates.remove(playerUUID);
	}

}
