package org.tomdang.platform.persistence;

import java.util.Optional;
import java.util.UUID;

/** Storage boundary: repositories persist data only and never own live player sessions. */
public interface PlayerStateRepository<T> {
	Optional<T> load(UUID playerId);
	void save(UUID playerId, T state);
}
