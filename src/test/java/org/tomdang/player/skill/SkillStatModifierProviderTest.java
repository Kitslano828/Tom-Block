package org.tomdang.player.skill;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import org.tomdang.player.stats.modifier.PlayerStatModifierCalculator;
import org.tomdang.player.stats.rule.PlayerStatRule;
import org.tomdang.player.stats.rule.PlayerStatRuleRegistry;

import java.util.OptionalDouble;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SkillStatModifierProviderTest {
	@Test
	void levelRewardsAreSeparateContributorsAndSurviveBaseReset() {
		UUID id = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(id);
		PlayerProfile profile = new PlayerProfile(id);
		PlayerProfileService profiles = new PlayerProfileService();
		profiles.addPlayerToMap(profile);
		PlayerStatRuleRegistry rules = new PlayerStatRuleRegistry();
		for (PlayerStatType stat : PlayerStatType.values())
			rules.register(new PlayerStatRule(stat, OptionalDouble.empty()));
		PlayerStatsService stats = new PlayerStatsService(profiles,
				new SkillStatModifierProvider(profiles), new PlayerStatModifierCalculator(rules));

		profile.setMiningLVL(5);
		profile.setCombatLvl(3);
		profile.setMiningFortune(7);
		assertEquals(27, stats.getTotalMiningFortune(player));
		assertEquals(6, stats.getTotalStrength(player));
		var fortune = stats.evaluate(player).getBreakdown(PlayerStatType.MINING_FORTUNE);
		assertEquals(7, fortune.baseValue());
		assertEquals(1, fortune.contributions().size());
		assertEquals(PlayerStatContributionSource.SKILL, fortune.contributions().getFirst().source());
		assertEquals("skill:mining:fortune", fortune.contributions().getFirst().sourceId());
		assertEquals(20, fortune.contributions().getFirst().amount());

		profile.resetAllStats();
		assertEquals(20, stats.getTotalMiningFortune(player));
		assertEquals(6, stats.getTotalStrength(player));
		profile.setMiningLVL(2);
		assertEquals(8, stats.getTotalMiningFortune(player));
	}
}
