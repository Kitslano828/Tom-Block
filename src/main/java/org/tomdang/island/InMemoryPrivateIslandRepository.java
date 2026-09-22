package org.tomdang.island;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryPrivateIslandRepository implements PrivateIslandRepository {
	private final Map<UUID, PrivateIsland> byOwner = new HashMap<>();
	@Override public Optional<PrivateIsland> findByOwner(UUID ownerId) { return Optional.ofNullable(byOwner.get(ownerId)); }
	@Override public PrivateIsland createForOwner(UUID ownerId) {
		return byOwner.computeIfAbsent(ownerId, PrivateIsland::starter);
	}
}
