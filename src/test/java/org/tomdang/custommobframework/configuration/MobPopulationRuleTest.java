package org.tomdang.custommobframework.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MobPopulationRuleTest {
	@Test
	void acceptsAnAmbientGroundPopulation() {
		MobPopulationRule rule = rule(3, 48, 80, 16, 40);
		assertEquals("TRAINING_ZOMBIE@BLACKSMITH_DEVELOPMENT_AREA", rule.id());
	}

	@Test
	void rejectsUnsafeOrContradictoryLimits() {
		assertThrows(IllegalArgumentException.class, () -> rule(0, 48, 80, 16, 40));
		assertThrows(IllegalArgumentException.class, () -> rule(3, 48, 48, 16, 40));
		assertThrows(IllegalArgumentException.class, () -> rule(3, 48, 80, 40, 16));
		assertThrows(IllegalArgumentException.class, () -> rule(3, 48, 80, 16, 50));
	}

	@Test
	void airPlacementRequiresAnOrderedHeightRange() {
		MobPopulationRule air = new MobPopulationRule("JELLYFISH", "AREA", 3, 100,
				48, 80, 200, 8, 24, MobSpawnPlacement.AIR, 65, 90);
		assertEquals(MobSpawnPlacement.AIR, air.placement());
		assertEquals(65, air.minimumY());
		assertThrows(IllegalArgumentException.class, () -> new MobPopulationRule("JELLYFISH", "AREA", 3, 100,
				48, 80, 200, 8, 24, MobSpawnPlacement.AIR, null, null));
		assertThrows(IllegalArgumentException.class, () -> new MobPopulationRule("JELLYFISH", "AREA", 3, 100,
				48, 80, 200, 8, 24, MobSpawnPlacement.AIR, 90, 65));
	}

	@Test
	void nearbyCapCannotExceedGlobalCap() {
		MobPopulationRule spread = new MobPopulationRule("JELLYFISH", "AREA", 30, 40,
				48, 80, 200, 12, 40, MobSpawnPlacement.AIR, 65, 135, 6);
		assertEquals(6, spread.maxNearPlayer());
		assertThrows(IllegalArgumentException.class, () -> new MobPopulationRule("JELLYFISH", "AREA", 30, 40,
				48, 80, 200, 12, 40, MobSpawnPlacement.AIR, 65, 135, 31));
	}

	private MobPopulationRule rule(int maxAlive, int activation, int despawn, int minimum, int maximum) {
		return new MobPopulationRule("TRAINING_ZOMBIE", "BLACKSMITH_DEVELOPMENT_AREA",
				maxAlive, 100, activation, despawn, 200, minimum, maximum);
	}
}
