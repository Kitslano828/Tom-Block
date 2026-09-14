package org.tomdang.player.stats;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerStatCategoryTest {

	@Test
	void exposesTheInitialGameplayCategories() {
		assertEquals(
				EnumSet.of(
						PlayerStatCategory.COMBAT,
						PlayerStatCategory.MINING,
						PlayerStatCategory.FORAGING,
						PlayerStatCategory.FISHING,
						PlayerStatCategory.FARMING,
						PlayerStatCategory.UTILITY
				),
				EnumSet.allOf(PlayerStatCategory.class)
		);
	}
}
