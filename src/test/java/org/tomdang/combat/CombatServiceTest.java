package org.tomdang.combat;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.damage.PlayerDamageCalculator;
import org.tomdang.combat.damage.PlayerAttackResult;
import org.tomdang.combat.attackspeed.AttackReadinessCalculator;
import org.tomdang.combat.attackspeed.AttackReadinessDamageScaler;
import org.tomdang.combat.attackspeed.PlayerAttackReadinessService;
import org.tomdang.combat.configuration.CombatTimingConfiguration;
import org.tomdang.combat.weapons.Weapon;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.combat.attackspeed.HeldItemCombatResolver;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;

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
				new PlayerDamageCalculator(), chance -> true, readinessService(() -> 0),
				new AttackReadinessDamageScaler(), heldResolver(mock(CustomItemResolver.class)));

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
				readinessService(() -> 0),
				new AttackReadinessDamageScaler(),
				heldResolver(mock(CustomItemResolver.class))
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
				readinessService(() -> 0),
				new AttackReadinessDamageScaler(),
				heldResolver(mock(CustomItemResolver.class))
		);

		assertEquals(1, service.finalDamage(player), 0.000001);
	}

	@Test
	void repeatedHitsDealReducedDamageInsteadOfBeingDiscarded() {
		AtomicLong tick = new AtomicLong(100);
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		PlayerInventory inventory = mock(PlayerInventory.class);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(mock(ItemStack.class));
		LivingEntity target = mock(LivingEntity.class);
		EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
		when(event.getDamager()).thenReturn(player);
		when(event.getEntity()).thenReturn(target);

		CustomMob customMob = mock(CustomMob.class);
		CustomMobResolver resolver = mock(CustomMobResolver.class);
		when(resolver.getCustomMob(target)).thenReturn(customMob);
		CustomMobHealthService healthService = mock(CustomMobHealthService.class);
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		when(statsService.getTotalStat(player, PlayerStatType.ATTACK_SPEED)).thenReturn(0.0);
		when(statsService.getTotalDamage(player)).thenReturn(10.0);
		when(statsService.getTotalStrength(player)).thenReturn(0.0);
		when(statsService.getTotalCritChance(player)).thenReturn(0.0);
		when(statsService.getTotalCritDamage(player)).thenReturn(50.0);
		var criticalHitRoller = mock(org.tomdang.combat.damage.CriticalHitRoller.class);
		CombatService service = new CombatService(
				mock(PlayerProfileService.class), resolver, statsService,
				mock(PlayerResourceService.class), healthService, new PlayerDamageCalculator(),
				criticalHitRoller, readinessService(tick::get), new AttackReadinessDamageScaler(),
				heldResolver(mock(CustomItemResolver.class))
		);

		service.damageMob(event);
		tick.set(100);
		service.damageMob(event);

		verify(event, times(2)).setCancelled(true);
		verify(criticalHitRoller, times(2)).isCritical(0.0);
		verify(healthService).damageMob(player, target, 10.0);
		verify(healthService).damageMob(player, target, 2.0);
	}

	@Test
	void hitContextCarriesWeaponTraitsAndReadiness() {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack stack = mock(ItemStack.class);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(stack);
		LivingEntity target = mock(LivingEntity.class);
		Weapon weapon = new Weapon("TEST_HAMMER", Material.IRON_AXE, "Test Hammer", Rarity.COMMON,
				ItemCategory.WEAPON, new CustomItemStatModifiers(Map.of(PlayerStatType.DAMAGE, 10.0)),
				CustomItemStatCapModifiers.empty(), CombatWeightClass.HEAVY, CombatDamageType.BLUNT, 30);
		CustomItemResolver itemResolver = mock(CustomItemResolver.class);
		when(itemResolver.getCustomItem(stack)).thenReturn(weapon);
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		when(statsService.getTotalStat(player, PlayerStatType.ATTACK_SPEED)).thenReturn(0.0);
		when(statsService.getTotalDamage(player)).thenReturn(10.0);

		CombatService service = new CombatService(mock(PlayerProfileService.class), mock(CustomMobResolver.class),
				statsService, mock(PlayerResourceService.class), mock(CustomMobHealthService.class),
				new PlayerDamageCalculator(), chance -> false, readinessService(() -> 100),
				new AttackReadinessDamageScaler(), heldResolver(itemResolver));

		PlayerCombatHitContext context = service.createHitContext(player, target);

		assertEquals(weapon, context.item().orElseThrow());
		assertEquals(CombatWeightClass.HEAVY, context.weightClass().orElseThrow());
		assertEquals(CombatDamageType.BLUNT, context.damageType().orElseThrow());
		assertEquals(30, context.readiness().effectiveRecoveryTicks());
		assertEquals(true, context.fullyCharged());
	}

	private PlayerAttackReadinessService readinessService(java.util.function.LongSupplier tick) {
		return new PlayerAttackReadinessService(new AttackReadinessCalculator(), tick);
	}

	private CombatTimingConfiguration timingConfiguration() {
		return new CombatTimingConfiguration(10);
	}

	private HeldItemCombatResolver heldResolver(CustomItemResolver itemResolver) {
		return new HeldItemCombatResolver(itemResolver, timingConfiguration());
	}
}
