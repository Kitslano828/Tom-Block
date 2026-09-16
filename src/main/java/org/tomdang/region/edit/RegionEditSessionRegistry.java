package org.tomdang.region.edit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class RegionEditSessionRegistry {
	private final Map<UUID, RegionEditSession> sessions = new LinkedHashMap<>();
	private final int historyLimit;

	public RegionEditSessionRegistry(int historyLimit) {
		if (historyLimit < 1) throw new IllegalArgumentException("historyLimit must be positive");
		this.historyLimit = historyLimit;
	}

	public RegionEditSession begin(UUID playerId, String regionId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		RegionEditSession session = new RegionEditSession(regionId, historyLimit);
		sessions.put(playerId, session);
		return session;
	}

	public Optional<RegionEditSession> find(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		return Optional.ofNullable(sessions.get(playerId));
	}

	public void end(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		sessions.remove(playerId);
	}
}
