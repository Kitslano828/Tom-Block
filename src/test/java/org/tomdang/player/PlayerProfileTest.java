package org.tomdang.player;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PlayerProfileTest {

	@Test
	void strengthUsesTheProfilesStatBlockAsItsSingleSourceOfTruth() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());

		profile.setStrength(25);
		assertEquals(25, profile.getStats().get(PlayerStatType.STRENGTH), 0.000001);

		profile.getStats().set(PlayerStatType.STRENGTH, 40);
		assertEquals(40, profile.getStrength(), 0.000001);
	}

	@Test
	void migratedStatsUseTheProfilesStatBlockAsTheirSingleSourceOfTruth() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());

		profile.setDefense(15);
		profile.setMiningFortune(20);
		profile.setMaximumHealth(150);
		profile.setMaximumEnergy(125);

		assertEquals(15, profile.getStats().get(PlayerStatType.DEFENSE), 0.000001);
		assertEquals(20, profile.getStats().get(PlayerStatType.MINING_FORTUNE), 0.000001);
		assertEquals(150, profile.getStats().get(PlayerStatType.MAX_HEALTH), 0.000001);
		assertEquals(125, profile.getStats().get(PlayerStatType.MAX_ENERGY), 0.000001);

		profile.getStats().set(PlayerStatType.DEFENSE, 30);
		profile.getStats().set(PlayerStatType.MINING_FORTUNE, 40);
		profile.getStats().set(PlayerStatType.MAX_HEALTH, 175);
		profile.getStats().set(PlayerStatType.MAX_ENERGY, 160);

		assertEquals(30, profile.getDefense(), 0.000001);
		assertEquals(40, profile.getMiningFortune(), 0.000001);
		assertEquals(175, profile.getMaximumHealth(), 0.000001);
		assertEquals(160, profile.getMaximumEnergy(), 0.000001);
	}

	@Test
	void reducingResourceMaximumClampsItsCurrentValue() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());
		profile.getHealth().setCurrent(150);
		profile.getEnergy().setCurrent(150);

		profile.setMaximumHealth(80);
		profile.setMaximumEnergy(70);

		assertEquals(80, profile.getHealth().getCurrent(), 0.000001);
		assertEquals(70, profile.getEnergy().getCurrent(), 0.000001);
	}

	@Test
	void miningFortuneAndDefenseMutatorsUseStatBlockValidation() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());
		profile.setMiningFortune(10);
		profile.setDefense(10);

		profile.increaseMiningFortune(5);
		profile.reduceDefense(20);

		assertEquals(15, profile.getMiningFortune(), 0.000001);
		assertEquals(PlayerStatType.DEFENSE.getMinimumValue(), profile.getDefense(), 0.000001);
	}

	@Test
	void profileKeepsTheSameStatBlockForItsLifetime() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());

		assertSame(profile.getStats(), profile.getStats());
	}

	@Test
	void strengthCannotBeSetBelowItsMinimum() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());

		profile.setStrength(-10);

		assertEquals(PlayerStatType.STRENGTH.getMinimumValue(), profile.getStrength(), 0.000001);
	}

	@Test
	void genericStatSetterPreservesMaximumResourceInvariants() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());
		profile.getHealth().setCurrent(200);
		profile.getEnergy().setCurrent(200);

		profile.setStat(PlayerStatType.MAX_HEALTH, 80);
		profile.setStat(PlayerStatType.MAX_ENERGY, 70);
		profile.setStat(PlayerStatType.ABILITY_HASTE, 125);

		assertEquals(80, profile.getHealth().getCurrent(), 0.000001);
		assertEquals(70, profile.getEnergy().getCurrent(), 0.000001);
		assertEquals(125, profile.getStats().get(PlayerStatType.ABILITY_HASTE), 0.000001);
	}

	@Test
	void resettingAllStatsRestoresDefaultsAndClampsCurrentResources() {
		PlayerProfile profile = new PlayerProfile(UUID.randomUUID());
		profile.getStats().set(PlayerStatType.MAX_HEALTH, 300);
		profile.getStats().set(PlayerStatType.MAX_ENERGY, 250);
		profile.getStats().set(PlayerStatType.ABILITY_HASTE, 100);
		profile.getHealth().setCurrent(250);
		profile.getEnergy().setCurrent(200);

		profile.resetAllStats();

		assertEquals(PlayerStatType.MAX_HEALTH.getDefaultValue(), profile.getHealth().getCurrent(), 0.000001);
		assertEquals(PlayerStatType.MAX_ENERGY.getDefaultValue(), profile.getEnergy().getCurrent(), 0.000001);
		for (PlayerStatType statType : PlayerStatType.values()) {
			assertEquals(statType.getDefaultValue(), profile.getStats().get(statType), 0.000001);
		}
	}
}
