package org.tomdang.player.playerresource;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.weapons.Weapon;
import org.tomdang.customarmorframework.ArmorBonuses;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerStatsServiceTest {

	private UUID playerId;
	private Player player;
	private PlayerProfile profile;
	private PlayerProfileService profileService;
	private CustomArmorService armorService;
	private PlayerStatsService statsService;

	@BeforeEach
	void setUp() {
		playerId = UUID.randomUUID();
		player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(playerId);

		profile = new PlayerProfile(playerId);
		profileService = new PlayerProfileService();
		profileService.addPlayerToMap(profile);

		armorService = mock(CustomArmorService.class);
		when(armorService.calculateBonusStats(player)).thenReturn(armorBonuses(0, 0));

		statsService = new PlayerStatsService(profileService, armorService);
	}

	@Test
	void healthIncludesProfileMaximumAndArmorBonus() {
		profile.setMaximumHealth(125);
		when(armorService.calculateBonusStats(player)).thenReturn(armorBonuses(35, 0));

		assertEquals(160, statsService.getTotalHealthStat(player), 0.000001);
	}

	@Test
	void energyUsesProfileMaximum() {
		profile.setMaximumEnergy(140);

		assertEquals(140, statsService.getTotalEnergy(player), 0.000001);
	}

	@Test
	void defenseIncludesProfileAndArmorBonus() {
		profile.setDefense(24);
		when(armorService.calculateBonusStats(player)).thenReturn(armorBonuses(0, 16));

		assertEquals(40, statsService.getTotalDefense(player), 0.000001);
	}

	@Test
	void miningFortuneIncludesProfileAndToolBonus() {
		profile.setMiningFortune(12);
		MiningTool tool = mock(MiningTool.class);
		when(tool.getFortune()).thenReturn(8.0);

		assertEquals(20, statsService.getTotalMiningFortune(player, tool), 0.000001);
	}

	@Test
	void miningFortuneUsesProfileValueWhenNoToolIsEquipped() {
		profile.setMiningFortune(12);

		assertEquals(12, statsService.getTotalMiningFortune(player, null), 0.000001);
	}

	@Test
	void strengthIncludesProfileAndWeaponBonus() {
		profile.setStrength(18);
		Weapon weapon = mock(Weapon.class);
		when(weapon.getStrength()).thenReturn(7.0);

		assertEquals(25, statsService.getTotalStrength(player, weapon), 0.000001);
	}

	@Test
	void strengthUsesProfileValueWhenNoWeaponIsEquipped() {
		profile.setStrength(18);

		assertEquals(18, statsService.getTotalStrength(player, null), 0.000001);
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
	void nullDependenciesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatsService(null, armorService));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatsService(profileService, null));
	}

	private ArmorBonuses armorBonuses(double health, double defense) {
		ArmorBonuses bonuses = new ArmorBonuses();
		bonuses.setHealthBonus(health);
		bonuses.setDefenseBonus(defense);
		return bonuses;
	}
}
