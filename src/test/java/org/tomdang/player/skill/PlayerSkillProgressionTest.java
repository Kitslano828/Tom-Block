package org.tomdang.player.skill;

import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSkillProgressionTest {
	@Test
	void levelSetterMovesXpToThresholdAndRewardsAreReversible() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());
		assertEquals(0, profile.getMiningLVL());
		assertEquals(0, profile.getCombatLvl());
		double initialFortune = profile.getMiningFortune();
		double initialStrength = profile.getStrength();
		profile.setMiningLVL(10);
		profile.setCombatLvl(10);
		assertEquals(SkillXpCurve.totalXpForLevel(10), profile.getMiningXP());
		assertEquals(SkillXpCurve.totalXpForLevel(10), profile.getCombatXP());
		assertEquals(initialFortune + 40, profile.getMiningFortune());
		assertEquals(initialStrength + 20, profile.getStrength());
		profile.resetAllStats();
		assertEquals(initialFortune + 40, profile.getMiningFortune());
		assertEquals(initialStrength + 20, profile.getStrength());
		profile.setMiningLVL(10);
		assertEquals(initialFortune + 40, profile.getMiningFortune());
		profile.setMiningXP(0);
		profile.setCombatXP(0);
		assertEquals(initialFortune, profile.getMiningFortune());
		assertEquals(initialStrength, profile.getStrength());
	}

	@Test
	void multiLevelAwardAndCapUseLongXp() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());
		profile.increaseMiningXP(1000);
		assertTrue(profile.getMiningLVL() > 2);
		profile.setMiningXP(Long.MAX_VALUE);
		assertEquals(100, profile.getMiningLVL());
		assertEquals(SkillXpCurve.totalXpForLevel(100), profile.getMiningXP());
		assertThrows(IllegalArgumentException.class, () -> profile.setCombatXP(-1));
	}
}
