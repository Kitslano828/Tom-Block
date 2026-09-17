package org.tomdang.player.skill;

import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SkillProgressionServiceTest {
	private final SkillProgressionService service = new SkillProgressionService();
	private final PlayerProfile profile = new PlayerProfile(UUID.randomUUID());

	@Test
	void awardCanCrossSeveralLevelsAndReportsBothEndpoints() {
		long threshold = SkillXpCurve.totalXpForLevel(5);
		SkillAwardResult result = service.awardXp(profile, SkillType.MINING, threshold + 7);
		assertEquals(0, result.before().level());
		assertEquals(5, result.after().level());
		assertEquals(5, result.levelsGained());
		assertTrue(result.leveledUp());
		assertEquals(7, result.after().xpIntoLevel());
		assertEquals(threshold + 7, profile.getMiningXP());
		assertEquals(0, profile.getCombatXP());
	}

	@Test
	void awardsAtCapDoNotOverflowOrClaimUnearnedXp() {
		long cap = SkillXpCurve.totalXpForLevel(100);
		profile.setCombatXP(cap - 2);
		SkillAwardResult result = service.awardXp(profile, SkillType.COMBAT, Long.MAX_VALUE);
		assertEquals(2, result.awardedXp());
		assertEquals(100, result.after().level());
		assertEquals(cap, profile.getCombatXP());
		SkillAwardResult capped = service.awardXp(profile, SkillType.COMBAT, 100);
		assertEquals(0, capped.awardedXp());
		assertFalse(capped.leveledUp());
	}

	@Test
	void rejectsNegativeAwardsWithoutChangingProfile() {
		assertThrows(IllegalArgumentException.class, () -> service.awardXp(profile, SkillType.MINING, -1));
		assertEquals(0, profile.getMiningXP());
	}
}
