package org.tomdang.island.block;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryBlockOriginStoreTest {
	@Test void recordsAndRemovesExactBlockOrigins() {
		InMemoryBlockOriginStore store = new InMemoryBlockOriginStore();
		ManagedBlockPosition position = new ManagedBlockPosition("island_one", 4, 70, -8);
		store.recordPlacement(position, UUID.randomUUID());
		assertTrue(store.isPlayerPlaced(position));
		assertFalse(store.isPlayerPlaced(new ManagedBlockPosition("island_one", 4, 71, -8)));
		store.remove(position);
		assertFalse(store.isPlayerPlaced(position));
	}
}
