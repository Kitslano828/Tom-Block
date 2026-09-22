package org.tomdang.island.block;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryBlockOriginStore implements BlockOriginStore {
	private final Set<ManagedBlockPosition> positions = ConcurrentHashMap.newKeySet();
	@Override public boolean isPlayerPlaced(ManagedBlockPosition position) { return positions.contains(position); }
	@Override public void recordPlacement(ManagedBlockPosition position, UUID playerId) { positions.add(position); }
	@Override public void remove(ManagedBlockPosition position) { positions.remove(position); }
}
