package org.tomdang.platform.session;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Single owner for per-player runtime state across otherwise independent systems. */
public final class PlayerSessionCoordinator implements AutoCloseable {
	private final Map<UUID, PlayerSession> sessions = new LinkedHashMap<>();

	public PlayerSession open(UUID playerId) {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (sessions.containsKey(playerId)) throw new IllegalStateException("Player session already exists: " + playerId);
		PlayerSession session = new PlayerSession(playerId);
		sessions.put(playerId, session);
		return session;
	}

	public Optional<PlayerSession> find(UUID playerId) { return Optional.ofNullable(sessions.get(playerId)); }
	public PlayerSession require(UUID playerId) {
		return find(playerId).orElseThrow(() -> new IllegalStateException("No player session: " + playerId));
	}
	public Collection<PlayerSession> all() { return java.util.List.copyOf(sessions.values()); }

	public void close(UUID playerId) {
		PlayerSession session = sessions.remove(playerId);
		if (session != null) session.close();
	}

	@Override public void close() {
		for (PlayerSession session : java.util.List.copyOf(sessions.values())) session.close();
		sessions.clear();
	}
}
