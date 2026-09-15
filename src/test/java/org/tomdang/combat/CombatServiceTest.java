package org.tomdang.combat;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.damage.PlayerDamageCalculator;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CombatServiceTest {

	@Test
	void finalDamageUsesEffectiveStatsWithoutResolvingAnItemCategory() {
		Player player = mock(Player.class);
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		when(statsService.getTotalDamage(player)).thenReturn(500.0);
		when(statsService.getTotalStrength(player)).thenReturn(100.0);

		CombatService service = new CombatService(
				mock(PlayerProfileService.class),
				mock(CustomMobResolver.class),
				statsService,
				mock(PlayerResourceService.class),
				mock(CustomMobHealthService.class),
				new PlayerDamageCalculator()
		);

		assertEquals(600, service.finalDamage(player), 0.000001);
	}

	@Test
	void itemWithoutDamageUsesUnarmedFallback() {
		Player player = mock(Player.class);
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		when(statsService.getTotalDamage(player)).thenReturn(0.0);
		when(statsService.getTotalStrength(player)).thenReturn(0.0);

		CombatService service = new CombatService(
				mock(PlayerProfileService.class),
				mock(CustomMobResolver.class),
				statsService,
				mock(PlayerResourceService.class),
				mock(CustomMobHealthService.class),
				new PlayerDamageCalculator()
		);

		assertEquals(1, service.finalDamage(player), 0.000001);
	}
}
