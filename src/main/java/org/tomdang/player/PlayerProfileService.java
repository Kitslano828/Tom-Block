package org.tomdang.player;

import lombok.Getter;

import java.util.*;

public class PlayerProfileService {

	@Getter
	private Map<UUID, PlayerProfile> playerProfiles = new HashMap<>();

	public PlayerProfileService() {

	}

	public void addPlayerToMap(PlayerProfile playerProfile) {
		playerProfiles.put(playerProfile.getUuid(), playerProfile);
	}

	public PlayerProfile getPlayerProfileFromMap(UUID uuid) {
		return playerProfiles.get(uuid);
	}

	public boolean playerAlreadyExist(UUID uuid) {
		return playerProfiles.containsKey(uuid);
	}

	public void removePlayerProfileFromMap(UUID uuid) {
		playerProfiles.remove(uuid);
	}

}
