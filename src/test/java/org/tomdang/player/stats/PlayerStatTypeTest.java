package org.tomdang.player.stats;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerStatTypeTest {

	@Test
	void maximumHealthDefinitionMatchesCurrentProfileBehavior() {
		assertDefinition(PlayerStatType.MAX_HEALTH, "maxHealth", "Health", PlayerStatCategory.COMBAT, 100, 1);
	}

	@Test
	void maximumEnergyDefinitionMatchesCurrentProfileBehavior() {
		assertDefinition(PlayerStatType.MAX_ENERGY, "maxEnergy", "Maximum Energy", PlayerStatCategory.UTILITY, 100, 0);
	}

	@Test
	void defenseDefinitionMatchesCurrentProfileBehavior() {
		assertDefinition(PlayerStatType.DEFENSE, "defense", "Defense", PlayerStatCategory.COMBAT, 0, 0);
	}

	@Test
	void strengthDefinitionMatchesCurrentProfileBehavior() {
		assertDefinition(PlayerStatType.STRENGTH, "strength", "Strength", PlayerStatCategory.COMBAT, 0, 0);
	}

	@Test
	void damageDefinitionMatchesCurrentItemBehavior() {
		assertDefinition(PlayerStatType.DAMAGE, "damage", "Damage", PlayerStatCategory.COMBAT, 0, 0);
	}

	@Test
	void miningFortuneDefinitionMatchesCurrentProfileBehavior() {
		assertDefinition(PlayerStatType.MINING_FORTUNE, "mining-fortune", "Mining Fortune", PlayerStatCategory.MINING, 0, 0);
	}

	@Test
	void miningSpeedDefinitionMatchesCurrentItemBehavior() {
		assertDefinition(PlayerStatType.MINING_SPEED, "mining-speed", "Mining Speed", PlayerStatCategory.MINING, 0, 0);
	}

	@Test
	void choppingPowerDefinitionMatchesForagingBehavior() {
		assertDefinition(PlayerStatType.CHOPPING_POWER, "chopping-power", "Chopping Power", PlayerStatCategory.FORAGING, 0, 0);
	}

	@Test
	void abilityHasteDefinitionMatchesCooldownBehavior() {
		assertDefinition(PlayerStatType.ABILITY_HASTE, "ability-haste", "Ability Haste", PlayerStatCategory.UTILITY, 0, 0);
	}

	@Test
	void attackSpeedDefinitionUsesZeroBonusAsItsBaseline() {
		assertDefinition(PlayerStatType.ATTACK_SPEED, "attack-speed", "Attack Speed", PlayerStatCategory.COMBAT, 0, 0);
	}

	@Test
	void speedDefinitionUsesNormalMovementAsItsBaseline() {
		assertDefinition(PlayerStatType.SPEED, "speed", "Speed", PlayerStatCategory.UTILITY, 100, 0);
	}

	@Test
	void storageKeysAreUnique() {
		Set<String> storageKeys = Arrays.stream(PlayerStatType.values())
				.map(PlayerStatType::getStorageKey)
				.collect(Collectors.toSet());

		assertEquals(PlayerStatType.values().length, storageKeys.size());
	}

	private void assertDefinition(
			PlayerStatType stat,
			String storageKey,
			String displayName,
			PlayerStatCategory category,
			double defaultValue,
			double minimumValue
	) {
		assertAll(
				() -> assertEquals(storageKey, stat.getStorageKey()),
				() -> assertEquals(displayName, stat.getDisplayName()),
				() -> assertEquals(category, stat.getCategory()),
				() -> assertEquals(defaultValue, stat.getDefaultValue(), 0.000001),
				() -> assertEquals(minimumValue, stat.getMinimumValue(), 0.000001)
		);
	}
}
