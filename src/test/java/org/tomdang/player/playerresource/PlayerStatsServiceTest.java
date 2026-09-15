package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;
import org.tomdang.player.stats.modifier.PlayerStatModifierCalculator;
import org.tomdang.player.stats.modifier.PlayerStatModifierProvider;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import org.tomdang.player.stats.evaluation.PlayerStatEvaluation;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class PlayerStatsServiceTest {

	private UUID playerId;
	private Player player;
	private PlayerProfile profile;
	private PlayerProfileService profileService;
	private PlayerStatModifierProvider statModifierProvider;
	private PlayerStatsService statsService;

	@BeforeEach
	void setUp() {
		playerId = UUID.randomUUID();
		player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(playerId);

		profile = new PlayerProfile(playerId);
		profileService = new PlayerProfileService();
		profileService.addPlayerToMap(profile);

		statModifierProvider = mock(PlayerStatModifierProvider.class);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of());

		statsService = new PlayerStatsService(
				profileService,
				statModifierProvider,
				new PlayerStatModifierCalculator()
		);
	}

	@Test
	void healthIncludesProfileMaximumAndArmorBonus() {
		profile.setMaximumHealth(125);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.MAX_HEALTH, "equipment:armor:health", 35)
		));

		assertEquals(160, statsService.getTotalHealthStat(player), 0.000001);
	}

	@Test
	void energyIncludesProfileMaximumAndItemBonus() {
		profile.setMaximumEnergy(140);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.MAX_ENERGY, "equipment:main-hand:energy", 10)
		));

		assertEquals(150, statsService.getTotalEnergy(player), 0.000001);
	}

	@Test
	void defenseIncludesProfileAndArmorBonus() {
		profile.setDefense(24);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.DEFENSE, "equipment:armor:defense", 16)
		));

		assertEquals(40, statsService.getTotalDefense(player), 0.000001);
	}

	@Test
	void miningFortuneIncludesProfileAndToolBonus() {
		profile.setMiningFortune(12);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.MINING_FORTUNE, "equipment:main-hand:fortune", 8)
		));

		assertEquals(20, statsService.getTotalMiningFortune(player), 0.000001);
	}

	@Test
	void miningFortuneUsesProfileValueWhenNoToolIsEquipped() {
		profile.setMiningFortune(12);

		assertEquals(12, statsService.getTotalMiningFortune(player), 0.000001);
	}

	@Test
	void strengthIncludesProfileAndWeaponBonus() {
		profile.setStrength(18);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.STRENGTH, "equipment:main-hand:strength", 7)
		));

		assertEquals(25, statsService.getTotalStrength(player), 0.000001);
	}

	@Test
	void strengthUsesProfileValueWhenNoWeaponIsEquipped() {
		profile.setStrength(18);

		assertEquals(18, statsService.getTotalStrength(player), 0.000001);
	}

	@Test
	void genericStatLookupSupportsNewStatsWithoutAnotherServiceDependency() {
		profile.getStats().set(PlayerStatType.DAMAGE, 3);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.DAMAGE, "equipment:main-hand:damage", 17),
				modifier(PlayerStatType.MINING_SPEED, "equipment:main-hand:mining-speed", 45)
		));

		assertEquals(20, statsService.getTotalDamage(player), 0.000001);
		assertEquals(45, statsService.getTotalMiningSpeed(player), 0.000001);
	}

	@Test
	void criticalStatsIncludeProfileAndEquipmentContributions() {
		profile.getStats().set(PlayerStatType.CRIT_CHANCE, 10);
		profile.getStats().set(PlayerStatType.CRIT_DAMAGE, 50);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.CRIT_CHANCE, "equipment:crit-chance", 15),
				modifier(PlayerStatType.CRIT_DAMAGE, "equipment:crit-damage", 25)
		));
		assertEquals(25, statsService.getTotalCritChance(player), 0.000001);
		assertEquals(75, statsService.getTotalCritDamage(player), 0.000001);
	}

	@Test
	void completeEvaluationCollectsModifiersOnceAndBuildsBreakdownsAndSnapshot() {
		profile.setMaximumHealth(125);
		PlayerStatModifier boots = new PlayerStatModifier(
				PlayerStatType.MAX_HEALTH,
				"equipment:armor:feet:RABBIT_BOOTS:maxHealth",
				PlayerStatContributionSource.ARMOR,
				"Rabbit Boots",
				300
		);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(boots));

		PlayerStatEvaluation evaluation = statsService.evaluate(player);

		assertEquals(425, evaluation.getSnapshot().get(PlayerStatType.MAX_HEALTH), 0.000001);
		assertEquals(125, evaluation.getBreakdown(PlayerStatType.MAX_HEALTH).baseValue(), 0.000001);
		assertEquals(425, evaluation.getBreakdown(PlayerStatType.MAX_HEALTH).effectiveValue(), 0.000001);
		assertEquals(1, evaluation.getBreakdown(PlayerStatType.MAX_HEALTH).contributions().size());
		assertEquals("Rabbit Boots",
				evaluation.getBreakdown(PlayerStatType.MAX_HEALTH).contributions().getFirst().displayName());
		assertEquals(PlayerStatContributionSource.ARMOR,
				evaluation.getBreakdown(PlayerStatType.MAX_HEALTH).contributions().getFirst().source());
		assertEquals(PlayerStatType.ABILITY_HASTE.getDefaultValue(),
				evaluation.getSnapshot().get(PlayerStatType.ABILITY_HASTE), 0.000001);
		verify(statModifierProvider).getModifiers(player);
	}

	@Test
	void evaluationRejectsInvalidModifierCollections() {
		when(statModifierProvider.getModifiers(player)).thenReturn(null);
		assertThrows(IllegalStateException.class, () -> statsService.evaluate(player));

		when(statModifierProvider.getModifiers(player)).thenReturn(java.util.Arrays.asList((PlayerStatModifier) null));
		assertThrows(IllegalStateException.class, () -> statsService.evaluate(player));
	}

	@Test
	void healthAndDefenseIgnoreEachOthersArmorModifiers() {
		profile.setMaximumHealth(125);
		profile.setDefense(24);
		when(statModifierProvider.getModifiers(player)).thenReturn(List.of(
				modifier(PlayerStatType.MAX_HEALTH, "equipment:armor:health", 35),
				modifier(PlayerStatType.DEFENSE, "equipment:armor:defense", 16)
		));

		assertEquals(160, statsService.getTotalHealthStat(player), 0.000001);
		assertEquals(40, statsService.getTotalDefense(player), 0.000001);
	}

	@Test
	void missingProfileProducesDescriptiveFailure() {
		Player missingPlayer = mock(Player.class);
		UUID missingId = UUID.randomUUID();
		when(missingPlayer.getUniqueId()).thenReturn(missingId);

		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> statsService.getTotalEnergy(missingPlayer)
		);

		assertTrue(exception.getMessage().contains(missingId.toString()));
	}

	@Test
	void nullPlayerIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> statsService.getTotalHealthStat(null));
	}

	@Test
	void nullStatTypeIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> statsService.getTotalStat(player, null));
	}

	@Test
	void nullDependenciesAreRejected() {
		PlayerStatModifierCalculator calculator = new PlayerStatModifierCalculator();
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatsService(null, statModifierProvider, calculator));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatsService(profileService, null, calculator));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatsService(profileService, statModifierProvider, null));
	}

	private PlayerStatModifier modifier(PlayerStatType statType, String sourceId, double amount) {
		return new PlayerStatModifier(statType, sourceId, amount);
	}
}
