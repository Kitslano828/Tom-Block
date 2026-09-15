package org.tomdang.customitemframework.refresh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerInventoryItemRefreshResultTest {

	@Test
	void exposesRefreshCounts() {
		PlayerInventoryItemRefreshResult result = new PlayerInventoryItemRefreshResult(10, 3, 5, 2);

		assertEquals(10, result.inspected());
		assertEquals(3, result.updated());
		assertEquals(5, result.skipped());
		assertEquals(2, result.failed());
	}

	@Test
	void invalidCountsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerInventoryItemRefreshResult(-1, 0, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> new PlayerInventoryItemRefreshResult(1, -1, 1, 1));
		assertThrows(IllegalArgumentException.class, () -> new PlayerInventoryItemRefreshResult(1, 1, -1, 1));
		assertThrows(IllegalArgumentException.class, () -> new PlayerInventoryItemRefreshResult(1, 1, 1, -1));
		assertThrows(IllegalArgumentException.class, () -> new PlayerInventoryItemRefreshResult(4, 1, 1, 1));
	}
}
