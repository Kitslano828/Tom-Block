package org.tomdang.player.skill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkillXpCurveTest {
	@Test
	void usesSeventyPercentStepsAndCumulativeThresholds() {
		assertEquals(0, SkillXpCurve.totalXpForLevel(0));
		assertEquals(35, SkillXpCurve.xpForLevel(1));
		assertEquals(88, SkillXpCurve.xpForLevel(2));
		assertEquals(123, SkillXpCurve.totalXpForLevel(2));
		assertEquals(4_900_000, SkillXpCurve.xpForLevel(60));
		assertEquals(5_110_000, SkillXpCurve.xpForLevel(61));
		for (int level = 1; level <= 100; level++) {
			assertTrue(SkillXpCurve.totalXpForLevel(level) > SkillXpCurve.totalXpForLevel(level - 1));
			assertEquals(level - 1, SkillXpCurve.levelForXp(SkillXpCurve.totalXpForLevel(level) - 1));
			assertEquals(level, SkillXpCurve.levelForXp(SkillXpCurve.totalXpForLevel(level)));
		}
	}

	@Test
	void showsProgressWithinLevelAndCapsAtOneHundred() {
		assertEquals(0, SkillProgress.fromTotalXp(0).level());
		assertEquals(35, SkillProgress.fromTotalXp(0).xpNeededForNextLevel());
		assertEquals(10, SkillProgress.fromTotalXp(45).xpIntoLevel());
		assertEquals(88, SkillProgress.fromTotalXp(45).xpNeededForNextLevel());
		SkillProgress capped = SkillProgress.fromTotalXp(SkillXpCurve.totalXpForLevel(100));
		assertTrue(capped.maxLevel());
		assertEquals(0, capped.xpNeededForNextLevel());
		assertEquals(1.0, capped.fraction());
		assertEquals(100, SkillXpCurve.levelForXp(Long.MAX_VALUE));
		assertThrows(IllegalArgumentException.class, () -> SkillXpCurve.levelForXp(-1));
		assertThrows(IllegalArgumentException.class, () -> SkillXpCurve.totalXpForLevel(101));
	}
}
