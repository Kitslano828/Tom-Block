package org.tomdang.player.playeractionbar;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ActionBarSuppressionService {

	private final Set<UUID> playerUUIDSet = new HashSet<>();

	public void suppress(UUID uuid) {
		if (uuid == null) throw new IllegalArgumentException("UUID cannot be null");
		playerUUIDSet.add(uuid);
	}

	public void release(UUID uuid) {
		if (uuid == null) throw new IllegalArgumentException("UUID cannot be null");
		playerUUIDSet.remove(uuid);
	}

	public boolean isSuppressed(UUID uuid) {
		if (uuid == null) throw new IllegalArgumentException("UUID cannot be null");
		return playerUUIDSet.contains(uuid);
	}

}
