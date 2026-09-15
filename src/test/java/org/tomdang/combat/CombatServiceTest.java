package org.tomdang.combat;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.damage.PlayerDamageCalculator;
import org.tomdang.combat.damage.PlayerAttackResult;
import org.tomdang.combat.attackspeed.AttackCooldownCalculator;
import org.tomdang.combat.attackspeed.PlayerAttackCooldownService;
import org.tomdang.combat.configuration.CombatTimingConfiguration;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class CombatServiceTest {
	@Test
	void attackResultUsesCriticalStatsAndInjectedRoller() {
		Player player = mock(Player.class);
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		when(statsService.getTotalDamage(player)).thenReturn(500.0);
		when(statsService.getTotalStrength(player)).thenReturn(100.0);
		when(statsService.getTotalCritChance(player)).thenReturn(25.0);
		when(statsService.getTotalCritDamage(player)).thenReturn(50.0);
		CombatService service = new CombatService(mock(PlayerProfileService.class), mock(CustomMobResolver.class),
				statsService, mock(PlayerResourceService.class), mock(CustomMobHealthService.class),
				new PlayerDamageCalculator(), chance -> true, cooldownService(), timingConfiguration());

		PlayerAttackResult result = service.attackResult(player);
		assertEquals(900, result.damage(), 0.000001);
		assertEquals(true, result.critical());
	}

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
				new PlayerDamageCalculator(),
				cooldownService(),
				timingConfiguration()
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
				new PlayerDamageCalculator(),
				cooldownService(),
				timingConfiguration()
		);

		assertEquals(1, service.finalDamage(player), 0.000001);
	}

	@Test
	void repeatedHitsDuringCooldownDoNotRollOrDealDamageAgain() {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		LivingEntity target = mock(LivingEntity.class);
		EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
		when(event.getDamager()).thenReturn(player);
		when(event.getEntity()).thenReturn(target);

		CustomMob customMob = mock(CustomMob.class);
		CustomMobResolver resolver = mock(CustomMobResolver.class);
		when(resolver.getCustomMob(target)).thenReturn(customMob);
		CustomMobHealthService healthService = mock(CustomMobHealthService.class);
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		when(statsService.getTotalStat(player, PlayerStatType.ATTACK_SPEED)).thenReturn(100.0);
		when(statsService.getTotalDamage(player)).thenReturn(10.0);
		when(statsService.getTotalStrength(player)).thenReturn(0.0);
		when(statsService.getTotalCritChance(player)).thenReturn(0.0);
		when(statsService.getTotalCritDamage(player)).thenReturn(50.0);
		var criticalHitRoller = mock(org.tomdang.combat.damage.CriticalHitRoller.class);
		CombatService service = new CombatService(
				mock(PlayerProfileService.class), resolver, statsService,
				mock(PlayerResourceService.class), healthService, new PlayerDamageCalculator(),
				criticalHitRoller, cooldownService(), timingConfiguration()
		);

		service.damageMob(event);
		service.damageMob(event);

		verify(event, times(2)).setCancelled(true);
		verify(criticalHitRoller, times(1)).isCritical(0.0);
		verify(healthService, times(1)).damageMob(
				org.mockito.ArgumentMatchers.eq(player),
				org.mockito.ArgumentMatchers.eq(target),
				org.mockito.ArgumentMatchers.anyDouble()
		);
	}

	private PlayerAttackCooldownService cooldownService() {
		return new PlayerAttackCooldownService(new AttackCooldownCalculator(), () -> 0);
	}

	private CombatTimingConfiguration timingConfiguration() {
		return new CombatTimingConfiguration(10);
	}
}
