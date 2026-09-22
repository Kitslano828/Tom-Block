package org.tomdang.foraging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TreeModelTest {
	@Test void modelOakHasAConnectedTrunkAndSeparateCanopy() {
		TreeModel oak = TreeModel.modelOak();
		assertEquals("MODEL_OAK", oak.id());
		assertFalse(oak.logs().isEmpty());
		assertFalse(oak.leaves().isEmpty());
		assertEquals(oak.logs().size(), oak.logs().stream().distinct().count());
		assertEquals(1, oak.tier());
		assertEquals(100.0, oak.durability());
		assertEquals("OAK_LOG", oak.collectionId());
	}
}
