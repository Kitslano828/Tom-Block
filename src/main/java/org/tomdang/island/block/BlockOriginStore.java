package org.tomdang.island.block;

import java.util.UUID;

public interface BlockOriginStore extends AutoCloseable {
	boolean isPlayerPlaced(ManagedBlockPosition position);
	void recordPlacement(ManagedBlockPosition position, UUID playerId);
	void remove(ManagedBlockPosition position);
	@Override default void close() { }
}
