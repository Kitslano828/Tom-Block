package org.tomdang.playernpc.runtime;

import java.util.*;

public class PlayerNpcRegistry {

	private final Map<UUID, PlayerNPC> playerNPCs = new HashMap<>();

	public void register(PlayerNPC playerNPC) {
		if (playerNPC == null) throw new IllegalArgumentException("Player NPC cannot be null");
		UUID npcUUID = playerNPC.getProfileUUID();
		if (playerNPCs.containsKey(npcUUID)) throw new IllegalStateException("NPC UUID already exist");
		playerNPCs.put(npcUUID, playerNPC);
	}

	public PlayerNPC get(UUID npcUUID) {
		if (npcUUID == null) throw new IllegalArgumentException("UUID cannot be null");
		return playerNPCs.get(npcUUID);
	}

	public PlayerNPC getByEntityID(int entityID) {
		for (PlayerNPC npc : playerNPCs.values()) {
			if (npc.getEntityID() == entityID) return npc;
		}
		return null;
	}

	public boolean contains(UUID npcUUID) {
		if (npcUUID == null) throw new IllegalArgumentException("UUID cannot be null");
		return playerNPCs.containsKey(npcUUID);
	}

	public PlayerNPC remove(UUID npcUUID) {
		if (npcUUID == null) throw new IllegalArgumentException("UUID cannot be null");
		return playerNPCs.remove(npcUUID);
	}

	public Collection<PlayerNPC> getRegisteredNpcs() {
		return List.copyOf(playerNPCs.values());
	}

}
