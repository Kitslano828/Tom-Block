package org.tomdang.platform.session;

import org.tomdang.platform.runtime.RuntimeScope;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlayerSession implements AutoCloseable {
	private final UUID playerId;
	private final RuntimeScope runtime = new RuntimeScope();
	private final Map<SessionKey<?>, Object> values = new LinkedHashMap<>();
	private PlayerSessionState state = PlayerSessionState.LOADING;

	PlayerSession(UUID playerId) { this.playerId = playerId; }
	public UUID playerId() { return playerId; }
	public PlayerSessionState state() { return state; }
	public RuntimeScope runtime() { return runtime; }

	public void activate() {
		if (state != PlayerSessionState.LOADING) throw new IllegalStateException("Only a loading session can activate");
		state = PlayerSessionState.ACTIVE;
	}

	public <T> void put(SessionKey<T> key, T value) {
		if (state == PlayerSessionState.CLOSING || state == PlayerSessionState.CLOSED) throw new IllegalStateException("Session is closing");
		if (key == null || value == null) throw new IllegalArgumentException("key and value cannot be null");
		if (!key.type().isInstance(value)) throw new IllegalArgumentException("Value does not match " + key.type().getName());
		values.put(key, value);
	}

	public <T> Optional<T> find(SessionKey<T> key) {
		return Optional.ofNullable(values.get(key)).map(key.type()::cast);
	}

	@Override
	public void close() {
		if (state == PlayerSessionState.CLOSED) return;
		state = PlayerSessionState.CLOSING;
		try { runtime.close(); }
		finally { values.clear(); state = PlayerSessionState.CLOSED; }
	}
}
