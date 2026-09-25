package org.tomdang.hud.composition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlayerHudSessionRegistry {
	private final Map<UUID, PlayerHudSession> sessions = new LinkedHashMap<>();

	public PlayerHudSession open(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("Player id cannot be null");
		return sessions.computeIfAbsent(playerId, PlayerHudSession::new);
	}

	public Optional<PlayerHudSession> find(UUID playerId) { return Optional.ofNullable(sessions.get(playerId)); }
	public void close(UUID playerId) { sessions.remove(playerId); }
	public Collection<PlayerHudSession> all() { return java.util.List.copyOf(sessions.values()); }
}
